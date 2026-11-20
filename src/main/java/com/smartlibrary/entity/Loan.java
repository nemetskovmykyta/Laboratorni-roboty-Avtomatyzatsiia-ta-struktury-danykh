package com.smartlibrary.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "loans")
public class Loan {

    public static final int STANDARD_LOAN_DAYS = 14;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "reader_id", nullable = false)
    private Reader reader;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    public Loan() {
    }

    public Loan(Reader reader, Book book, LocalDate loanDate) {
        this.reader = reader;
        this.book = book;
        this.loanDate = loanDate;
        this.dueDate = loanDate.plusDays(STANDARD_LOAN_DAYS);
        this.returnDate = null;
    }

    public Loan(Reader reader, Book book, LocalDate loanDate, LocalDate dueDate) {
        this.reader = reader;
        this.book = book;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isActive() {
        return this.returnDate == null;
    }

    public boolean isOverdue(LocalDate asOfDate) {
        return isActive() && this.dueDate.isBefore(asOfDate);
    }

    public boolean wasReturnedOnTime() {
        return this.returnDate != null && !this.returnDate.isAfter(this.dueDate);
    }

    public boolean wasReturnedLate() {
        return this.returnDate != null && this.returnDate.isAfter(this.dueDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return Objects.equals(id, loan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", readerId=" + (reader != null ? reader.getId() : null) +
                ", bookId=" + (book != null ? book.getId() : null) +
                ", loanDate=" + loanDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                '}';
    }
}
