package com.smartlibrary.service;

import com.smartlibrary.dto.request.ReaderCreateRequest;
import com.smartlibrary.dto.response.ReaderResponse;
import com.smartlibrary.exception.EmailAlreadyExistsException;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.repository.ReaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReaderServiceTest {

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private ReaderService readerService;

    @BeforeEach
    void setUp() {
        readerRepository.deleteAll();
    }

    @Test
    @DisplayName("Створення читача успішно зберігає читача")
    void createReader_success() {
        ReaderCreateRequest request = new ReaderCreateRequest("Іван Петренко", "ivan.petrenko@example.com", "+380501112233");
        ReaderResponse response = readerService.createReader(request);

        assertNotNull(response.id());
        assertEquals("Іван Петренко", response.name());
        assertEquals("ivan.petrenko@example.com", response.email());
        assertEquals("+380501112233", response.phone());
        assertNotNull(response.registeredAt());
    }

    @Test
    @DisplayName("Створення читача з дублікатом email викликає EmailAlreadyExistsException")
    void createReader_duplicateEmail_throwsException() {
        readerService.createReader(new ReaderCreateRequest("Перший Читач", "unique@example.com", null));

        assertThrows(EmailAlreadyExistsException.class, () ->
                readerService.createReader(new ReaderCreateRequest("Другий Читач", "unique@example.com", null))
        );
    }

    @Test
    @DisplayName("Пошук неіснуючого читача викликає ResourceNotFoundException")
    void getReaderById_notFound_throwsException() {
        assertThrows(ResourceNotFoundException.class, () ->
                readerService.getReaderById(999999L)
        );
    }
}
