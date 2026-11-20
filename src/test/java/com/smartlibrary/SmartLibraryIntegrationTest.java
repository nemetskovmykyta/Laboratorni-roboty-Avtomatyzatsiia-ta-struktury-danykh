package com.smartlibrary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlibrary.dto.request.BookCreateRequest;
import com.smartlibrary.dto.request.LoanCreateRequest;
import com.smartlibrary.dto.request.ReaderCreateRequest;
import com.smartlibrary.entity.BookStatus;
import com.smartlibrary.entity.ReaderReliabilityStatus;
import com.smartlibrary.repository.BookRepository;
import com.smartlibrary.repository.LoanRepository;
import com.smartlibrary.repository.ReaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SmartLibraryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        readerRepository.deleteAll();
    }

    @Test
    @DisplayName("Повний цикл: реєстрація читача, створення книги, видача, перевірка активних позик, повернення")
    void fullLibraryWorkflowTest() throws Exception {
        // 1. Створення читача: POST /api/readers
        ReaderCreateRequest readerReq = new ReaderCreateRequest("Читач Тестовий", "test.user@example.com", "+380501234567");
        String readerJson = mockMvc.perform(post("/api/readers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(readerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name", is("Читач Тестовий")))
                .andExpect(jsonPath("$.email", is("test.user@example.com")))
                .andReturn().getResponse().getContentAsString();

        Long readerId = objectMapper.readTree(readerJson).get("id").asLong();

        // 2. Створення дубліката читача повертає 409 Conflict
        mockMvc.perform(post("/api/readers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(readerReq)))
                .andExpect(status().isConflict());

        // 3. Створення книги: POST /api/books
        BookCreateRequest bookReq = new BookCreateRequest("Алгоритми та структури", "Автор Тестовий", "ISBN-001");
        String bookJson = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title", is("Алгоритми та структури")))
                .andExpect(jsonPath("$.status", is("AVAILABLE")))
                .andExpect(jsonPath("$.available", is(true)))
                .andReturn().getResponse().getContentAsString();

        Long bookId = objectMapper.readTree(bookJson).get("id").asLong();

        // 4. Отримання списку книг: GET /api/books
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookId.intValue())));

        // 5. Перевірка надійності нового читача: GET /api/readers/{readerId}/reliability
        mockMvc.perform(get("/api/readers/{readerId}/reliability", readerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readerId", is(readerId.intValue())))
                .andExpect(jsonPath("$.status", is(ReaderReliabilityStatus.REGULAR.name())))
                .andExpect(jsonPath("$.reliable", is(false)))
                .andExpect(jsonPath("$.maxAllowedLoans", is(2)))
                .andExpect(jsonPath("$.currentActiveLoansCount", is(0)));

        // 6. Видача книги: POST /api/loans
        LoanCreateRequest loanReq = new LoanCreateRequest(readerId, bookId);
        String loanJson = mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.readerId", is(readerId.intValue())))
                .andExpect(jsonPath("$.bookId", is(bookId.intValue())))
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.returnDate").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        Long loanId = objectMapper.readTree(loanJson).get("id").asLong();

        // 7. Спроба видати ту саму книгу ще раз повертає помилку 400
        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("недоступна")));

        // 8. Отримання активних позик читача: GET /api/readers/{readerId}/loans/active
        mockMvc.perform(get("/api/readers/{readerId}/loans/active", readerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(loanId.intValue())))
                .andExpect(jsonPath("$[0].active", is(true)));

        // 9. Повернення книги: POST /api/loans/{loanId}/return
        mockMvc.perform(post("/api/loans/{loanId}/return", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(loanId.intValue())))
                .andExpect(jsonPath("$.active", is(false)))
                .andExpect(jsonPath("$.returnDate").isNotEmpty());

        // 10. Активні позики після повернення мають стати порожніми
        mockMvc.perform(get("/api/readers/{readerId}/loans/active", readerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 11. Книга знову доступна для видачі в списку книг
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is(BookStatus.AVAILABLE.name())))
                .andExpect(jsonPath("$[0].available", is(true)));

        // 12. Повторне повернення книги викликає помилку 400
        mockMvc.perform(post("/api/loans/{loanId}/return", loanId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("вже була повернена")));
    }

    @Test
    @DisplayName("Валідація вхідних даних повертає 400 з картою помилок")
    void validationErrorsTest() throws Exception {
        // Порожній читач
        ReaderCreateRequest invalidReader = new ReaderCreateRequest("", "invalid-email", null);
        mockMvc.perform(post("/api/readers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReader)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());

        // Порожня книга
        BookCreateRequest invalidBook = new BookCreateRequest("", "  ", null);
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.author").exists());
    }
}
