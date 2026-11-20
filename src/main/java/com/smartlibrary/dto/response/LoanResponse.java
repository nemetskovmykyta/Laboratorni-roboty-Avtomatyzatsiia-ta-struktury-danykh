package com.smartlibrary.dto.response;

import com.smartlibrary.entity.Loan;

import java.time.LocalDate;

public record LoanResponse(
        Long id,
        Long readerId,
        String readerName,
        String readerEmail,
        Long bookId,
        String bookTitle,
        String bookAuthor,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate,
        boolean active,
        boolean overdue
) {
    public static LoanResponse from(Loan loan, LocalDate currentDate) {
        return new LoanResponse(
                loan.getId(),
                loan.getReader().getId(),
                loan.getReader().getName(),
                loan.getReader().getEmail(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getBook().getAuthor(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.isActive(),
                loan.isOverdue(currentDate)
        );
    }
}
