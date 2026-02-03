import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InteractiveOpdEngine {

    // Enums
    enum TokenSource {
        EMERGENCY(1), PAID(2), FOLLOW_UP(3), ONLINE(4), WALK_IN(5);
        int priority;
        TokenSource(int p) { priority = p; }
        
        public static TokenSource fromInt(int i) {
            for (TokenSource s : values()) if (s.ordinal() + 1 == i) return s;
            return WALK_IN;
        }
    }

    enum TokenStatus { CREATED, ACTIVE, VISITED, CANCELLED, NO_SHOW, RESCHEDULED }

    // Entities
    static class Token {
        static long idCounter = 1;
        Long id = idCounter++;
        String patientName;
        String contactNumber;
        String userIdNumber;
        TokenSource source;
        int priority;
        TokenStatus status;
        long assignedSlotId;
        long createdAt;
        LocalDateTime visitedAt;

        public Token(String name, String contact, String uid, TokenSource source, long slotId) {
            this.patientName = name;
            this.contactNumber = contact;
            this.userIdNumber = uid;
            this.source = source;
            this.priority = source.priority;
            this.status = TokenStatus.ACTIVE;
            this.assignedSlotId = slotId;
            this.createdAt = System.nanoTime();
        }
        
        @Override
        public String toString() {
            String vTime = visitedAt != null ? " | Visited: " + visitedAt.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "";
            return String.format("ID: %d | %-10s | %-12s | %-10s | %-10s | %s%s", 
                id, patientName, contactNumber, userIdNumber, source, status, vTime);
        }
    }

    static class TimeSlot {
        Long id;
        int capacity;
        String doctorName;
        String timeRange;
        
        public TimeSlot(Long id, int capacity, String doctorName, String timeRange) {
            this.id = id;
            this.capacity = capacity;
            this.doctorName = doctorName;
            this.timeRange = timeRange;
        }
    }

    // State
    static Map<Long, TimeSlot> slots = new HashMap<>();
    static List<Token> allTokens = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   OPD Token Allocation Engine (Interactive)");
        System.out.println("==========================================");

        // Setup initial slot
        long defaultSlotId = 1L;
        
        System.out.print("\nInitialize System - Enter Doctor Name: ");
        String docName = scanner.nextLine();
        if (docName.trim().isEmpty()) docName = "Dr. Default";
        
        System.out.print("Enter Slot Capacity for " + docName + " (e.g., 3): ");
        int cap = 3;
        try {
            String input = scanner.nextLine();
            cap = Integer.parseInt(input);
        } catch (Exception e) {}
        System.out.print("Enter Time Slot (e.g., 09:00 - 10:00): ");
        String timeRange = scanner.nextLine();
        if (timeRange.trim().isEmpty()) timeRange = "09:00 - 10:00";
        
        slots.put(defaultSlotId, new TimeSlot(defaultSlotId, cap, docName, timeRange));
        System.out.println(">> Created Slot #1 for " + docName + " (" + timeRange + ") with Capacity: " + cap);

        while (true) {
            System.out.println("\n---------------- MENU ----------------");
            System.out.println("1. Book Token");
            System.out.println("2. View Schedule");
            System.out.println("3. Patient Visit (Mark Completed)");
            System.out.println("4. View Visited History");
            System.out.println("5. Exit");
            System.out.print("Select Option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    bookFlow(defaultSlotId);
                    break;
                case "2":
                    printSlotState(defaultSlotId);
                    break;
                case "3":
                    visitFlow(defaultSlotId);
                    break;
                case "4":
                    printVisitedHistory(defaultSlotId);
                    break;
                case "5":
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    static void bookFlow(Long slotId) {
        System.out.print("Enter Patient Name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter Contact Number: ");
        String contact = scanner.nextLine();
        
        System.out.print("Enter Government/Hospital ID: ");
        String uid = scanner.nextLine();
        
        System.out.println("Select Source:");
        System.out.println("  1. EMERGENCY (Priority 1)");
        System.out.println("  2. PAID (Priority 2)");
        System.out.println("  3. FOLLOW_UP (Priority 3)");
        System.out.println("  4. ONLINE (Priority 4)");
        System.out.println("  5. WALK_IN (Priority 5)");
        System.out.print("Choice (1-5): ");
        
        int sourceIdx = 5;
        try {
            sourceIdx = Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {}
        TokenSource source = TokenSource.fromInt(sourceIdx);

        book(slotId, name, contact, uid, source);
    }

    static void book(Long slotId, String name, String contact, String uid, TokenSource source) {
        TimeSlot slot = slots.get(slotId);
        long activeCount = allTokens.stream()
                .filter(t -> t.assignedSlotId == slotId && t.status == TokenStatus.ACTIVE)
                .count();

        if (activeCount < slot.capacity) {
            Token t = new Token(name, contact, uid, source, slotId);
            t.status = TokenStatus.ACTIVE;
            allTokens.add(t);
            System.out.println(">> SUCCESS: Token Generated. Status: ACTIVE");
        } else {
            // Full
            if (source == TokenSource.EMERGENCY) {
                handleEmergency(slotId, name, contact, uid);
            } else {
                Token t = new Token(name, contact, uid, source, slotId);
                t.status = TokenStatus.CREATED;
                allTokens.add(t);
                System.out.println(">> SLOT FULL: Added to WAITLIST. Status: CREATED");
            }
        }
    }

    static void handleEmergency(Long slotId, String name, String contact, String uid) {
        // Find lowest priority active token
        List<Token> activeTokens = allTokens.stream()
                .filter(t -> t.assignedSlotId == slotId && t.status == TokenStatus.ACTIVE)
                .sorted((t1, t2) -> Integer.compare(t2.priority, t1.priority)) // Descending priority value (5, 4...)
                .collect(Collectors.toList());

        if (activeTokens.isEmpty()) { 
             // Should not happen if full
             return;
        }

        Token victim = activeTokens.get(0);
        if (victim.priority == 1) {
            Token t = new Token(name, contact, uid, TokenSource.EMERGENCY, slotId);
            t.status = TokenStatus.CREATED;
            allTokens.add(t);
            System.out.println(">> CRITICAL: All slots occupied by Emergency patients. Added to Waitlist.");
            return;
        }

        // Bump
        victim.status = TokenStatus.RESCHEDULED;
        Token emergency = new Token(name, contact, uid, TokenSource.EMERGENCY, slotId);
        emergency.status = TokenStatus.ACTIVE;
        allTokens.add(emergency);
        
        System.out.println(">> EMERGENCY OVERRIDE TRIGGERED!");
        System.out.println("   Bumped: " + victim.patientName + " (Moved to WAITLIST/RESCHEDULED)");
        System.out.println("   Active: " + emergency.patientName + " (EMERGENCY)");
    }

    static void visitFlow(Long slotId) {
        List<Token> active = allTokens.stream()
            .filter(t -> t.assignedSlotId == slotId && t.status == TokenStatus.ACTIVE)
            .collect(Collectors.toList());
            
        if (active.isEmpty()) {
            System.out.println("No active patients to visit.");
            return;
        }
        
        System.out.println("Select Patient to Mark Processed:");
        for (int i = 0; i < active.size(); i++) {
            System.out.println((i+1) + ". " + active.get(i).patientName);
        }
        System.out.print("Choice: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx >= 0 && idx < active.size()) {
                updateStatus(active.get(idx), TokenStatus.VISITED);
            }
        } catch (Exception e) {
             System.out.println("Invalid choice.");
        }
    }

    static void updateStatus(Token t, TokenStatus newStatus) {
        TokenStatus oldStatus = t.status;
        t.status = newStatus;
        System.out.println(">> Updated " + t.patientName + " to " + newStatus);

        if (oldStatus == TokenStatus.ACTIVE && newStatus == TokenStatus.VISITED) {
            t.visitedAt = LocalDateTime.now();
        }

        if (oldStatus == TokenStatus.ACTIVE && (newStatus == TokenStatus.VISITED || newStatus == TokenStatus.CANCELLED)) {
            reallocate(t.assignedSlotId);
        }
    }

    static void reallocate(Long slotId) {
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
            System.out.println(">> SLOT FREED: Promoted " + lucky.patientName + " from Waitlist to ACTIVE.");
        }
    }

    static void printSlotState(Long slotId) {
        TimeSlot s = slots.get(slotId);
        long activeCount = allTokens.stream().filter(t -> t.assignedSlotId == slotId && t.status == TokenStatus.ACTIVE).count();
        
        System.out.println("\n--- Current Schedule for " + s.doctorName + " [" + s.timeRange + "] (Capacity: " + activeCount + "/" + s.capacity + ") ---");
        System.out.println("[ ACTIVE ]");
        allTokens.stream().filter(t -> t.assignedSlotId == slotId && t.status == TokenStatus.ACTIVE)
                .forEach(t -> System.out.println("  " + t));
        
        System.out.println("[ WAITLIST ]");
        allTokens.stream().filter(t -> t.assignedSlotId == slotId && (t.status == TokenStatus.CREATED || t.status == TokenStatus.RESCHEDULED))
                .forEach(t -> System.out.println("  " + t));
    }

    static void printVisitedHistory(Long slotId) {
        System.out.println("\n--- Visited History ---");
        List<Token> visited = allTokens.stream()
                .filter(t -> t.assignedSlotId == slotId && t.status == TokenStatus.VISITED)
                .sorted((t1, t2) -> t1.visitedAt.compareTo(t2.visitedAt))
                .collect(Collectors.toList());
        
        if (visited.isEmpty()) {
            System.out.println("(No patients have visited yet)");
        } else {
            visited.forEach(t -> System.out.println("  " + t));
        }
    }
}
