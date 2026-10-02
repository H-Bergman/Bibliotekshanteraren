import java.time.LocalDate;

public class Member {

    private int id;
    private String name;
    private BookLoan[] activeLoans = new BookLoan[5];

    public Member(String name, int lastId) {
        setName(name);
        setId(lastId);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setId(int id) {
        this.id = id;
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

    public void removeLoan(BookLoan loanToRemove) {
        for (int i = 0; i <  activeLoans.length; i++) {
            if (activeLoans[i] == loanToRemove) {
                activeLoans[i] = null;
                return;
            }
        }
    }

    public boolean canLoan() {
        LocalDate now = LocalDate.now();
        for  (BookLoan loan : activeLoans) {
            if(loan != null && loan.getReturnDate().isBefore(now)) {
                return false;
            }
        }
        return true;
    }

    public int printActiveLoans() {
        int loanCount = 0;
        for (BookLoan loan : activeLoans) {
            if (loan != null) {
                loanCount++;
                IO.println(loanCount + ") ");
                Library.printBook(loan.book());
            }
        }
        return loanCount;
    }

    public BookLoan getActiveLoanFromInput(int input) {
        int loanCount = 0;
        for (BookLoan loan : activeLoans) {
            if (loan != null) {
                loanCount++;
                if (loanCount == input) {
                    return loan;
                }
            }
        }
        return null;
    }

    public void printLoanCount() {
        IO.println("----------------------------");
        IO.println("Medlems-ID: " + id);
        IO.println("Namn: " + name);
        IO.println("Aktiva lån: " + getActiveLoanCount());
        IO.println("----------------------------");
    }

    public int getActiveLoanCount() {
        int count = 0;
        for (BookLoan loan : activeLoans) {
            if (loan != null) {
                count++;
            }
        }
        return count;
    }

}
