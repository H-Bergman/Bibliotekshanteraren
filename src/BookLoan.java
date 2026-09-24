import java.time.LocalDate;

public record BookLoan(Member member, Book book, LocalDate loanDate) {
    public BookLoan(Member member, Book book) {
        this(member, book, LocalDate.now());
    }
    public LocalDate getReturnDate() {
        return loanDate.plusDays(14);
    }
}
