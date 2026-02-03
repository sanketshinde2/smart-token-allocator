import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleOpdEngine {

    // Enums
    enum TokenSource {
        EMERGENCY(1), PAID(2), FOLLOW_UP(3), ONLINE(4), WALK_IN(5);
        int priority;
        TokenSource(int p) { priority = p; }
    }

    enum TokenStatus { CREATED, ACTIVE, VISITED, CANCELLED, NO_SHOW, RESCHEDULED }

    // Entities
    static class Token {
        static long idCounter = 1;
        Long id = idCounter++;
        String patientName;
        TokenSource source;
        int priority;
        TokenStatus status;
        long assignedSlotId;
        long createdAt;

        public Token(String name, TokenSource source, long slotId) {
            this.patientName = name;
            this.source = source;
            this.priority = source.priority;
            this.status = TokenStatus.ACTIVE; // Default to active if capacity allows, else created
            this.assignedSlotId = slotId;
            this.createdAt = System.nanoTime();
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s (%s) - %s", id, patientName, source, status);
        }
    }

    static class TimeSlot {
        Long id;
        int capacity;
        List<Token> tokens = new ArrayList<>();

        public TimeSlot(Long id, int capacity) {
            this.id = id;
            this.capacity = capacity;
        }
    }

    // Database simulation
    static Map<Long, TimeSlot> slots = new HashMap<>();
    static List<Token> allTokens = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Running Standalone OPD Engine Simulation...");

        // 1. Create Slot with capacity 2
        Long slotId = 100L;
        slots.put(slotId, new TimeSlot(slotId, 2));
        System.out.println("Created Slot 100 with Capacity 2");

        // 2. Book Normal Patients
        book(slotId, "Patient A", TokenSource.WALK_IN); // Active 1/2
        book(slotId, "Patient B", TokenSource.ONLINE);  // Active 2/2
        book(slotId, "Patient C", TokenSource.WALK_IN); // Full -> Waitlist

        // 3. Emergency Bump
        System.out.println("\n--- Emergency Booking ---");
        book(slotId, "Patient D", TokenSource.EMERGENCY); 

        // 4. Verify State
        printSlotState(slotId);
        
        // 5. Patient Visits (Process Emergency)
        System.out.println("\n--- Patient Visits (Reallocation) ---");
        // Assume Patient D (Emergency) visits
        Token emergencyToken = allTokens.stream()
                .filter(t -> t.patientName.equals("Patient D") && t.status == TokenStatus.ACTIVE)
                .findFirst().orElse(null);
        
        if (emergencyToken != null) {
            updateStatus(emergencyToken, TokenStatus.VISITED);
        }
        
        printSlotState(slotId);
    }

    static void book(Long slotId, String name, TokenSource source) {
        TimeSlot slot = slots.get(slotId);
        long activeCount = allTokens.stream()
                .filter(t -> t.assignedSlotId == slotId && t.status == TokenStatus.ACTIVE)
                .count();

        if (activeCount < slot.capacity) {
            Token t = new Token(name, source, slotId);
            t.status = TokenStatus.ACTIVE;
            allTokens.add(t);
            System.out.println("Booked: " + t);
        } else {
            if (source == TokenSource.EMERGENCY) {
                handleEmergency(slotId, name);
            } else {
                Token t = new Token(name, source, slotId);
                t.status = TokenStatus.CREATED; // Waitlist
                allTokens.add(t);
                System.out.println("Waitlisted: " + t);
            }
        }
    }

    static void handleEmergency(Long slotId, String name) {
        // Find lowest priority active token
        // Priority value: 1 (High) to 5 (Low)
        // We want bumping candidate with HIGHEST priority value (Lowest importance)
        
        List<Token> activeTokens = allTokens.stream()
                .filter(t -> t.assignedSlotId == slotId && t.status == TokenStatus.ACTIVE)
                .sorted((t1, t2) -> Integer.compare(t2.priority, t1.priority)) // Descending by value (5, 4...)
                .collect(Collectors.toList());

        if (activeTokens.isEmpty()) {
            Token t = new Token(name, TokenSource.EMERGENCY, slotId);
            t.status = TokenStatus.ACTIVE;
            allTokens.add(t);
            System.out.println("Emergency Booked (Empty Slot): " + t);
            return;
        }

        Token victim = activeTokens.get(0);
        if (victim.priority == 1) {
            Token t = new Token(name, TokenSource.EMERGENCY, slotId);
            t.status = TokenStatus.CREATED;
            allTokens.add(t); // Cannot bump emergency
            System.out.println("Emergency Waitlisted (Slot full of emergencies): " + t);
            return;
        }

        // Bump
        victim.status = TokenStatus.RESCHEDULED;
        System.out.println("BUMPED: " + victim);

        Token emergency = new Token(name, TokenSource.EMERGENCY, slotId);
        emergency.status = TokenStatus.ACTIVE;
        allTokens.add(emergency);
        System.out.println("Emergency Booked (Bumped): " + emergency);
    }

    static void updateStatus(Token t, TokenStatus newStatus) {
        TokenStatus oldStatus = t.status;
        t.status = newStatus;
        System.out.println("Status Update: " + t.patientName + " -> " + newStatus);

        if (oldStatus == TokenStatus.ACTIVE && (newStatus == TokenStatus.VISITED || newStatus == TokenStatus.CANCELLED)) {
            reallocate(t.assignedSlotId);
        }
    }

    static void reallocate(Long slotId) {
        // Find best waitlist candidate
        // Candidates: CREATED or RESCHEDULED
        // Sort by Priority (Ascending Value 1..5) then Creation Time
        
        List<Token> candidates = allTokens.stream()
                .filter(t -> t.assignedSlotId == slotId && (t.status == TokenStatus.CREATED || t.status == TokenStatus.RESCHEDULED))
                .sorted((t1, t2) -> {
                    if (t1.priority != t2.priority) return Integer.compare(t1.priority, t2.priority);
                    return Long.compare(t1.createdAt, t2.createdAt);
                })
                .collect(Collectors.toList());

        if (!candidates.isEmpty()) {
            Token lucky = candidates.get(0);
            lucky.status = TokenStatus.ACTIVE;
            System.out.println("Reallocated (Promoted): " + lucky);
        }
    }

    static void printSlotState(Long slotId) {
        System.out.println("\n[Slot State]");
        System.out.println("ACTIVE:");
        allTokens.stream().filter(t -> t.assignedSlotId == slotId && t.status == TokenStatus.ACTIVE)
                .forEach(t -> System.out.println("  " + t));
        System.out.println("WAITLIST/RESCHEDULED:");
        allTokens.stream().filter(t -> t.assignedSlotId == slotId && (t.status == TokenStatus.CREATED || t.status == TokenStatus.RESCHEDULED))
                .forEach(t -> System.out.println("  " + t));
    }
}
