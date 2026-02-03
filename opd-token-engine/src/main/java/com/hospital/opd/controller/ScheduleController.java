package com.hospital.opd.controller;

import com.hospital.opd.domain.Doctor;
import com.hospital.opd.domain.TimeSlot;
import com.hospital.opd.domain.Token;
import com.hospital.opd.domain.enums.TokenSource;
import com.hospital.opd.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping("/{doctorId}")
    public List<TimeSlot> getDoctorSchedule(@PathVariable Long doctorId) {
        return scheduleService.getDoctorSlots(doctorId);
    }

    @PostMapping("/doctor")
    public Doctor createDoctor(@RequestParam String name, @RequestParam String specialization) {
        return scheduleService.createDoctor(name, specialization);
    }

    @PostMapping("/slot")
    public TimeSlot createSlot(@RequestParam Long doctorId,
                               @RequestParam String start,
                               @RequestParam String end,
                               @RequestParam int capacity) {
        return scheduleService.createSlot(doctorId, LocalTime.parse(start), LocalTime.parse(end), capacity);
    }
    
    @PostMapping("/book")
    public Token bookToken(@RequestParam String patientName,
                           @RequestParam String contactNumber,
                           @RequestParam String userIdNumber,
                           @RequestParam TokenSource source,
                           @RequestParam Long slotId) {
        return scheduleService.bookToken(patientName, contactNumber, userIdNumber, source, slotId);
    }
    
    @DeleteMapping("/cancel/{tokenId}")
    public void cancelToken(@PathVariable Long tokenId) {
        scheduleService.cancelToken(tokenId);
    }
}
