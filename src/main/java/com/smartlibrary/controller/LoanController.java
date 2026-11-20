package com.smartlibrary.controller;

import com.smartlibrary.dto.request.LoanCreateRequest;
import com.smartlibrary.dto.response.LoanResponse;
import com.smartlibrary.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loans", description = "Управління позиками та поверненням книг")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    @Operation(summary = "Видача книги", description = "Видає книгу читачу на 14 днів, якщо виконуються всі бізнес-правила (книга доступна, ліміт не перевищено).")
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanCreateRequest request) {
        LoanResponse response = loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{loanId}/return")
    @Operation(summary = "Повернення книги", description = "Реєструє повернення книги: фіксує дату повернення та змінює статус книги на AVAILABLE.")
    public ResponseEntity<LoanResponse> returnBook(@PathVariable Long loanId) {
        LoanResponse response = loanService.returnBook(loanId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{loanId}")
    @Operation(summary = "Отримання позики за ідентифікатором")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanById(loanId));
    }

    @GetMapping
    @Operation(summary = "Отримання списку всіх позик")
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }
}
