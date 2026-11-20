package com.smartlibrary.dto.response;

import com.smartlibrary.entity.Reader;

import java.time.LocalDateTime;

public record ReaderResponse(
        Long id,
        String name,
        String email,
        String phone,
        LocalDateTime registeredAt
) {
    public static ReaderResponse from(Reader reader) {
        return new ReaderResponse(
                reader.getId(),
                reader.getName(),
                reader.getEmail(),
                reader.getPhone(),
                reader.getRegisteredAt()
        );
    }
}
