package library;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a book in the library system.
 * Implements Serializable for file-based persistence.
 */
public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    // Fine rate: $0.50 per day overdue
    public static final double FINE_PER_DAY = 0.50;

    private String isbn;
    private String title;
    private String author;
    private int year;
    private String genre;
    private boolean available;
    private String borrowedBy;   // Member ID
    private LocalDate dueDate;
    private String reservedBy;   // Member ID (reservation system)

    // ─────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────

    public Book(String isbn, String title, String author, int year, String genre) {
        this.isbn      = isbn;
        this.title     = title;
        this.author    = author;
        this.year      = year;
        this.genre     = genre;
        this.available = true;
        this.borrowedBy = null;
        this.dueDate    = null;
        this.reservedBy = null;
    }

    // Convenience constructor without genre
    public Book(String isbn, String title, String author, int year) {
        this(isbn, title, author, year, "General");
    }

    // ─────────────────────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────────────────────

    public String getIsbn()        { return isbn; }
    public String getTitle()       { return title; }
    public String getAuthor()      { return author; }
    public int    getYear()        { return year; }
    public String getGenre()       { return genre; }
    public boolean isAvailable()   { return available; }
    public String getBorrowedBy()  { return borrowedBy; }
    public LocalDate getDueDate()  { return dueDate; }
    public String getReservedBy()  { return reservedBy; }

    // ─────────────────────────────────────────────────────────
    // Setters
    // ─────────────────────────────────────────────────────────

    public void setTitle(String title)         { this.title = title; }
    public void setAuthor(String author)       { this.author = author; }
    public void setYear(int year)              { this.year = year; }
    public void setGenre(String genre)         { this.genre = genre; }
    public void setAvailable(boolean available){ this.available = available; }
    public void setBorrowedBy(String memberId) { this.borrowedBy = memberId; }
    public void setDueDate(LocalDate dueDate)  { this.dueDate = dueDate; }
    public void setReservedBy(String memberId) { this.reservedBy = memberId; }

    // ─────────────────────────────────────────────────────────
    // Business Logic
    // ─────────────────────────────────────────────────────────

    /**
     * Checks whether this book is currently overdue.
     */
    public boolean isOverdue() {
        if (dueDate == null || available) return false;
        return LocalDate.now().isAfter(dueDate);
    }

    /**
     * Calculates the overdue fine in dollars.
     */
    public double calculateFine() {
        if (!isOverdue()) return 0.0;
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return daysOverdue * FINE_PER_DAY;
    }

    /**
     * Returns number of days until due (negative if overdue).
     */
    public long daysUntilDue() {
        if (dueDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    /**
     * Checks if this book is reserved by someone else.
     */
    public boolean isReserved() {
        return reservedBy != null && !reservedBy.isEmpty();
    }

    // ─────────────────────────────────────────────────────────
    // Serialization helpers (pipe-delimited for books.txt)
    // ─────────────────────────────────────────────────────────

    /**
     * Converts the book to a pipe-delimited string for file storage.
     */
    public String toFileString() {
        return String.join("|",
            isbn,
            title,
            author,
            String.valueOf(year),
            genre,
            String.valueOf(available),
            borrowedBy  == null ? "NULL" : borrowedBy,
            dueDate     == null ? "NULL" : dueDate.toString(),
            reservedBy  == null ? "NULL" : reservedBy
        );
    }

    /**
     * Parses a pipe-delimited line from books.txt and returns a Book object.
     */
    public static Book fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 9) throw new IllegalArgumentException("Invalid book record: " + line);

        Book book = new Book(
            parts[0].trim(),
            parts[1].trim(),
            parts[2].trim(),
            Integer.parseInt(parts[3].trim()),
            parts[4].trim()
        );
        book.available  = Boolean.parseBoolean(parts[5].trim());
        book.borrowedBy = "NULL".equals(parts[6].trim()) ? null : parts[6].trim();
        book.dueDate    = "NULL".equals(parts[7].trim()) ? null : LocalDate.parse(parts[7].trim());
        book.reservedBy = "NULL".equals(parts[8].trim()) ? null : parts[8].trim();
        return book;
    }

    // ─────────────────────────────────────────────────────────
    // Display
    // ─────────────────────────────────────────────────────────

    @Override
    public String toString() {
        String status;
        if (available) {
            status = isReserved() ? "[RESERVED] by: " + reservedBy : "[AVAILABLE]";
        } else {
            status = "[BORROWED] by: " + borrowedBy
                   + " | Due: " + dueDate
                   + (isOverdue() ? " *** OVERDUE ($" + String.format("%.2f", calculateFine()) + ")" : "");
        }
        return String.format("ISBN: %-15s | %-40s | %-20s | %d | %s",
            isbn, title, author, year, status);
    }

    /**
     * Returns a compact one-line summary.
     */
    public String toSummaryString() {
        return String.format("[%s] %s by %s (%d) — %s",
            isbn, title, author, year,
            available ? "Available" : "Borrowed");
    }
}
