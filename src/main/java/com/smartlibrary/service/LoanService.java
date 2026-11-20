package com.smartlibrary.service;

import com.smartlibrary.dto.request.LoanCreateRequest;
import com.smartlibrary.dto.response.LoanResponse;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.BookStatus;
import com.smartlibrary.entity.Loan;
import com.smartlibrary.entity.Reader;
import com.smartlibrary.entity.ReaderReliabilityStatus;
import com.smartlibrary.exception.BookNotAvailableException;
import com.smartlibrary.exception.LoanAlreadyReturnedException;
import com.smartlibrary.exception.LoanLimitExceededException;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.repository.BookRepository;
import com.smartlibrary.repository.LoanRepository;
import com.smartlibrary.repository.ReaderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;
    private final ReliabilityService reliabilityService;
    private final Clock clock;

    public LoanService(
            LoanRepository loanRepository,
            ReaderRepository readerRepository,
            BookRepository bookRepository,
            ReliabilityService reliabilityService,
            Clock clock
    ) {
        this.loanRepository = loanRepository;
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
        this.reliabilityService = reliabilityService;
        this.clock = clock;
    }

    @Transactional
    public LoanResponse createLoan(LoanCreateRequest request) {
        Reader reader = readerRepository.findById(request.readerId())
                .orElseThrow(() -> new ResourceNotFoundException("Читача з ID " + request.readerId() + " не знайдено"));

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Книгу з ID " + request.bookId() + " не знайдено"));

        if (!book.isAvailable()) {
            throw new BookNotAvailableException(String.format(
                    "Книга '%s' (ID: %d) наразі недоступна для видачі (поточний статус: %s)",
                    book.getTitle(), book.getId(), book.getStatus()
            ));
        }

        ReaderReliabilityStatus reliabilityStatus = reliabilityService.getReliabilityStatus(reader.getId());
        long activeLoansCount = loanRepository.countByReaderIdAndReturnDateIsNull(reader.getId());

        if (activeLoansCount >= reliabilityStatus.getMaxActiveLoans()) {
            throw new LoanLimitExceededException(String.format(
                    "Читач '%s' (ID: %d) досяг максимального ліміту активних позик (%d для статусу %s). Поточна кількість активних позик: %d",
                    reader.getName(), reader.getId(), reliabilityStatus.getMaxActiveLoans(),
                    reliabilityStatus.name(), activeLoansCount
            ));
        }

        LocalDate today = LocalDate.now(clock);
        Loan loan = new Loan(reader, book, today);
        book.setStatus(BookStatus.LOANED);

        bookRepository.save(book);
        Loan savedLoan = loanRepository.save(loan);

        return LoanResponse.from(savedLoan, today);
    }

    @Transactional
    public LoanResponse returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Позику з ID " + loanId + " не знайдено"));

        if (loan.getReturnDate() != null) {
            throw new LoanAlreadyReturnedException(String.format(
                    "Книга за позикою з ID %d вже була повернена раніше (%s)",
                    loanId, loan.getReturnDate()
            ));
        }

        LocalDate today = LocalDate.now(clock);
        loan.setReturnDate(today);

        Book book = loan.getBook();
        book.setStatus(BookStatus.AVAILABLE);

        bookRepository.save(book);
        Loan savedLoan = loanRepository.save(loan);

        return LoanResponse.from(savedLoan, today);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getActiveLoansByReader(Long readerId) {
        if (!readerRepository.existsById(readerId)) {
            throw new ResourceNotFoundException("Читача з ID " + readerId + " не знайдено");
        }

        LocalDate today = LocalDate.now(clock);
        return loanRepository.findByReaderIdAndReturnDateIsNull(readerId).stream()
                .map(loan -> LoanResponse.from(loan, today))
                .toList();
    }

    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Позику з ID " + loanId + " не знайдено"));
        LocalDate today = LocalDate.now(clock);
        return LoanResponse.from(loan, today);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getAllLoans() {
        LocalDate today = LocalDate.now(clock);
        return loanRepository.findAll().stream()
                .map(loan -> LoanResponse.from(loan, today))
                .toList();
    }
}
