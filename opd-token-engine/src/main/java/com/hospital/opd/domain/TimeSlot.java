package com.hospital.opd.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    private LocalTime startTime;
    private LocalTime endTime;
    
    private int maxCapacity;
    
    public TimeSlot(Doctor doctor, LocalTime startTime, LocalTime endTime, int maxCapacity) {
        this.doctor = doctor;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = maxCapacity;
    }
}
