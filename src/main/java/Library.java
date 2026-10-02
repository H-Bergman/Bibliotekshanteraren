import java.time.LocalDate;

public class Library {

    static Book[] books = new Book[5];
    static Member[] members  = new Member[5];
    static BookLoan[] bookLoans  = new BookLoan[5];

    static int lastMemberId = 0;

    public static final String MAIN_MENU = """
            Välkommen till Bibliotekshanteraren!
            
            1) Lägg till bok i biblioteket
            2) Registrera ny medlem
            
            3) Låna en bok
            4) Returnera en bok
            
            5) Sök
            6) Bläddra
            7) Statistik
            
            q) Avsluta
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
                default -> IO.println("Ogiltigt val.");
            }
        }
    }


    private static void addBook() {
        String bookTitle = getRequiredInput("Ange titeln: ");
        String bookAuthor = getRequiredInput("Ange författaren: ");
        String bookISBN = getRequiredNumberInput("Ange ISBN: ");

        for (Book book : books) {
            if (book == null) continue;
            if (book.isbn().equals(bookISBN)) {
                IO.println("Boken finns redan. Återgår till huvudmenyn");
                return;
            }
        }

        Book newBook = new Book(bookISBN, bookTitle, bookAuthor);


        if (!isFull(books)) {
            int emptyIndex = getEmpty(books);
            books[emptyIndex] = newBook;
        } else {
            books = extendBooksAndAdd(books, newBook);
        }
        IO.println("Boken har lagts till.");
    }



    private static void registerMember() {
        String memberName = getRequiredInput("Ange medlemmens namn: ");

        lastMemberId++;
        Member  newMember = new Member(memberName, lastMemberId);

        if (!isFull(members)) {
            int emptyIndex = getEmpty(members);
            members[emptyIndex] = newMember;
        } else {
            members = extendMembersAndAdd(members, newMember);
        }
        IO.println("Medlemmen har lagts till.");
    }

    private static void loanBookByTitle() {
        Member member = getMember();
        if (member == null) return;
        IO.println("Ange boktiteln: ");
        String bookTitle = readLine();
        for (Book book : books) {
            if (book != null && bookTitle.equalsIgnoreCase(book.title())) {
                IO.println("Hittad bok: ");
                printBook(book);
                IO.println("Vill du låna den här boken? \u001B[90m[\u001B[1mJ\u001B[0;90m/n]: \u001B[0m ");
                String yesNo = readLine();
                if (yesNo.equalsIgnoreCase("j") || yesNo.equalsIgnoreCase("ja")) {
                    loanBook(book, member);
                }
                return;
            }
        }
        IO.println("Ingen bok med den titeln hittades");
    }

    private static void loanBook(Book bookToLoan, Member member) {
        if (member == null) return;
        if (!member.canLoan()) {
            IO.println("Du har försenade lån och kan inte låna nya böcker förrän de är återlämnade");
            return;
        }
        for (BookLoan bookLoan : bookLoans) {
            if(bookLoan != null && bookLoan.book().isbn().equals(bookToLoan.isbn())) {
                LocalDate returnDate = bookLoan.getReturnDate();
                IO.println("Boken är redan utlånad. Hitta en annan bok, eller återkom efter " + returnDate + " för att se om boken har återlämnats");
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
        IO.println("Boken har lånats.");
    }

    private static void returnBook() {
        Member member = getMember();
        if (member == null) return;

        IO.println("Dessa är dina aktuella boklån: ");
        int loanCount = member.printActiveLoans();

        if (loanCount == 0) {
            IO.println("Inga aktiva lån");
        } else {
            IO.println("Välj vilken bok du vill returnera (ange boknummer från listan ovan): ");
            int choice = getInput(1, loanCount);

            BookLoan bookToReturn = member.getActiveLoanFromInput(choice);
            if (bookToReturn == null) {
                IO.println("Återlämning avbruten");
                return;
            }

            member.removeLoan(bookToReturn);
            removeLoanFromLibraryLoan(bookToReturn);

            if (returnOnTime(bookToReturn)) {
                IO.println("Boken har returnerats!");
            }  else {
                IO.println("Boken returnerades efter sista återlämningsdatum. Vänligen återlämna boken i tid nästa gång");
            }
        }

    }

    private static void search() {

        while (true) {
            IO.println("Sök...");
            IO.println("Ange sökord: ");
            String search = readLine().toLowerCase();
            if (search.isBlank()) continue;
            if ("q".equals(search)) return;
            Book[] matches = new Book[books.length];
            int matchIndex = 0;

            for (Book book : books) {
                if (book == null) continue;
                if (book.title().toLowerCase().contains(search) || book.author().toLowerCase().contains(search)) {
                    matches[matchIndex++] = book;
                }
            }
            if (matchIndex == 0) IO.println("Inga träffar hittades");
            else {
                int listCount = 0;
                for (Book book : matches) {
                    if (book == null) continue;
                    listCount++;
                    IO.println(listCount + ") ");
                    printBook(book);
                    printBookLoan(book);
                }
                IO.println("Ange boknummer från listan för att låna boken, eller [Q] för att återgå till huvudmenyn: ");
                int choice = getInput(1, listCount);
                if (choice == 0) return;
                Member member = getMember();
                if (member == null) return;
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
        if (listIndex == 0) IO.println("Inga böcker i biblioteket");
    }

    private static void showStatistics() {
        int listIndex = 0;
        Member[] displayedMembers = new Member[members.length];
        for (int i = 0; i < members.length; i++) {
            displayedMembers[i] = members[i];
        }
        Member[] sortedMembers = sortMembersByActiveLoans(displayedMembers);

        IO.println("Lista över medlemmars antal aktiva lån, med flest aktiva lån överst: ");

        for (Member member : sortedMembers) {
            if (member == null) continue;
            listIndex++;
            IO.println(listIndex + ") ");
            member.printLoanCount();
        }
        if (listIndex == 0) IO.println("Inga medlemmar registrerade");
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
        IO.println("Ange ditt namn eller medlems-ID: ");
        Member member;
        String line;
        do {
            line = readLine();
            if (line.equalsIgnoreCase("q")) return null;
            try {
                int memberId = Integer.parseInt(line);
                member = findMember(memberId);
            } catch (NumberFormatException e) {
                member = findMember(line);
            }
            if (member == null) {
                IO.println("Medlemmen hittades inte, försök igen");
            }
        } while (member == null);
        return  member;
    }
    public static boolean returnOnTime(BookLoan loan) {
        return !loan.getReturnDate().isBefore(LocalDate.now());
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
                                Utlånad till: %s
                                Återlämningsdatum: %s
                                """.formatted("Utlånad", bookLoan.member().getName(), bookLoan.getReturnDate().toString()));
        }
        else {
            IO.println("Status: Tillgänglig");
        }
    }

    public static void printBook(Book book) {
        IO.println("----------------------------");
        IO.println("ISBN: " + book.isbn());
        IO.println("Titel: " + book.title());
        IO.println("Författare: " + book.author());
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

    private static String readLine() {
        String line = IO.readln();
        return line == null ? "q" : line.trim();
    }

    private static String getRequiredInput(String prompt) {
        while(true) {
            IO.println(prompt);
            String line = readLine();
            if (!line.isBlank()) return line;
            IO.println("Fältet kan inte vara tomt, försök igen");
        }
    }

    private static String getRequiredNumberInput(String prompt) {
        while(true) {
            IO.println(prompt);
            String line = readLine();
            if (line.isBlank()) {
                IO.println("Fältet kan inte vara tomt, försök igen");
                continue;
            }
            if (!isNumber(line)) {
                IO.println("Endast siffror tillåtna, försök igen");
                continue;
            }
            return line;
        }
    }
    private static boolean isNumber(String line) {
        if (line == null || line.isBlank()) return false;
        for (int i = 0; i < line.length(); i++) {
            if (!Character.isDigit(line.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static int getInput(int min, int max) {
        while(true){
            String line = readLine();

            if (line.equalsIgnoreCase("q")) return 0; //
            try {
                int input = Integer.parseInt(line);
                if (input >= min && input <= max) return input;
            } catch (NumberFormatException _) {}
            IO.println("Ogiltig inmatning! Försök igen");
        }
    }


}
