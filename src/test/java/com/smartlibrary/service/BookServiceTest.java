package com.smartlibrary.service;

import com.smartlibrary.dto.request.BookCreateRequest;
import com.smartlibrary.dto.response.BookResponse;
import com.smartlibrary.entity.BookStatus;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookServiceTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("Створення книги встановлює статус AVAILABLE")
    void createBook_success() {
        BookCreateRequest request = new BookCreateRequest("Книга А", "Автор А", "ISBN-A-100");
        BookResponse response = bookService.createBook(request);

        assertNotNull(response.id());
        assertEquals("Книга А", response.title());
        assertEquals("Автор А", response.author());
        assertEquals("ISBN-A-100", response.isbn());
        assertEquals(BookStatus.AVAILABLE, response.status());
        assertTrue(response.available());
    }

    @Test
    @DisplayName("Отримання списку книг повертає всі створені книги")
    void getAllBooks_returnsList() {
        bookService.createBook(new BookCreateRequest("Книга 1", "Автор 1", "ISBN-1"));
        bookService.createBook(new BookCreateRequest("Книга 2", "Автор 2", "ISBN-2"));

        List<BookResponse> books = bookService.getAllBooks();
        assertEquals(2, books.size());
    }

    @Test
    @DisplayName("Пошук неіснуючої книги викликає ResourceNotFoundException")
    void getBookById_notFound_throwsException() {
        assertThrows(ResourceNotFoundException.class, () ->
                bookService.getBookById(999999L)
        );
    }
}
