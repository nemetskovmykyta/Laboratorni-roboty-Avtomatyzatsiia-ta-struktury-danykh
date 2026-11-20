package com.smartlibrary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookCreateRequest(
        @NotBlank(message = "Назва книги є обов'язковою")
        @Size(min = 1, max = 255, message = "Назва книги повинна містити від 1 до 255 символів")
        String title,

        @NotBlank(message = "Автор книги є обов'язковим")
        @Size(min = 1, max = 255, message = "Автор книги повинен містити від 1 до 255 символів")
        String author,

        String isbn
) {
}
