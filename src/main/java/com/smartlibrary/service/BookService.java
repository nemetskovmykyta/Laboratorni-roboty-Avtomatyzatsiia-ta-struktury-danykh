package com.smartlibrary.service;

import com.smartlibrary.dto.request.BookCreateRequest;
import com.smartlibrary.dto.response.BookResponse;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.BookStatus;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public BookResponse createBook(BookCreateRequest request) {
        Book book = new Book(
                request.title().trim(),
                request.author().trim(),
                request.isbn() != null ? request.isbn().trim() : null
        );
        book.setStatus(BookStatus.AVAILABLE);

        Book saved = bookRepository.save(book);
        return BookResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Book findEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книгу з ID " + id + " не знайдено"));
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        return BookResponse.from(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(BookResponse::from)
                .toList();
    }
}
