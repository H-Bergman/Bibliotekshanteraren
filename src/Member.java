import java.time.LocalDate;

public class Member {

    private int id;
    private String name;
    private BookLoan[] activeLoans = new BookLoan[5];

    public Member(String name, int lastId) {
        this.name = name;
        this.id = lastId;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public BookLoan[] getActiveLoans() {
        return activeLoans;
    }

    public void addLoan(BookLoan loan) {
        if (!Library.isFull(this.activeLoans)) {
            int emptyIndex = Library.getEmpty(this.activeLoans);
            activeLoans[emptyIndex] = loan;
        } else {
            this.activeLoans = Library.extendLoansAndAdd(this.activeLoans, loan);
        }
    }

    public boolean canLoan() {
        LocalDate now = LocalDate.now();
        for  (BookLoan loan : activeLoans) {
            if(loan.getReturnDate().isAfter(now)) {
                return false;
            }
        }
        return true;
    }
}
