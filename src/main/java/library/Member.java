package library;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a library member.
 * Tracks personal details, borrowed books, and reservation history.
 */
public class Member implements Serializable {
    private static final long serialVersionUID = 1L;

    // Maximum books a member can borrow simultaneously
    public static final int MAX_BORROW_LIMIT = 5;

    private String id;
    private String name;
    private String email;
    private String phone;
    private List<String> borrowedIsbns;    // Currently borrowed
    private List<String> reservedIsbns;    // Currently reserved
    private double totalFinesPaid;

    // ─────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────

    public Member(String id, String name, String email, String phone) {
        this.id             = id;
        this.name           = name;
        this.email          = email;
        this.phone          = phone;
        this.borrowedIsbns  = new ArrayList<>();
        this.reservedIsbns  = new ArrayList<>();
        this.totalFinesPaid = 0.0;
    }

    // Convenience constructor without contact details
    public Member(String id, String name) {
        this(id, name, "N/A", "N/A");
    }

    // ─────────────────────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────────────────────

    public String getId()                     { return id; }
    public String getName()                   { return name; }
    public String getEmail()                  { return email; }
    public String getPhone()                  { return phone; }
    public List<String> getBorrowedIsbns()    { return Collections.unmodifiableList(borrowedIsbns); }
    public List<String> getReservedIsbns()    { return Collections.unmodifiableList(reservedIsbns); }
    public double getTotalFinesPaid()         { return totalFinesPaid; }
    public int getBorrowCount()               { return borrowedIsbns.size(); }

    // ─────────────────────────────────────────────────────────
    // Setters
    // ─────────────────────────────────────────────────────────

    public void setName(String name)   { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    // ─────────────────────────────────────────────────────────
    // Business Logic
    // ─────────────────────────────────────────────────────────

    /** Returns true if the member has not reached the borrow limit. */
    public boolean canBorrow() {
        return borrowedIsbns.size() < MAX_BORROW_LIMIT;
    }

    /** Adds an ISBN to this member's borrowed list. */
    public void borrowBook(String isbn) {
        if (!borrowedIsbns.contains(isbn)) {
            borrowedIsbns.add(isbn);
        }
    }

    /** Removes an ISBN from the borrowed list (on return). */
    public boolean returnBook(String isbn) {
        return borrowedIsbns.remove(isbn);
    }

    /** Adds a reservation for this member. */
    public void reserveBook(String isbn) {
        if (!reservedIsbns.contains(isbn)) {
            reservedIsbns.add(isbn);
        }
    }

    /** Cancels a reservation for this member. */
    public boolean cancelReservation(String isbn) {
        return reservedIsbns.remove(isbn);
    }

    /** Records a fine payment. */
    public void payFine(double amount) {
        if (amount > 0) totalFinesPaid += amount;
    }

    /** Checks whether this member has currently borrowed a specific ISBN. */
    public boolean hasBorrowed(String isbn) {
        return borrowedIsbns.contains(isbn);
    }

    // ─────────────────────────────────────────────────────────
    // Serialization helpers (pipe-delimited for members.txt)
    // ─────────────────────────────────────────────────────────

    /**
     * Converts the member to a pipe-delimited line for file storage.
     * ISBN lists are comma-separated within their field.
     */
    public String toFileString() {
        String borrowed  = borrowedIsbns.isEmpty()  ? "NONE" : String.join(",", borrowedIsbns);
        String reserved  = reservedIsbns.isEmpty()  ? "NONE" : String.join(",", reservedIsbns);
        return String.join("|",
            id, name, email, phone,
            borrowed, reserved,
            String.valueOf(totalFinesPaid)
        );
    }

    /**
     * Parses a pipe-delimited line from members.txt and returns a Member object.
     */
    public static Member fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) throw new IllegalArgumentException("Invalid member record: " + line);

        Member m = new Member(
            parts[0].trim(),
            parts[1].trim(),
            parts[2].trim(),
            parts[3].trim()
        );

        if (!"NONE".equals(parts[4].trim())) {
            for (String isbn : parts[4].trim().split(",")) {
                m.borrowedIsbns.add(isbn.trim());
            }
        }
        if (!"NONE".equals(parts[5].trim())) {
            for (String isbn : parts[5].trim().split(",")) {
                m.reservedIsbns.add(isbn.trim());
            }
        }
        m.totalFinesPaid = Double.parseDouble(parts[6].trim());
        return m;
    }

    // ─────────────────────────────────────────────────────────
    // Display
    // ─────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("ID: %-10s | Name: %-25s | Email: %-30s | Phone: %-15s | Books borrowed: %d/%d",
            id, name, email, phone, borrowedIsbns.size(), MAX_BORROW_LIMIT);
    }

    /** Full details with borrowed/reserved lists. */
    public String toDetailString() {
        StringBuilder sb = new StringBuilder();
        sb.append("─────────────────────────────────────────────────\n");
        sb.append(String.format("  Member ID    : %s%n", id));
        sb.append(String.format("  Name         : %s%n", name));
        sb.append(String.format("  Email        : %s%n", email));
        sb.append(String.format("  Phone        : %s%n", phone));
        sb.append(String.format("  Books Held   : %d / %d%n", borrowedIsbns.size(), MAX_BORROW_LIMIT));
        sb.append(String.format("  Total Fines  : $%.2f%n", totalFinesPaid));

        if (!borrowedIsbns.isEmpty()) {
            sb.append("  Borrowed ISBNs: ").append(String.join(", ", borrowedIsbns)).append("\n");
        }
        if (!reservedIsbns.isEmpty()) {
            sb.append("  Reserved ISBNs: ").append(String.join(", ", reservedIsbns)).append("\n");
        }
        sb.append("─────────────────────────────────────────────────");
        return sb.toString();
    }
}
