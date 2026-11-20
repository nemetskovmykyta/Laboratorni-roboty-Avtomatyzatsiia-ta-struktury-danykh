package com.smartlibrary.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReaderCreateRequest(
        @NotBlank(message = "Ім'я читача є обов'язковим")
        @Size(min = 2, max = 100, message = "Ім'я читача повинно містити від 2 до 100 символів")
        String name,

        @NotBlank(message = "Email є обов'язковим")
        @Email(message = "Некоректний формат email")
        String email,

        String phone
) {
}
