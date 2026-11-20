package com.smartlibrary.dto.response;

import com.smartlibrary.entity.ReaderReliabilityStatus;

import java.time.LocalDate;

public record ReliabilityResponse(
        Long readerId,
        String readerName,
        String readerEmail,
        ReaderReliabilityStatus status,
        boolean reliable,
        int maxAllowedLoans,
        long currentActiveLoansCount,
        int returnedInLast30DaysCount,
        boolean allReturnsOnTimeInLast30Days,
        boolean hasOverdueActiveLoans,
        LocalDate evaluationDate,
        String explanation
) {
}
