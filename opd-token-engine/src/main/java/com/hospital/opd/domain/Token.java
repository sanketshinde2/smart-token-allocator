package com.hospital.opd.domain;

import com.hospital.opd.domain.enums.TokenSource;
import com.hospital.opd.domain.enums.TokenStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientName;

    @Enumerated(EnumType.STRING)
    private TokenSource source;

    private int priority; // Derived from source for easier querying

    @Enumerated(EnumType.STRING)
    private TokenStatus status;

    @ManyToOne
    @JoinColumn(name = "assigned_slot_id")
    private TimeSlot assignedSlot;
    
    // For sorting in waitlist (FIFO for same priority)
    private LocalDateTime createdAt;
    
    private String contactNumber;
    private String userIdNumber; // e.g. Government ID or Hospital ID

    public Token(String patientName, String contactNumber, String userIdNumber, TokenSource source, TimeSlot slot) {
        this.patientName = patientName;
        this.contactNumber = contactNumber;
        this.userIdNumber = userIdNumber;
        this.source = source;
        this.priority = source.getPriority();
        this.status = TokenStatus.PENDING;
        this.assignedSlot = slot;
        this.createdAt = LocalDateTime.now();
    }
}
