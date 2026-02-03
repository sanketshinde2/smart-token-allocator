package com.hospital.opd.domain.enums;

public enum TokenSource {
    EMERGENCY(1),
    PAID(2),
    FOLLOW_UP(3),
    ONLINE(4),
    WALK_IN(5);

    private final int priority;

    TokenSource(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
