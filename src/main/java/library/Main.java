package library;

import java.util.List;
import java.util.Scanner;

/**
 * Entry point -- console menu UI for the Library Management System.
 * Reads user input, validates it, and delegates to the Library service layer.
 */
public class Main {

    private static final Library library = new Library();
    private static final Scanner scanner  = new Scanner(System.in);

    // ─────────────────────────────────────────────────────────
    // Entry Point
    // ─────────────────────────────────────────────────────────

    public static void main(String[] args) {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("  Enter your choice: ", 1, 9);
            System.out.println();
            switch (choice) {
                case 1 -> bookMenu();
                case 2 -> memberMenu();
                case 3 -> borrowMenu();
                case 4 -> searchMenu();
                case 5 -> library.displayStatistics();
                case 6 -> exportMenu();
                case 7 -> reservationMenu();
                case 8 -> overdueMenu();
                case 9 -> {
                    System.out.println("  Thank you for using the Library Management System. Goodbye!");
                    running = false;
                }
            }
        }
        scanner.close();
    }

    // ─────────────────────────────────────────────────────────
    // Main Menu
    // ─────────────────────────────────────────────────────────

    private static void printMainMenu() {
        System.out.println();
        System.out.println("  +------------------------------------------+");
        System.out.println("  |      LIBRARY MANAGEMENT SYSTEM           |");
        System.out.println("  +------------------------------------------+");
        System.out.println("  |  1.  Book Management                     |");
        System.out.println("  |  2.  Member Management                   |");
        System.out.println("  |  3.  Borrow / Return                     |");
        System.out.println("  |  4.  Search                              |");
        System.out.println("  |  5.  Library Statistics                  |");
        System.out.println("  |  6.  Export to CSV                       |");
        System.out.println("  |  7.  Reservations                        |");
        System.out.println("  |  8.  Overdue Report                      |");
        System.out.println("  |  9.  Exit                                |");
        System.out.println("  +------------------------------------------+");
    }

    // ─────────────────────────────────────────────────────────
    // Book Sub-Menu
    // ─────────────────────────────────────────────────────────

    private static void bookMenu() {
        System.out.println("  -- BOOK MANAGEMENT ----------------------------------");
        System.out.println("  1. Add New Book");
        System.out.println("  2. Remove Book");
        System.out.println("  3. View All Books");
        System.out.println("  4. View Available Books");
        System.out.println("  5. View Borrowed Books");
        System.out.println("  6. Back");

        int choice = readInt("  Choice: ", 1, 6);
        System.out.println();
        switch (choice) {
            case 1 -> addBook();
            case 2 -> removeBook();
            case 3 -> library.displayAllBooks();
            case 4 -> library.displayAvailableBooks();
            case 5 -> library.displayBorrowedBooks();
            case 6 -> { /* back */ }
        }
    }

    private static void addBook() {
        System.out.println("  -- ADD NEW BOOK -------------------------------------");
        String isbn   = readNonEmpty("  ISBN       : ");
        String title  = readNonEmpty("  Title      : ");
        String author = readNonEmpty("  Author     : ");
        int    year   = readInt("  Year       : ", 1000, 2100);
        String genre  = readNonEmpty("  Genre      : ");

        library.addBook(new Book(isbn, title, author, year, genre));
    }

    private static void removeBook() {
        System.out.println("  -- REMOVE BOOK --------------------------------------");
        String isbn = readNonEmpty("  ISBN to remove: ");
        library.removeBook(isbn);
    }

    // ─────────────────────────────────────────────────────────
    // Member Sub-Menu
    // ─────────────────────────────────────────────────────────

    private static void memberMenu() {
        System.out.println("  -- MEMBER MANAGEMENT --------------------------------");
        System.out.println("  1. Register New Member");
        System.out.println("  2. Remove Member");
        System.out.println("  3. View All Members");
        System.out.println("  4. View Member Details");
        System.out.println("  5. Back");

        int choice = readInt("  Choice: ", 1, 5);
        System.out.println();
        switch (choice) {
            case 1 -> registerMember();
            case 2 -> removeMember();
            case 3 -> library.displayAllMembers();
            case 4 -> {
                String id = readNonEmpty("  Member ID: ");
                library.displayMemberDetails(id);
            }
            case 5 -> { /* back */ }
        }
    }

    private static void registerMember() {
        System.out.println("  -- REGISTER MEMBER ----------------------------------");
        String id    = readNonEmpty("  Member ID  : ");
        String name  = readNonEmpty("  Full Name  : ");
        String email = readNonEmpty("  Email      : ");
        String phone = readNonEmpty("  Phone      : ");
        library.registerMember(new Member(id, name, email, phone));
    }

    private static void removeMember() {
        System.out.println("  -- REMOVE MEMBER ------------------------------------");
        String id = readNonEmpty("  Member ID to remove: ");
        library.removeMember(id);
    }

    // ─────────────────────────────────────────────────────────
    // Borrow / Return Sub-Menu
    // ─────────────────────────────────────────────────────────

    private static void borrowMenu() {
        System.out.println("  -- BORROW / RETURN ----------------------------------");
        System.out.println("  1. Borrow a Book");
        System.out.println("  2. Return a Book");
        System.out.println("  3. Back");

        int choice = readInt("  Choice: ", 1, 3);
        System.out.println();
        switch (choice) {
            case 1 -> {
                System.out.println("  -- BORROW BOOK --------------------------------------");
                String isbn     = readNonEmpty("  ISBN      : ");
                String memberId = readNonEmpty("  Member ID : ");
                library.borrowBook(isbn, memberId);
            }
            case 2 -> {
                System.out.println("  -- RETURN BOOK --------------------------------------");
                String isbn     = readNonEmpty("  ISBN      : ");
                String memberId = readNonEmpty("  Member ID : ");
                double fine = library.returnBook(isbn, memberId);
                if (fine > 0) {
                    System.out.printf("  Please collect $%.2f overdue fine from the member.%n", fine);
                }
            }
            case 3 -> { /* back */ }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Search Sub-Menu
    // ─────────────────────────────────────────────────────────

    private static void searchMenu() {
        System.out.println("  -- SEARCH -------------------------------------------");
        System.out.println("  1. Search Books  (title / author / genre)");
        System.out.println("  2. Search Members (name / email)");
        System.out.println("  3. Look Up Book by ISBN");
        System.out.println("  4. Look Up Member by ID");
        System.out.println("  5. Back");

        int choice = readInt("  Choice: ", 1, 5);
        System.out.println();
        switch (choice) {
            case 1 -> {
                String kw = readNonEmpty("  Keyword: ");
                List<Book> results = library.searchBooks(kw);
                if (results.isEmpty()) {
                    System.out.println("  No books matched '" + kw + "'.");
                } else {
                    System.out.println("  Found " + results.size() + " result(s):");
                    results.forEach(b -> System.out.println("    > " + b));
                }
            }
            case 2 -> {
                String kw = readNonEmpty("  Keyword: ");
                List<Member> results = library.searchMembers(kw);
                if (results.isEmpty()) {
                    System.out.println("  No members matched '" + kw + "'.");
                } else {
                    System.out.println("  Found " + results.size() + " result(s):");
                    results.forEach(m -> System.out.println("    > " + m));
                }
            }
            case 3 -> {
                String isbn = readNonEmpty("  ISBN: ");
                Book b = library.findBookByIsbn(isbn);
                if (b == null) System.out.println("  Not found.");
                else System.out.println("  " + b);
            }
            case 4 -> {
                String id = readNonEmpty("  Member ID: ");
                library.displayMemberDetails(id);
            }
            case 5 -> { /* back */ }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Reservation Sub-Menu
    // ─────────────────────────────────────────────────────────

    private static void reservationMenu() {
        System.out.println("  -- RESERVATIONS -------------------------------------");
        System.out.println("  1. Reserve a Book");
        System.out.println("  2. Cancel a Reservation");
        System.out.println("  3. Back");

        int choice = readInt("  Choice: ", 1, 3);
        System.out.println();
        switch (choice) {
            case 1 -> {
                String isbn     = readNonEmpty("  ISBN      : ");
                String memberId = readNonEmpty("  Member ID : ");
                library.reserveBook(isbn, memberId);
            }
            case 2 -> {
                String isbn     = readNonEmpty("  ISBN      : ");
                String memberId = readNonEmpty("  Member ID : ");
                library.cancelReservation(isbn, memberId);
            }
            case 3 -> { /* back */ }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Overdue & Export
    // ─────────────────────────────────────────────────────────

    private static void overdueMenu()  { library.displayOverdueBooks(); }
    private static void exportMenu()   {
        System.out.println("  Exporting all data to CSV files...");
        library.exportToCSV();
    }

    // ─────────────────────────────────────────────────────────
    // Input Helpers
    // ─────────────────────────────────────────────────────────

    /** Reads a non-empty trimmed string from the user. Re-prompts until valid. */
    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("  [!] Input cannot be empty. Please try again.");
        }
    }

    /** Reads an integer in [min, max]. Re-prompts on invalid input. */
    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.printf("  [!] Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid input. Please enter a number.");
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Banner
    // ─────────────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println();
        System.out.println("  +--------------------------------------------------+");
        System.out.println("  |                                                  |");
        System.out.println("  |       LIBRARY MANAGEMENT SYSTEM                  |");
        System.out.println("  |         Console Edition  v1.0                    |");
        System.out.println("  |                                                  |");
        System.out.println("  |   Manage books, members & borrowing operations   |");
        System.out.println("  |                                                  |");
        System.out.println("  +--------------------------------------------------+");
        System.out.println();
    }
}
