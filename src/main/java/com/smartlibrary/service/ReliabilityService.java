package com.smartlibrary.service;

import com.smartlibrary.dto.response.ReliabilityResponse;
import com.smartlibrary.entity.Loan;
import com.smartlibrary.entity.Reader;
import com.smartlibrary.entity.ReaderReliabilityStatus;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.repository.LoanRepository;
import com.smartlibrary.repository.ReaderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReliabilityService {

    public static final int REQUIRED_RETURNS_COUNT = 3;
    public static final int EVALUATION_WINDOW_DAYS = 30;

    private final ReaderRepository readerRepository;
    private final LoanRepository loanRepository;
    private final Clock clock;

    public ReliabilityService(ReaderRepository readerRepository, LoanRepository loanRepository, Clock clock) {
        this.readerRepository = readerRepository;
        this.loanRepository = loanRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ReliabilityResponse evaluateReliability(Long readerId) {
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new ResourceNotFoundException("Читача з ID " + readerId + " не знайдено"));

        LocalDate today = LocalDate.now(clock);
        LocalDate windowStart = today.minusDays(EVALUATION_WINDOW_DAYS);

        // 1. Повернення за останні 30 днів
        List<Loan> returnedInWindow = loanRepository.findReturnedLoansSince(readerId, windowStart, today);
        int returnedCount = returnedInWindow.size();
        boolean hasEnoughReturns = returnedCount >= REQUIRED_RETURNS_COUNT;

        // 2. Усі повернені книги протягом цього періоду повернені не пізніше встановленого терміну
        boolean anyLateReturns = returnedInWindow.stream().anyMatch(Loan::wasReturnedLate);
        boolean allReturnsOnTime = !anyLateReturns;

        // 3. Відсутність прострочених активних позик на момент перевірки
        long overdueActiveLoansCount = loanRepository.countOverdueActiveLoans(readerId, today);
        boolean hasNoOverdueActiveLoans = overdueActiveLoansCount == 0;

        // Усі 3 умови мають виконуватись одночасно
        boolean isReliable = hasEnoughReturns && allReturnsOnTime && hasNoOverdueActiveLoans;
        ReaderReliabilityStatus status = isReliable ? ReaderReliabilityStatus.RELIABLE : ReaderReliabilityStatus.REGULAR;

        long currentActiveLoansCount = loanRepository.countByReaderIdAndReturnDateIsNull(readerId);

        String explanation = buildExplanation(
                isReliable,
                returnedCount,
                anyLateReturns,
                overdueActiveLoansCount,
                status.getMaxActiveLoans()
        );

        return new ReliabilityResponse(
                reader.getId(),
                reader.getName(),
                reader.getEmail(),
                status,
                isReliable,
                status.getMaxActiveLoans(),
                currentActiveLoansCount,
                returnedCount,
                allReturnsOnTime,
                overdueActiveLoansCount > 0,
                today,
                explanation
        );
    }

    @Transactional(readOnly = true)
    public ReaderReliabilityStatus getReliabilityStatus(Long readerId) {
        return evaluateReliability(readerId).status();
    }

    private String buildExplanation(
            boolean isReliable,
            int returnedCount,
            boolean anyLateReturns,
            long overdueActiveCount,
            int maxAllowed
    ) {
        if (isReliable) {
            return String.format(
                    "Читач має рівень 'НАДІЙНИЙ' (максимум %d активних позик): за останні 30 днів повернуто %d книг (вимога: >= %d), усі вчасно, прострочених активних позик немає.",
                    maxAllowed,
                    returnedCount,
                    REQUIRED_RETURNS_COUNT
            );
        }

        List<String> failedReasons = new ArrayList<>();
        if (returnedCount < REQUIRED_RETURNS_COUNT) {
            failedReasons.add(String.format("повернуто лише %d книг за останні 30 днів (вимагається щонайменше %d)",
                    returnedCount, REQUIRED_RETURNS_COUNT));
        }
        if (anyLateReturns) {
            failedReasons.add("серед книг, повернутих за останні 30 днів, є повернуті із запізненням");
        }
        if (overdueActiveCount > 0) {
            failedReasons.add(String.format("наявно %d прострочених активних позик на руках", overdueActiveCount));
        }

        return String.format(
                "Читач має рівень 'ЗВИЧАЙНИЙ' (максимум %d активних позик). Не виконано умови надійності: %s.",
                maxAllowed,
                String.join("; ", failedReasons)
        );
    }
}
