/*

Needed:

Book record
Member class
Library class

Extra
BookLoan record or class

Book array
Book loan array



Methods:

Needed:

Add book to library
Register new member
Load a book - Unable to loan book if loan is past return date
            - Check that book isnt already loaned out
Return a book
Search for book - Selecting a book gives book info and loan book option
                - Search title, author,
Browse all books - Selecting a book gives book info and loan book option
                 - Show available books and loaned out books and who has the loan


Extra:


 */

import java.time.LocalDate;

public class Library {

    static Book[] books = new Book[5];
    static Member[] members  = new Member[5];
    static BookLoan[] bookLoans  = new BookLoan[5];

    static int lastMemberId = 0;

    public static final String MAIN_MENU = """
            Welcome to Bibliotekshanteraren!
            
            1) Add a new book to the library
            2) Register new member
            
            3) Loan a book
            4) Return a book
            
            5) Search
            6) Browse
            
            q) Quit
            """;

    static void main() {
        printMenu();
    }

    private static void printMenu() {
        IO.println(MAIN_MENU);
    }

    private static void addBook() {
        // TODO: Add loop and valid check
        IO.println("Please enter the title: ");
        String bookTitle = IO.readln().trim();

        IO.println("Please enter the author: ");
        String bookAuthor = IO.readln().trim();

        IO.println("Please enter the books ISBN: ");
        String bookISBN = IO.readln().trim();

        Book newBook = new Book(bookISBN, bookTitle, bookAuthor);


        if (!isFull(books)) {
            int emptyIndex = getEmpty(books);
            books[emptyIndex] = newBook;
        } else {
            books = extendBooksAndAdd(books, newBook);

        }
    }



    private static void registerMember() {

        // TODO: Add loop and valid check
        IO.println("Please enter the name of the member: ");
        String memberName = IO.readln().trim();

        lastMemberId++;
        Member  newMember = new Member(memberName, lastMemberId);

        if (!isFull(members)) {
            int emptyIndex = getEmpty(members);
            members[emptyIndex] = newMember;
        } else {
            members = extendMembersAndAdd(members, newMember);

        }
    }

    private static void loanBook(Book bookToLoan, Member member) {
        if (!member.canLoan()) return;
        for (BookLoan bookLoan : bookLoans) {
            if(bookLoan != null && bookLoan.book().isbn().equals(bookToLoan.isbn())) {
                LocalDate returnDate = bookLoan.getReturnDate();
                IO.println("The book is already loaned. Please find another book or return after " + returnDate.toString() + " to check if the book has been returned");
                return;
            }
        }
        BookLoan newLoan = new BookLoan(member, bookToLoan);

        if (!isFull(bookLoans)) {
            int emptyIndex = getEmpty(bookLoans);
            bookLoans[emptyIndex] = newLoan;
        } else {
            bookLoans = extendLoansAndAdd(bookLoans, newLoan);

        }
        member.addLoan(newLoan);
    }

    private static void returnBook() {

    }

    private static void search() {

    }

    private static void browse() {

    }


    // Helper methods

    private static Book[] extendBooksAndAdd(Book[] books, Book addBook) {
        int oldSize = books.length;
        int newSize = (int) (oldSize * 1.5);

        if (newSize <= oldSize) newSize = oldSize + 1;
        Book[] newBooks = new Book[newSize];
        for (int i = 0; i < oldSize; i++) {
            newBooks[i] = books[i];
        }
        newBooks[oldSize] = addBook;
        return newBooks;
    }

    private static Member[] extendMembersAndAdd(Member[] members, Member addMember) {
        int oldSize = members.length;
        int newSize = (int) (oldSize * 1.5);

        if (newSize <= oldSize) newSize = oldSize + 1;
        Member[] newMembers = new Member[newSize];
        for (int i = 0; i < oldSize; i++) {
            newMembers[i] = members[i];
        }
        newMembers[oldSize] = addMember;
        return newMembers;
    }

    static BookLoan[] extendLoansAndAdd(BookLoan[] bookLoans, BookLoan addBookLoan) {
        int oldSize = bookLoans.length;
        int newSize = (int) (oldSize * 1.5);

        if (newSize <= oldSize) newSize = oldSize + 1;
        BookLoan[] newLoans = new BookLoan[newSize];
        for (int i = 0; i < oldSize; i++) {
            newLoans[i] = bookLoans[i];
        }
        newLoans[oldSize] = addBookLoan;
        return newLoans;
    }

    static int getEmpty(Object[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                return i;
            }
        }
        return -1;
    }

    static boolean isFull(Object[] array) {
        for (Object o : array) {
            if (o == null) {
                return false;
            }
        }
        return true;
    }


    // Input loop methods


    public static int getInput() {
        while(true){
            String line = IO.readln().trim();

            if (line.equalsIgnoreCase("q")) return 0;
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException _) {}
            IO.println("Invalid input! Please try again");
        }
    }

    public static int getInput(int min, int max) {
        while(true){
            String line = IO.readln().trim();

            if (line.equalsIgnoreCase("q")) return 0; //
            try {
                int input = Integer.parseInt(line);
                if (input >= min && input <= max) return input;
            } catch (NumberFormatException _) {}
            IO.println("Invalid input! Please try again");
        }
    }


}
