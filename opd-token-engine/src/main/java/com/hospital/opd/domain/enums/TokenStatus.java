package com.hospital.opd.domain.enums;

public enum TokenStatus {
    PENDING,       // Initial state before allocation
    WAITLIST,      // Queueing for a slot
    ACTIVE,        // Assigned a slot
    VISITED,       // Completed
    CANCELLED,     // User cancelled
    NO_SHOW,       // Missed appointment
    RESCHEDULED    // Bumped by emergency
}
