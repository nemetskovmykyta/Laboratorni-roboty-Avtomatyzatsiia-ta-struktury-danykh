package com.smartlibrary.repository;

import com.smartlibrary.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByReaderId(Long readerId);

    List<Loan> findByReaderIdAndReturnDateIsNull(Long readerId);

    long countByReaderIdAndReturnDateIsNull(Long readerId);

    Optional<Loan> findByBookIdAndReturnDateIsNull(Long bookId);

    @Query("SELECT l FROM Loan l WHERE l.reader.id = :readerId AND l.returnDate IS NOT NULL AND l.returnDate >= :sinceDate AND l.returnDate <= :untilDate")
    List<Loan> findReturnedLoansSince(
            @Param("readerId") Long readerId,
            @Param("sinceDate") LocalDate sinceDate,
            @Param("untilDate") LocalDate untilDate
    );

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.reader.id = :readerId AND l.returnDate IS NULL AND l.dueDate < :asOfDate")
    long countOverdueActiveLoans(
            @Param("readerId") Long readerId,
            @Param("asOfDate") LocalDate asOfDate
    );

    @Query("SELECT l FROM Loan l WHERE l.reader.id = :readerId AND l.returnDate IS NULL AND l.dueDate < :asOfDate")
    List<Loan> findOverdueActiveLoans(
            @Param("readerId") Long readerId,
            @Param("asOfDate") LocalDate asOfDate
    );
}
