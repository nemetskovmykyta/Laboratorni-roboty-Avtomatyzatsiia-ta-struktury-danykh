package com.smartlibrary.service;

import com.smartlibrary.dto.request.LoanCreateRequest;
import com.smartlibrary.dto.response.LoanResponse;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.BookStatus;
import com.smartlibrary.entity.Loan;
import com.smartlibrary.entity.Reader;
import com.smartlibrary.exception.BookNotAvailableException;
import com.smartlibrary.exception.LoanAlreadyReturnedException;
import com.smartlibrary.exception.LoanLimitExceededException;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoanServiceTest {

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    private LoanService loanService;
    private ReliabilityService reliabilityService;

    private final LocalDate fixedNow = LocalDate.of(2026, 11, 20);
    private final Clock fixedClock = Clock.fixed(fixedNow.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    private Reader reader;
    private Book book1;
    private Book book2;
    private Book book3;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        readerRepository.deleteAll();

        reliabilityService = new ReliabilityService(readerRepository, loanRepository, fixedClock);
        loanService = new LoanService(loanRepository, readerRepository, bookRepository, reliabilityService, fixedClock);

        reader = readerRepository.save(new Reader("Тестовий Читач", "reader@example.com", "+380501234567"));
        book1 = bookRepository.save(new Book("Книга 1", "Автор 1", "ISBN-1"));
        book2 = bookRepository.save(new Book("Книга 2", "Автор 2", "ISBN-2"));
        book3 = bookRepository.save(new Book("Книга 3", "Автор 3", "ISBN-3"));
    }

    @Test
    @DisplayName("Успішна видача книги встановлює термін 14 днів і змінює статус книги на LOANED")
    void createLoan_success() {
        LoanResponse response = loanService.createLoan(new LoanCreateRequest(reader.getId(), book1.getId()));

        assertNotNull(response.id());
        assertEquals(reader.getId(), response.readerId());
        assertEquals(book1.getId(), response.bookId());
        assertEquals(fixedNow, response.loanDate());
        assertEquals(fixedNow.plusDays(14), response.dueDate());
        assertNull(response.returnDate());
        assertTrue(response.active());

        // Перевіряємо оновлення статусу книги в БД
        Book updatedBook = bookRepository.findById(book1.getId()).orElseThrow();
        assertEquals(BookStatus.LOANED, updatedBook.getStatus());
        assertFalse(updatedBook.isAvailable());
    }

    @Test
    @DisplayName("Спроба видати книгу, яка вже видана (LOANED), викликає BookNotAvailableException")
    void createLoan_bookAlreadyLoaned_throwsException() {
        // Видаємо book1
        loanService.createLoan(new LoanCreateRequest(reader.getId(), book1.getId()));

        // Створюємо другого читача
        Reader reader2 = readerRepository.save(new Reader("Другий Читач", "reader2@example.com", "+380509999999"));

        // Спроба видати ту саму book1 другому читачу
        assertThrows(BookNotAvailableException.class, () ->
                loanService.createLoan(new LoanCreateRequest(reader2.getId(), book1.getId()))
        );
    }

    @Test
    @DisplayName("Звичайний читач може мати не більше 2 активних позик: спроба взяти 3-тю книгу викидає виняток")
    void createLoan_regularReaderExceedsLimit_throwsException() {
        // Бере 1-шу книгу
        loanService.createLoan(new LoanCreateRequest(reader.getId(), book1.getId()));
        // Бере 2-гу книгу
        loanService.createLoan(new LoanCreateRequest(reader.getId(), book2.getId()));

        // Спроба взяти 3-тю книгу звичайним читачем
        assertThrows(LoanLimitExceededException.class, () ->
                loanService.createLoan(new LoanCreateRequest(reader.getId(), book3.getId()))
        );
    }

    @Test
    @DisplayName("Надійний читач може взяти до 5 активних позик, спроба взяти 6-ту книгу викликає виняток")
    void createLoan_reliableReader_canBorrowUpToFiveBooks() {
        // Робимо читача надійним: створюємо 3 повернені вчасно книги за останні 30 днів
        for (int i = 1; i <= 3; i++) {
            Book oldBook = bookRepository.save(new Book("Стара книга " + i, "Автор", "ISBN-OLD-" + i));
            Loan returnedLoan = new Loan(reader, oldBook, fixedNow.minusDays(15));
            returnedLoan.setDueDate(fixedNow.minusDays(1));
            returnedLoan.setReturnDate(fixedNow.minusDays(5));
            loanRepository.save(returnedLoan);
        }

        // Створюємо 6 нових книг
        Book b1 = bookRepository.save(new Book("B1", "A", "111"));
        Book b2 = bookRepository.save(new Book("B2", "A", "222"));
        Book b3 = bookRepository.save(new Book("B3", "A", "333"));
        Book b4 = bookRepository.save(new Book("B4", "A", "444"));
        Book b5 = bookRepository.save(new Book("B5", "A", "555"));
        Book b6 = bookRepository.save(new Book("B6", "A", "666"));

        // Надійний читач бере 5 книг поспіль
        loanService.createLoan(new LoanCreateRequest(reader.getId(), b1.getId()));
        loanService.createLoan(new LoanCreateRequest(reader.getId(), b2.getId()));
        loanService.createLoan(new LoanCreateRequest(reader.getId(), b3.getId()));
        loanService.createLoan(new LoanCreateRequest(reader.getId(), b4.getId()));
        loanService.createLoan(new LoanCreateRequest(reader.getId(), b5.getId()));

        assertEquals(5, loanRepository.countByReaderIdAndReturnDateIsNull(reader.getId()));

        // Спроба взяти 6-ту книгу
        assertThrows(LoanLimitExceededException.class, () ->
                loanService.createLoan(new LoanCreateRequest(reader.getId(), b6.getId()))
        );
    }

    @Test
    @DisplayName("Повернення книги фіксує дату повернення та робить книгу AVAILABLE, позика залишається в БД")
    void returnBook_success() {
        LoanResponse loan = loanService.createLoan(new LoanCreateRequest(reader.getId(), book1.getId()));

        LoanResponse returned = loanService.returnBook(loan.id());

        assertEquals(fixedNow, returned.returnDate());
        assertFalse(returned.active());

        // Перевіряємо статус книги
        Book updatedBook = bookRepository.findById(book1.getId()).orElseThrow();
        assertEquals(BookStatus.AVAILABLE, updatedBook.getStatus());
        assertTrue(updatedBook.isAvailable());

        // Перевіряємо, що позика залишилася в БД
        Loan dbLoan = loanRepository.findById(loan.id()).orElseThrow();
        assertEquals(fixedNow, dbLoan.getReturnDate());
    }

    @Test
    @DisplayName("Повторна спроба повернути вже повернуту книгу викликає LoanAlreadyReturnedException")
    void returnBook_alreadyReturned_throwsException() {
        LoanResponse loan = loanService.createLoan(new LoanCreateRequest(reader.getId(), book1.getId()));
        loanService.returnBook(loan.id());

        assertThrows(LoanAlreadyReturnedException.class, () ->
                loanService.returnBook(loan.id())
        );
    }

    @Test
    @DisplayName("Отримання активних позик читача повертає лише неповернуті книги")
    void getActiveLoansByReader_returnsOnlyActive() {
        LoanResponse loan1 = loanService.createLoan(new LoanCreateRequest(reader.getId(), book1.getId()));
        LoanResponse loan2 = loanService.createLoan(new LoanCreateRequest(reader.getId(), book2.getId()));

        // Повертаємо першу книгу
        loanService.returnBook(loan1.id());

        List<LoanResponse> activeLoans = loanService.getActiveLoansByReader(reader.getId());

        assertEquals(1, activeLoans.size());
        assertEquals(loan2.id(), activeLoans.get(0).id());
        assertEquals(book2.getId(), activeLoans.get(0).bookId());
    }
}
