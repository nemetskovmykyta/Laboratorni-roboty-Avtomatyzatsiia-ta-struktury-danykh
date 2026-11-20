package com.smartlibrary.dto.request;

import jakarta.validation.constraints.NotNull;

public record LoanCreateRequest(
        @NotNull(message = "Ідентифікатор читача є обов'язковим")
        Long readerId,

        @NotNull(message = "Ідентифікатор книги є обов'язковим")
        Long bookId
) {
}
