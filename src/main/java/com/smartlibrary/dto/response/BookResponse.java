package com.smartlibrary.dto.response;

import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.BookStatus;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        BookStatus status,
        boolean available
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getStatus(),
                book.isAvailable()
        );
    }
}
