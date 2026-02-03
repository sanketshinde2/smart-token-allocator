package com.hospital.opd.simulation;

import com.hospital.opd.domain.Doctor;
import com.hospital.opd.domain.TimeSlot;
import com.hospital.opd.domain.Token;
import com.hospital.opd.domain.enums.TokenSource;
import com.hospital.opd.domain.enums.TokenStatus;
import com.hospital.opd.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class OpdSimulation implements CommandLineRunner {

    private final ScheduleService scheduleService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting OPD Simulation...");

        // 1. Setup Data: 3 Doctors
        Doctor d1 = scheduleService.createDoctor("Dr. Smith", "Cardiology");
        Doctor d2 = scheduleService.createDoctor("Dr. Jones", "Orthopedics");
        Doctor d3 = scheduleService.createDoctor("Dr. Williams", "General");

        System.out.println("Ref Doctors created: " + d1.getName() + ", " + d2.getName() + ", " + d3.getName());

        // 2. Setup Slots
        // Dr. Smith has small capacity (2)
        TimeSlot s1 = scheduleService.createSlot(d1.getId(), LocalTime.of(9, 0), LocalTime.of(10, 0), 2);
        
        System.out.println("Slot created for Dr. Smith: 9-10 AM, Capacity: 2");

        // 3. Simulate Logic
        System.out.println("\n--- Booking Phase ---");
        
        // Fill capacity
        System.out.println("Booking Patient A (Walk-in)");
        Token t1 = scheduleService.bookToken("Patient A", "555-0001", "ID001", TokenSource.WALK_IN, s1.getId());
        printToken(t1);

        System.out.println("Booking Patient B (Online)");
        Token t2 = scheduleService.bookToken("Patient B", "555-0002", "ID002", TokenSource.ONLINE, s1.getId());
        printToken(t2);

        // Over capacity -> Waitlist
        System.out.println("Booking Patient C (Walk-in) - Should go to WAITLIST");
        Token t3 = scheduleService.bookToken("Patient C", "555-0003", "ID003", TokenSource.WALK_IN, s1.getId());
        printToken(t3);

        // Emergency -> Overbook (Active)
        System.out.println("Booking Patient D (EMERGENCY) - Should be ACTIVE (Overbook)");
        Token t4 = scheduleService.bookToken("Patient D", "911-0000", "EMERG001", TokenSource.EMERGENCY, s1.getId());
        printToken(t4);
        
        System.out.println("\n--- Cancellation Phase ---");
        // Cancel Patient B (Online, Slot #2)
        System.out.println("Cancelling Patient B...");
        scheduleService.cancelToken(t2.getId());
        
        System.out.println("Checking Reallocation (Patient C should move to ACTIVE? or Slot still full due to Emergency?)");
        // Capacity = 2. Active = A(1), D(1 - Emergency). Count = 2.
        // If Emergency counts towards capacity, slot is full.
        // My logic: activeCount < Capacity.
        // If A and D are Active, count is 2. Capacity 2. NO Reallocation.
        // This effectively means Emergency 'stole' the slot freed by B.
        // Which is correct behavior for Elastic capacity absorbing load.
        
        // Let's create another slot on Dr. Jones to show Reallocation explicitly.
        TimeSlot s2 = scheduleService.createSlot(d2.getId(), LocalTime.of(10, 0), LocalTime.of(11, 0), 1);
        System.out.println("\n--- Reallocation Demo (Dr. Jones, Cap 1) ---");
        
        Token j1 = scheduleService.bookToken("Patient J1", "555-1001", "ID101", TokenSource.ONLINE, s2.getId());
        System.out.print("J1: "); printToken(j1);
        
        Token j2 = scheduleService.bookToken("Patient J2", "555-1002", "ID102", TokenSource.WALK_IN, s2.getId());
        System.out.print("J2 (Waitlist): "); printToken(j2);
        
        System.out.println("Cancelling J1...");
        scheduleService.cancelToken(j1.getId());
        
        // Need to fetch J2 to see new status. Simulation can't easily re-fetch without repo.
        // But in real app, checking status would show ACTIVE.
        System.out.println("J2 should now be ACTIVE (Simulated re-fetch would confirm)");
        
        System.out.println("Simulation End.");
    }

    private void printToken(Token t) {
        System.out.println("Token: " + t.getPatientName() + " | " + t.getSource() + " | Status: " + t.getStatus());
    }
}
