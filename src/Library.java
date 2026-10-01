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
            7) Statistics
            
            q) Quit
            """;

    static void main() {
        while (true) {
            IO.println(MAIN_MENU);
            int choice = getInput(1, 7);
            switch (choice) {
                case 1 -> addBook();
                case 2 -> registerMember();
                case 3 -> loanBookByTitle();
                case 4 -> returnBook();
                case 5 -> search();
                case 6 -> browse();
                case 7 -> showStatistics();
                case 0 -> { return; }
                default -> IO.println("Invalid choice.");
            }
        }
    }


    private static void addBook() {
        // TODO: Maybe add duplicate ISBN number check
        String bookTitle = getRequiredInput("Please enter the title: ");
        String bookAuthor = getRequiredInput("Please enter the author: ");
        String bookISBN = getRequiredInput("Please enter the ISBN: ");

        Book newBook = new Book(bookISBN, bookTitle, bookAuthor);


        if (!isFull(books)) {
            int emptyIndex = getEmpty(books);
            books[emptyIndex] = newBook;
        } else {
            books = extendBooksAndAdd(books, newBook);
        }
        IO.println("Book added successfully.");
    }



    private static void registerMember() {
        // TODO: Maybe add same member name check
        String memberName = getRequiredInput("Please enter the name of the member: ");

        lastMemberId++;
        Member  newMember = new Member(memberName, lastMemberId);

        if (!isFull(members)) {
            int emptyIndex = getEmpty(members);
            members[emptyIndex] = newMember;
        } else {
            members = extendMembersAndAdd(members, newMember);
        }
        IO.println("Member added successfully.");
    }

    private static void loanBookByTitle() {
        Member member = getMember();
        IO.println("Please enter the book title: ");
        String bookTitle = IO.readln().trim();
        for (Book book : books) {
            if (book != null && bookTitle.equalsIgnoreCase(book.title())) {
                IO.println("Found book: ");
                printBook(book);
                IO.println("Do you want to borrow this book? \u001B[90m[\u001B[1mY\u001B[0;90m/n]: \u001B[0m ");
                String yesNo = IO.readln().trim();
                if (yesNo.equalsIgnoreCase("y") || yesNo.equalsIgnoreCase("yes")) {
                    loanBook(book, member);
                }
                return;
            }
        }
        IO.println("No book found with that title");
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
        IO.println("The book has been loaned.");
    }

    private static void returnBook() {
        Member member = getMember();

        IO.println("These are your current book loans: ");
        int loanCount = member.printActiveLoans();

        if (loanCount == 0) {
            IO.println("No active loans");
        } else {
            IO.println("Please select which book to return (Input book number from above list): ");
            int choice = getInput(1, loanCount);

            BookLoan bookToReturn = member.getActiveLoanFromInput(choice);
            if (bookToReturn == null) {
                IO.println("Return canceled");
                return;
            }

            member.removeLoan(bookToReturn);
            removeLoanFromLibraryLoan(bookToReturn);

            if (returnOnTime(bookToReturn)) {
                IO.println("The book has been returned!");
            }  else {
                IO.println("The book has been returned past it's return date. Next time please return the book in time");
            }
        }

    }

    private static void search() {

        while (true) { // Gå ur loopen = retunera till huvudmeny
            IO.println("Search...");
            IO.println("Enter to search: ");
            String search = IO.readln().trim().toLowerCase();
            if (search.isBlank()) continue;
            Book[] matches = new Book[books.length];
            int matchIndex = 0;

            for (Book book : books) {
                if (book == null) continue;
                if (book.title().toLowerCase().contains(search) || book.author().toLowerCase().contains(search)) {
                    matches[matchIndex++] = book;
                }
            }
            if (matchIndex == 0) IO.println("No matches found");
            else {
                int listCount = 0;
                for (Book book : matches) {
                    if (book == null) continue;
                    listCount++;
                    IO.println(listCount + ") ");
                    printBook(book);
                    printBookLoan(book);
                }
                IO.println("Enter book number from the list to loan book or [Q] to return to main menu: ");
                int choice = getInput(1, listCount);
                if (choice == 0) return;
                Member member = getMember();
                int optionCount = 0;
                for (Book book : matches) {
                    if (book != null) {
                        optionCount++;
                        if (optionCount == choice) {
                            loanBook(book, member);
                            return;
                        }
                    }
                }
            }
        }
    }

    private static void browse() {
        int listIndex = 0;
        Book[] displayedBooks = new Book[books.length];
        for (int i = 0; i < books.length; i++) {
            displayedBooks[i] = books[i];
        }
        Book[] sortedBooks = sortBooks(displayedBooks);

        for (Book book : sortedBooks) {
            if (book == null) continue;
            listIndex++;
            IO.println(listIndex + ") ");
            printBook(book);
            printBookLoan(book);
        }
    }

    private static void showStatistics() {
        int listIndex = 0;
        Member[] displayedMembers = new Member[members.length];
        for (int i = 0; i < members.length; i++) {
            displayedMembers[i] = members[i];
        }
        Member[] sortedMembers = sortMembersByActiveLoans(displayedMembers);

        IO.println("List of members active loan count, starting with the member that has the highest amount of active loans: ");

        for (Member member : sortedMembers) {
            if (member == null) continue;
            listIndex++;
            IO.println(listIndex + ") ");
            member.printLoanCount();
        }
    }

    private static Member[] sortMembersByActiveLoans(Member[] members) {
        boolean swapped;
        for (int h = 0; h < members.length; h++) {
            swapped = false;
            for (int i = 0; i < members.length - h - 1; i++) {
                if (members[i] != null && members[i + 1] != null && members[i].getActiveLoanCount() < members[i +1].getActiveLoanCount()) {
                    Member temp = members[i];
                    members[i] = members[i + 1];
                    members[i + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
        return members;
    }

    private static Book[] sortBooks(Book[] books) {
        boolean swapped;
        for (int h = 0; h < books.length; h++) {
            swapped = false;
            for (int i = 0; i < books.length - h - 1; i++) {
                if (books[i] != null && books[i + 1] != null && titleComesAfter(books[i], books[i + 1])) {
                    Book temp = books[i];
                    books[i] = books[i + 1];
                    books[i + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
        return books;
    }

    // Helper methods
    private static boolean titleComesAfter(Book first, Book second) {
        String titleFirst = first.title().toLowerCase();
        String titleSecond = second.title().toLowerCase();
        int minLength = Math.min(titleFirst.length(), titleSecond.length());
        for (int i = 0; i < minLength; i++) {
            char char1 = titleFirst.charAt(i);
            char char2 = titleSecond.charAt(i);
            if (char1 != char2) {
                return char1 > char2;
            }
        }
        return titleFirst.length() > titleSecond.length();
    }

    private static BookLoan getBookLoan(Book book) {
        for (BookLoan bookLoan : bookLoans) {
            if (bookLoan != null && bookLoan.book().equals(book)) {
                return bookLoan;
            }
        }
        return null;
    }
    private static Member getMember() {
        IO.println("Please enter your name or your member ID: ");
        Member member;
        do {
            String line = IO.readln().trim();
            try {
                int memberId = Integer.parseInt(line);
                member = findMember(memberId);
            } catch (NumberFormatException e) {
                member = findMember(line);
            }
            if (member == null) {
                IO.println("Member not found, try again");
            }
        } while (member == null);
        return  member;
    }
    public static boolean returnOnTime(BookLoan loan) {
        return loan.getReturnDate().isAfter(LocalDate.now());
    }

    public static void removeLoanFromLibraryLoan(BookLoan loanToRemove) {
        for (int i = 0; i < bookLoans.length; i++) {
            if (bookLoans[i] == loanToRemove) {
                bookLoans[i] = null;
                return;
            }
        }
    }

    public static void printBookLoan(Book book) {
        BookLoan bookLoan = getBookLoan(book);
        if (bookLoan != null) {
            IO.println("""
                                Status: %s
                                Loaned by: %s
                                Return date: %s
                                """.formatted("Loaned out", bookLoan.member().getName(), bookLoan.getReturnDate().toString()));
        }
    }

    public static void printBook(Book book) {
        IO.println("----------------------------");
        IO.println("ISBN: " + book.isbn());
        IO.println("Title: " + book.title());
        IO.println("Author: " + book.author());
        IO.println("----------------------------");
    }

    private static Member findMember(String memberName) {
        for (Member member : members) {
            if (member != null && member.getName().equals(memberName)) {
                return member;
            }
        }
        return null;
    }

    private static Member findMember(int memberId) {
        for (Member member : members) {
            if (member != null && member.getId() == memberId) {
                return member;
            }
        }
        return null;
    }

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

    private static String getRequiredInput(String prompt) {
        while(true) {
            IO.println(prompt);
            String line = IO.readln().trim();
            if (!line.isEmpty()) return line;
            IO.println("Input can't be empty, try again");
        }
    }

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
