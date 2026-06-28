package library;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core library engine -- manages all books, members, and business operations.
 * Acts as the service layer between the UI (Main) and data (FileHandler).
 */
public class Library {

    private List<Book>   books;
    private List<Member> members;
    private final FileHandler fileHandler;

    // Loan period in weeks
    private static final int LOAN_PERIOD_WEEKS = 2;

    // ─────────────────────────────────────────────────────────
    // Initialisation
    // ─────────────────────────────────────────────────────────

    public Library() {
        this.fileHandler = new FileHandler();
        loadData();
    }

    private void loadData() {
        books   = fileHandler.loadBooks();
        members = fileHandler.loadMembers();
        System.out.printf("[Library] Loaded %d book(s) and %d member(s) from disk.%n",
            books.size(), members.size());
    }

    // ─────────────────────────────────────────────────────────
    // Book Management
    // ─────────────────────────────────────────────────────────

    /**
     * Adds a new book to the library. Rejects duplicates by ISBN.
     * @return true if added, false if ISBN already exists.
     */
    public boolean addBook(Book book) {
        if (findBookByIsbn(book.getIsbn()) != null) {
            System.out.println("  ERROR: A book with ISBN " + book.getIsbn() + " already exists.");
            return false;
        }
        books.add(book);
        fileHandler.saveBooks(books);
        System.out.println("  SUCCESS: Book added: '" + book.getTitle() + "'");
        return true;
    }

    /**
     * Removes a book by ISBN. Refuses removal if the book is currently borrowed.
     * @return true if removed, false otherwise.
     */
    public boolean removeBook(String isbn) {
        Book book = findBookByIsbn(isbn);
        if (book == null) {
            System.out.println("  ERROR: No book found with ISBN: " + isbn);
            return false;
        }
        if (!book.isAvailable()) {
            System.out.println("  ERROR: Cannot remove a book that is currently borrowed (by: "
                + book.getBorrowedBy() + ").");
            return false;
        }
        books.remove(book);
        fileHandler.saveBooks(books);
        System.out.println("  SUCCESS: Book removed: '" + book.getTitle() + "'");
        return true;
    }

    /**
     * Finds a book by its exact ISBN (case-insensitive).
     */
    public Book findBookByIsbn(String isbn) {
        return books.stream()
            .filter(b -> b.getIsbn().equalsIgnoreCase(isbn.trim()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Searches books by keyword matching title, author, or genre (case-insensitive).
     */
    public List<Book> searchBooks(String keyword) {
        String kw = keyword.toLowerCase().trim();
        return books.stream()
            .filter(b -> b.getTitle().toLowerCase().contains(kw)
                      || b.getAuthor().toLowerCase().contains(kw)
                      || b.getGenre().toLowerCase().contains(kw))
            .collect(Collectors.toList());
    }

    /** Returns only books that are currently available. */
    public List<Book> getAvailableBooks() {
        return books.stream().filter(Book::isAvailable).collect(Collectors.toList());
    }

    /** Returns only books that are currently borrowed. */
    public List<Book> getBorrowedBooks() {
        return books.stream().filter(b -> !b.isAvailable()).collect(Collectors.toList());
    }

    /** Returns overdue books sorted by most overdue first. */
    public List<Book> getOverdueBooks() {
        return books.stream()
            .filter(Book::isOverdue)
            .sorted(Comparator.comparing(Book::getDueDate))
            .collect(Collectors.toList());
    }

    /** Sorted list of all books by title A to Z. */
    public List<Book> getAllBooksSortedByTitle() {
        return books.stream()
            .sorted(Comparator.comparing(b -> b.getTitle().toLowerCase()))
            .collect(Collectors.toList());
    }

    public List<Book> getAllBooks() { return books; }

    // ─────────────────────────────────────────────────────────
    // Member Management
    // ─────────────────────────────────────────────────────────

    /**
     * Registers a new member. Rejects duplicate IDs.
     * @return true if registered, false if ID already exists.
     */
    public boolean registerMember(Member member) {
        if (findMemberById(member.getId()) != null) {
            System.out.println("  ERROR: A member with ID " + member.getId() + " already exists.");
            return false;
        }
        members.add(member);
        fileHandler.saveMembers(members);
        System.out.println("  SUCCESS: Member registered: " + member.getName()
            + " [" + member.getId() + "]");
        return true;
    }

    /**
     * Removes a member by ID. Refuses if the member has unreturned books.
     * @return true if removed, false otherwise.
     */
    public boolean removeMember(String id) {
        Member member = findMemberById(id);
        if (member == null) {
            System.out.println("  ERROR: No member found with ID: " + id);
            return false;
        }
        if (member.getBorrowCount() > 0) {
            System.out.println("  ERROR: Cannot remove member who has "
                + member.getBorrowCount() + " unreturned book(s).");
            return false;
        }
        members.remove(member);
        fileHandler.saveMembers(members);
        System.out.println("  SUCCESS: Member removed: " + member.getName());
        return true;
    }

    /** Finds a member by their ID (case-insensitive). */
    public Member findMemberById(String id) {
        return members.stream()
            .filter(m -> m.getId().equalsIgnoreCase(id.trim()))
            .findFirst()
            .orElse(null);
    }

    /** Searches members by keyword matching name or email (case-insensitive). */
    public List<Member> searchMembers(String keyword) {
        String kw = keyword.toLowerCase().trim();
        return members.stream()
            .filter(m -> m.getName().toLowerCase().contains(kw)
                      || m.getEmail().toLowerCase().contains(kw))
            .collect(Collectors.toList());
    }

    public List<Member> getAllMembers() { return members; }

    // ─────────────────────────────────────────────────────────
    // Borrowing Operations
    // ─────────────────────────────────────────────────────────

    /**
     * Loans a book to a member.
     * Validates: book exists, member exists, book available, member under borrow limit.
     * @return true on success.
     */
    public boolean borrowBook(String isbn, String memberId) {
        Book   book   = findBookByIsbn(isbn);
        Member member = findMemberById(memberId);

        if (book == null) {
            System.out.println("  ERROR: Book not found (ISBN: " + isbn + ").");
            return false;
        }
        if (member == null) {
            System.out.println("  ERROR: Member not found (ID: " + memberId + ").");
            return false;
        }
        if (!book.isAvailable()) {
            System.out.println("  ERROR: Book is already borrowed by member "
                + book.getBorrowedBy() + ".");
            return false;
        }
        if (!member.canBorrow()) {
            System.out.printf("  ERROR: %s has reached the borrow limit of %d book(s).%n",
                member.getName(), Member.MAX_BORROW_LIMIT);
            return false;
        }
        if (book.isReserved() && !book.getReservedBy().equalsIgnoreCase(memberId)) {
            System.out.println("  ERROR: Book is reserved by member " + book.getReservedBy()
                + " and cannot be borrowed by others.");
            return false;
        }

        LocalDate dueDate = LocalDate.now().plusWeeks(LOAN_PERIOD_WEEKS);
        book.setAvailable(false);
        book.setBorrowedBy(memberId);
        book.setDueDate(dueDate);
        book.setReservedBy(null);

        member.borrowBook(isbn);
        member.cancelReservation(isbn);

        fileHandler.saveBooks(books);
        fileHandler.saveMembers(members);

        System.out.println("  SUCCESS: '" + book.getTitle() + "' borrowed by " + member.getName() + ".");
        System.out.println("  Due date: " + dueDate + " (" + LOAN_PERIOD_WEEKS + "-week loan period)");
        return true;
    }

    /**
     * Returns a borrowed book. Calculates and displays any overdue fine.
     * @return fine amount owed (0 if not overdue), or -1 on error.
     */
    public double returnBook(String isbn, String memberId) {
        Book   book   = findBookByIsbn(isbn);
        Member member = findMemberById(memberId);

        if (book == null) {
            System.out.println("  ERROR: Book not found (ISBN: " + isbn + ").");
            return -1;
        }
        if (member == null) {
            System.out.println("  ERROR: Member not found (ID: " + memberId + ").");
            return -1;
        }
        if (book.isAvailable()) {
            System.out.println("  ERROR: This book is not currently borrowed.");
            return -1;
        }
        if (!book.getBorrowedBy().equalsIgnoreCase(memberId)) {
            System.out.println("  ERROR: This book was borrowed by member "
                + book.getBorrowedBy() + ", not " + memberId + ".");
            return -1;
        }

        double fine = book.calculateFine();

        book.setAvailable(true);
        book.setBorrowedBy(null);
        book.setDueDate(null);

        member.returnBook(isbn);
        if (fine > 0) member.payFine(fine);

        fileHandler.saveBooks(books);
        fileHandler.saveMembers(members);

        System.out.println("  SUCCESS: '" + book.getTitle() + "' returned by " + member.getName() + ".");
        if (fine > 0) {
            System.out.printf("  WARNING: Overdue fine: $%.2f -- please collect payment.%n", fine);
        } else {
            System.out.println("  No fine -- returned on time. Thank you!");
        }
        return fine;
    }

    // ─────────────────────────────────────────────────────────
    // Reservation System
    // ─────────────────────────────────────────────────────────

    /** Allows a member to reserve a book that is currently borrowed. */
    public boolean reserveBook(String isbn, String memberId) {
        Book   book   = findBookByIsbn(isbn);
        Member member = findMemberById(memberId);

        if (book == null)   { System.out.println("  ERROR: Book not found."); return false; }
        if (member == null) { System.out.println("  ERROR: Member not found."); return false; }

        if (book.isAvailable()) {
            System.out.println("  INFO: Book is already available -- borrow it directly.");
            return false;
        }
        if (book.isReserved()) {
            System.out.println("  ERROR: Book is already reserved by member "
                + book.getReservedBy() + ".");
            return false;
        }
        if (book.getBorrowedBy().equalsIgnoreCase(memberId)) {
            System.out.println("  ERROR: You are currently borrowing this book.");
            return false;
        }

        book.setReservedBy(memberId);
        member.reserveBook(isbn);
        fileHandler.saveBooks(books);
        fileHandler.saveMembers(members);

        System.out.println("  SUCCESS: '" + book.getTitle()
            + "' reserved for " + member.getName() + ".");
        System.out.println("  You may borrow it once the current borrower returns it.");
        return true;
    }

    /** Cancels an existing reservation. */
    public boolean cancelReservation(String isbn, String memberId) {
        Book   book   = findBookByIsbn(isbn);
        Member member = findMemberById(memberId);

        if (book == null || member == null) {
            System.out.println("  ERROR: Book or member not found.");
            return false;
        }
        if (!memberId.equalsIgnoreCase(book.getReservedBy())) {
            System.out.println("  ERROR: No reservation found for this member on this book.");
            return false;
        }

        book.setReservedBy(null);
        member.cancelReservation(isbn);
        fileHandler.saveBooks(books);
        fileHandler.saveMembers(members);

        System.out.println("  SUCCESS: Reservation cancelled for '" + book.getTitle() + "'.");
        return true;
    }

    // ─────────────────────────────────────────────────────────
    // Statistics & Display
    // ─────────────────────────────────────────────────────────

    public void displayAllBooks() {
        List<Book> sorted = getAllBooksSortedByTitle();
        if (sorted.isEmpty()) { System.out.println("  No books in the library."); return; }
        printDivider("ALL BOOKS (" + sorted.size() + " total)");
        for (int i = 0; i < sorted.size(); i++) {
            System.out.printf("  %3d. %s%n", i + 1, sorted.get(i));
        }
        printLine();
    }

    public void displayAvailableBooks() {
        List<Book> avail = getAvailableBooks();
        printDivider("AVAILABLE BOOKS (" + avail.size() + ")");
        if (avail.isEmpty()) System.out.println("  No books are currently available.");
        else avail.forEach(b -> System.out.println("    " + b));
        printLine();
    }

    public void displayBorrowedBooks() {
        List<Book> borrowed = getBorrowedBooks();
        printDivider("BORROWED BOOKS (" + borrowed.size() + ")");
        if (borrowed.isEmpty()) System.out.println("  No books are currently borrowed.");
        else borrowed.forEach(b -> System.out.println("    " + b));
        printLine();
    }

    public void displayOverdueBooks() {
        List<Book> overdue = getOverdueBooks();
        printDivider("OVERDUE BOOKS (" + overdue.size() + ")");
        if (overdue.isEmpty()) System.out.println("  No books are currently overdue. Great job!");
        else overdue.forEach(b -> System.out.println("    " + b));
        printLine();
    }

    public void displayAllMembers() {
        if (members.isEmpty()) { System.out.println("  No members registered."); return; }
        printDivider("ALL MEMBERS (" + members.size() + " registered)");
        for (int i = 0; i < members.size(); i++) {
            System.out.printf("  %3d. %s%n", i + 1, members.get(i));
        }
        printLine();
    }

    public void displayMemberDetails(String memberId) {
        Member m = findMemberById(memberId);
        if (m == null) { System.out.println("  Member not found: " + memberId); return; }
        System.out.println(m.toDetailString());
        if (!m.getBorrowedIsbns().isEmpty()) {
            System.out.println("  BORROWED BOOK DETAILS:");
            for (String isbn : m.getBorrowedIsbns()) {
                Book b = findBookByIsbn(isbn);
                if (b != null) {
                    System.out.println("    > " + b.toSummaryString()
                        + " | Due: " + b.getDueDate()
                        + (b.isOverdue() ? " [OVERDUE]" : ""));
                }
            }
        }
    }

    public void displayStatistics() {
        long available = books.stream().filter(Book::isAvailable).count();
        long borrowed  = books.size() - available;
        long overdue   = books.stream().filter(Book::isOverdue).count();
        double outstanding = books.stream()
            .filter(Book::isOverdue).mapToDouble(Book::calculateFine).sum();
        double collected = members.stream()
            .mapToDouble(Member::getTotalFinesPaid).sum();

        printDivider("LIBRARY STATISTICS");
        System.out.printf("  %-30s %d%n",    "Total Books:",            books.size());
        System.out.printf("  %-30s %d%n",    "Available Books:",        available);
        System.out.printf("  %-30s %d%n",    "Borrowed Books:",         borrowed);
        System.out.printf("  %-30s %d%n",    "Overdue Books:",          overdue);
        System.out.printf("  %-30s $%.2f%n", "Outstanding Fines:",      outstanding);
        System.out.printf("  %-30s %d%n",    "Registered Members:",     members.size());
        System.out.printf("  %-30s $%.2f%n", "Total Fines Collected:",  collected);
        printLine();
    }

    // ─────────────────────────────────────────────────────────
    // CSV Export (delegates to FileHandler)
    // ─────────────────────────────────────────────────────────

    public void exportToCSV() {
        fileHandler.exportBooksToCSV(books);
        fileHandler.exportMembersToCSV(members);
    }

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────

    private static void printDivider(String title) {
        String line = "=".repeat(80);
        System.out.println("\n  " + line);
        System.out.println("   " + title);
        System.out.println("  " + line);
    }

    private static void printLine() {
        System.out.println("  " + "-".repeat(80));
    }
}
