import java.sql.*;
import java.util.Scanner;

public class LibraryManagement {
    // JDBC driver and database URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/library_management?useSSL=false&serverTimezone=UTC";

    // Database credentials
    static final String USER = "root";
    static final String PASS = "root@123";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);

            // Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Execute SQL queries
            stmt = conn.createStatement();

            while (true) {
                displayMenu();
                Scanner scanner = new Scanner(System.in);
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        addBook(stmt);
                        break;
                    case 2:
                        removeBook(stmt);
                        break;
                    case 3:
                        borrowBook(stmt);
                        break;
                    case 4:
                        returnBook(stmt);
                        break;
                    case 5:
                        displayAvailableBooks(stmt);
                        break;
                    case 6:
                        displayBorrowedBooks(stmt);
                        break;
                    case 7:
                        System.out.println("Exiting...");
                        stmt.close();
                        conn.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Please enter a valid option.");
                        break;
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void displayMenu() {
        System.out.println("\nLibrary Management System");
        System.out.println("-------------------------");
        System.out.println("1. Add Book");
        System.out.println("2. Remove Book");
        System.out.println("3. Borrow Book");
        System.out.println("4. Return Book");
        System.out.println("5. Display Available Books");
        System.out.println("6. Display Borrowed Books");
        System.out.println("7. Exit");
        System.out.print("Enter your choice: ");
    }

    public static void addBook(Statement stmt) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter title: ");
            String title = scanner.nextLine();
            System.out.print("Enter author: ");
            String author = scanner.nextLine();
            System.out.print("Enter ISBN: ");
            String isbn = scanner.nextLine();

            String sql = "INSERT INTO books (title, author, isbn) VALUES ('" + title + "', '" + author + "', '" + isbn + "')";
            stmt.executeUpdate(sql);
            System.out.println("Book added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeBook(Statement stmt) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter book ID to remove: ");
            int bookId = scanner.nextInt();

            // Check if the book is currently borrowed
            String checkBorrowedSql = "SELECT * FROM loans WHERE book_id = " + bookId + " AND return_date IS NULL";
            ResultSet borrowedRs = stmt.executeQuery(checkBorrowedSql);
            if (borrowedRs.next()) {
                System.out.println("Cannot remove the book as it is currently borrowed.");
                return;
            }

            // Delete the book
            String sql = "DELETE FROM books WHERE book_id = " + bookId;
            stmt.executeUpdate(sql);
            System.out.println("Book removed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void borrowBook(Statement stmt) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter book ID to borrow: ");
            int bookId = scanner.nextInt();
            System.out.print("Enter member ID: ");
            int memberId = scanner.nextInt();

            // Check if member exists
            String checkMemberSql = "SELECT * FROM members WHERE member_id = " + memberId;
            ResultSet memberRs = stmt.executeQuery(checkMemberSql);
            if (!memberRs.next()) {
                System.out.println("Member with ID " + memberId + " does not exist.");
                return;
            }

            // Insert loan
            String sql = "INSERT INTO loans (book_id, member_id, loan_date) VALUES (" + bookId + ", " + memberId + ", CURDATE())";
            stmt.executeUpdate(sql);

            // Update book availability
            sql = "UPDATE books SET available = FALSE WHERE book_id = " + bookId;
            stmt.executeUpdate(sql);

            System.out.println("Book borrowed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void returnBook(Statement stmt) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter book ID to return: ");
            int bookId = scanner.nextInt();

            String sql = "UPDATE loans SET return_date = CURDATE() WHERE book_id = " + bookId;
            stmt.executeUpdate(sql);

            sql = "UPDATE books SET available = TRUE WHERE book_id = " + bookId;
            stmt.executeUpdate(sql);

            System.out.println("Book returned successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void displayAvailableBooks(Statement stmt) {
        try {
            String sql = "SELECT * FROM books WHERE available = TRUE";
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("Available Books:");
            while (rs.next()) {
                System.out.println("Book ID: " + rs.getInt("book_id") + ", Title: " + rs.getString("title") + ", Author: " + rs.getString("author") + ", ISBN: " + rs.getString("isbn"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void displayBorrowedBooks(Statement stmt) {
        try {
            String sql = "SELECT b.title, m.first_name, m.last_name FROM loans l JOIN books b ON l.book_id = b.book_id JOIN members m ON l.member_id = m.member_id WHERE l.return_date IS NULL";
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("Borrowed Books:");
            while (rs.next()) {
                System.out.println("Title: " + rs.getString("title") + ", Borrowed by: " + rs.getString("first_name") + " " + rs.getString("last_name"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
