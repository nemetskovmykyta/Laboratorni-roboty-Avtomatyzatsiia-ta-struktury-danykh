package com.smartlibrary.service;

import com.smartlibrary.dto.response.ReliabilityResponse;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.BookStatus;
import com.smartlibrary.entity.Loan;
import com.smartlibrary.entity.Reader;
import com.smartlibrary.entity.ReaderReliabilityStatus;
import com.smartlibrary.repository.BookRepository;
import com.smartlibrary.repository.LoanRepository;
import com.smartlibrary.repository.ReaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReliabilityServiceTest {

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    private ReliabilityService reliabilityService;

    // Фіксована дата для передбачуваності тестів: 2026-11-20
    private final LocalDate fixedNow = LocalDate.of(2026, 11, 20);
    private final Clock fixedClock = Clock.fixed(fixedNow.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    private Reader testReader;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        readerRepository.deleteAll();

        reliabilityService = new ReliabilityService(readerRepository, loanRepository, fixedClock);
        testReader = readerRepository.save(new Reader("Тестовий Читач", "test.reader@example.com", "+380500000000"));
    }

    private Book createBook(String title) {
        return bookRepository.save(new Book(title, "Автор", "ISBN-" + title.hashCode()));
    }

    @Test
    @DisplayName("Новий читач без повернень має статус REGULAR (макс. 2 книги)")
    void newReader_isRegular() {
        ReliabilityResponse response = reliabilityService.evaluateReliability(testReader.getId());

        assertEquals(ReaderReliabilityStatus.REGULAR, response.status());
        assertFalse(response.reliable());
        assertEquals(2, response.maxAllowedLoans());
        assertEquals(0, response.returnedInLast30DaysCount());
        assertFalse(response.hasOverdueActiveLoans());
    }

    @Test
    @DisplayName("Читач із менш ніж 3 поверненнями за 30 днів залишається REGULAR")
    void readerWithLessThanThreeReturns_isRegular() {
        // Створюємо 2 повернені вчасно книги 10 днів тому
        for (int i = 1; i <= 2; i++) {
            Book book = createBook("Книга " + i);
            Loan loan = new Loan(testReader, book, fixedNow.minusDays(20));
            loan.setDueDate(fixedNow.minusDays(6));
            loan.setReturnDate(fixedNow.minusDays(10)); // вчасно (10 днів тому < 6 днів тому термін)
            loanRepository.save(loan);
        }

        ReliabilityResponse response = reliabilityService.evaluateReliability(testReader.getId());

        assertEquals(ReaderReliabilityStatus.REGULAR, response.status());
        assertFalse(response.reliable());
        assertEquals(2, response.returnedInLast30DaysCount());
    }

    @Test
    @DisplayName("Читач із 3+ вчасними поверненнями за останні 30 днів і без прострочень стає RELIABLE (макс. 5 книг)")
    void readerWithThreeOnTimeReturns_isReliable() {
        // Створюємо 3 повернені вчасно книги протягом останніх 30 днів
        for (int i = 1; i <= 3; i++) {
            Book book = createBook("Книга " + i);
            Loan loan = new Loan(testReader, book, fixedNow.minusDays(15));
            loan.setDueDate(fixedNow.minusDays(1));
            loan.setReturnDate(fixedNow.minusDays(5)); // повернено за 5 днів до сьогодні (вчасно)
            loanRepository.save(loan);
        }

        ReliabilityResponse response = reliabilityService.evaluateReliability(testReader.getId());

        assertEquals(ReaderReliabilityStatus.RELIABLE, response.status());
        assertTrue(response.reliable());
        assertEquals(5, response.maxAllowedLoans());
        assertEquals(3, response.returnedInLast30DaysCount());
        assertTrue(response.allReturnsOnTimeInLast30Days());
        assertFalse(response.hasOverdueActiveLoans());
    }

    @Test
    @DisplayName("Читач із 3 поверненнями, де хоча б одне було із запізненням, залишається REGULAR")
    void readerWithLateReturn_isRegular() {
        // 2 книги вчасно
        for (int i = 1; i <= 2; i++) {
            Book book = createBook("Книга " + i);
            Loan loan = new Loan(testReader, book, fixedNow.minusDays(25));
            loan.setDueDate(fixedNow.minusDays(11));
            loan.setReturnDate(fixedNow.minusDays(15)); // вчасно
            loanRepository.save(loan);
        }

        // 1 книга із запізненням (термін був 10 днів тому, повернена 2 дні тому)
        Book bookLate = createBook("Запізніла Книга");
        Loan lateLoan = new Loan(testReader, bookLate, fixedNow.minusDays(24));
        lateLoan.setDueDate(fixedNow.minusDays(10));
        lateLoan.setReturnDate(fixedNow.minusDays(2)); // повернено пізніше dueDate!
        loanRepository.save(lateLoan);

        ReliabilityResponse response = reliabilityService.evaluateReliability(testReader.getId());

        assertEquals(ReaderReliabilityStatus.REGULAR, response.status());
        assertFalse(response.reliable());
        assertEquals(3, response.returnedInLast30DaysCount());
        assertFalse(response.allReturnsOnTimeInLast30Days());
    }

    @Test
    @DisplayName("Читач із 3 вчасними поверненнями, але з наявною простроченою активною позикою, залишається REGULAR")
    void readerWithOverdueActiveLoan_isRegular() {
        // 3 книги повернуті вчасно
        for (int i = 1; i <= 3; i++) {
            Book book = createBook("Книга " + i);
            Loan loan = new Loan(testReader, book, fixedNow.minusDays(20));
            loan.setDueDate(fixedNow.minusDays(6));
            loan.setReturnDate(fixedNow.minusDays(7));
            loanRepository.save(loan);
        }

        // Активна прострочена позика: видана 20 днів тому, термін був 6 днів тому, ще не повернута (returnDate == null)
        Book activeOverdueBook = createBook("Прострочена книга на руках");
        Loan overdueLoan = new Loan(testReader, activeOverdueBook, fixedNow.minusDays(20));
        overdueLoan.setDueDate(fixedNow.minusDays(6)); // прострочена відносно fixedNow
        overdueLoan.setReturnDate(null);
        loanRepository.save(overdueLoan);

        ReliabilityResponse response = reliabilityService.evaluateReliability(testReader.getId());

        assertEquals(ReaderReliabilityStatus.REGULAR, response.status());
        assertFalse(response.reliable());
        assertTrue(response.hasOverdueActiveLoans());
    }

    @Test
    @DisplayName("Повернення, що відбулися більше 30 днів тому, не враховуються у вікні надійності")
    void returnsOlderThan30Days_areNotCounted() {
        // 3 книги повернуті 35 днів тому (поза 30-денним вікном)
        for (int i = 1; i <= 3; i++) {
            Book book = createBook("Стара Книга " + i);
            Loan loan = new Loan(testReader, book, fixedNow.minusDays(50));
            loan.setDueDate(fixedNow.minusDays(36));
            loan.setReturnDate(fixedNow.minusDays(35));
            loanRepository.save(loan);
        }

        ReliabilityResponse response = reliabilityService.evaluateReliability(testReader.getId());

        assertEquals(ReaderReliabilityStatus.REGULAR, response.status());
        assertFalse(response.reliable());
        assertEquals(0, response.returnedInLast30DaysCount());
    }
}
