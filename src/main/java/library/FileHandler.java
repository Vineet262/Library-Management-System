package library;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all file I/O operations for the library system.
 * Data is stored in human-readable pipe-delimited text files under the /data directory.
 */
public class FileHandler {

    private static final String DATA_DIR       = "data";
    private static final String BOOKS_FILE     = DATA_DIR + File.separator + "books.txt";
    private static final String MEMBERS_FILE   = DATA_DIR + File.separator + "members.txt";
    private static final String BOOKS_CSV      = DATA_DIR + File.separator + "books_export.csv";
    private static final String MEMBERS_CSV    = DATA_DIR + File.separator + "members_export.csv";

    // ─────────────────────────────────────────────────────────
    // Initialisation
    // ─────────────────────────────────────────────────────────

    public FileHandler() {
        ensureDataDirectory();
    }

    /** Creates the /data directory if it does not already exist. */
    private void ensureDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("[FileHandler] Created data directory: " + dir.getAbsolutePath());
            } else {
                System.err.println("[FileHandler] WARNING: Could not create data directory.");
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Books
    // ─────────────────────────────────────────────────────────

    /**
     * Saves the entire list of books to books.txt.
     * Each book occupies one pipe-delimited line.
     */
    public void saveBooks(List<Book> books) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(BOOKS_FILE), StandardCharsets.UTF_8))) {
            for (Book book : books) {
                writer.write(book.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] ERROR saving books: " + e.getMessage());
        }
    }

    /**
     * Loads books from books.txt. Returns an empty list if the file does not exist
     * or if any individual line is corrupt (bad lines are skipped with a warning).
     */
    public List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        File file = new File(BOOKS_FILE);
        if (!file.exists()) return books;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    books.add(Book.fromFileString(line));
                } catch (Exception e) {
                    System.err.printf("[FileHandler] WARNING: Skipping corrupt book record on line %d: %s%n",
                        lineNumber, e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] ERROR loading books: " + e.getMessage());
        }
        return books;
    }

    // ─────────────────────────────────────────────────────────
    // Members
    // ─────────────────────────────────────────────────────────

    /**
     * Saves the entire list of members to members.txt.
     */
    public void saveMembers(List<Member> members) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(MEMBERS_FILE), StandardCharsets.UTF_8))) {
            for (Member member : members) {
                writer.write(member.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] ERROR saving members: " + e.getMessage());
        }
    }

    /**
     * Loads members from members.txt.
     */
    public List<Member> loadMembers() {
        List<Member> members = new ArrayList<>();
        File file = new File(MEMBERS_FILE);
        if (!file.exists()) return members;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    members.add(Member.fromFileString(line));
                } catch (Exception e) {
                    System.err.printf("[FileHandler] WARNING: Skipping corrupt member record on line %d: %s%n",
                        lineNumber, e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] ERROR loading members: " + e.getMessage());
        }
        return members;
    }

    // ─────────────────────────────────────────────────────────
    // CSV Export
    // ─────────────────────────────────────────────────────────

    /**
     * Exports the books list to a CSV file (books_export.csv).
     */
    public void exportBooksToCSV(List<Book> books) {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(BOOKS_CSV), StandardCharsets.UTF_8))) {
            pw.println("ISBN,Title,Author,Year,Genre,Available,BorrowedBy,DueDate,ReservedBy");
            for (Book b : books) {
                pw.printf("\"%s\",\"%s\",\"%s\",%d,\"%s\",%b,\"%s\",\"%s\",\"%s\"%n",
                    b.getIsbn(),
                    escapeCsv(b.getTitle()),
                    escapeCsv(b.getAuthor()),
                    b.getYear(),
                    escapeCsv(b.getGenre()),
                    b.isAvailable(),
                    b.getBorrowedBy()  == null ? "" : b.getBorrowedBy(),
                    b.getDueDate()     == null ? "" : b.getDueDate().toString(),
                    b.getReservedBy()  == null ? "" : b.getReservedBy()
                );
            }
            System.out.println("[FileHandler] Books exported to: " + new File(BOOKS_CSV).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[FileHandler] ERROR exporting books to CSV: " + e.getMessage());
        }
    }

    /**
     * Exports the members list to a CSV file (members_export.csv).
     */
    public void exportMembersToCSV(List<Member> members) {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(MEMBERS_CSV), StandardCharsets.UTF_8))) {
            pw.println("ID,Name,Email,Phone,BorrowedISBNs,ReservedISBNs,TotalFinesPaid");
            for (Member m : members) {
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f%n",
                    m.getId(),
                    escapeCsv(m.getName()),
                    escapeCsv(m.getEmail()),
                    escapeCsv(m.getPhone()),
                    String.join(";", m.getBorrowedIsbns()),
                    String.join(";", m.getReservedIsbns()),
                    m.getTotalFinesPaid()
                );
            }
            System.out.println("[FileHandler] Members exported to: " + new File(MEMBERS_CSV).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[FileHandler] ERROR exporting members to CSV: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────

    /** Escapes double-quotes inside CSV field values. */
    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    /** Returns the absolute path of the data directory (useful for UI display). */
    public String getDataDirectoryPath() {
        return new File(DATA_DIR).getAbsolutePath();
    }
}
