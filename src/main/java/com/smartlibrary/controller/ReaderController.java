package com.smartlibrary.controller;

import com.smartlibrary.dto.request.ReaderCreateRequest;
import com.smartlibrary.dto.response.LoanResponse;
import com.smartlibrary.dto.response.ReaderResponse;
import com.smartlibrary.dto.response.ReliabilityResponse;
import com.smartlibrary.service.LoanService;
import com.smartlibrary.service.ReaderService;
import com.smartlibrary.service.ReliabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readers")
@Tag(name = "Readers", description = "Управління читачами бібліотеки")
public class ReaderController {

    private final ReaderService readerService;
    private final ReliabilityService reliabilityService;
    private final LoanService loanService;

    public ReaderController(
            ReaderService readerService,
            ReliabilityService reliabilityService,
            LoanService loanService
    ) {
        this.readerService = readerService;
        this.reliabilityService = reliabilityService;
        this.loanService = loanService;
    }

    @PostMapping
    @Operation(summary = "Створення нового читача", description = "Реєструє нового читача у системі. Email повинен бути унікальним.")
    public ResponseEntity<ReaderResponse> createReader(@Valid @RequestBody ReaderCreateRequest request) {
        ReaderResponse response = readerService.createReader(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Отримання списку всіх читачів")
    public ResponseEntity<List<ReaderResponse>> getAllReaders() {
        return ResponseEntity.ok(readerService.getAllReaders());
    }

    @GetMapping("/{readerId}")
    @Operation(summary = "Отримання читача за ідентифікатором")
    public ResponseEntity<ReaderResponse> getReaderById(@PathVariable Long readerId) {
        return ResponseEntity.ok(readerService.getReaderById(readerId));
    }

    @GetMapping("/{readerId}/reliability")
    @Operation(summary = "Перевірка надійності читача", description = "Повертає актуальну інформацію про рівень надійності читача (REGULAR / RELIABLE) на основі аналізу останніх 30 днів.")
    public ResponseEntity<ReliabilityResponse> getReaderReliability(@PathVariable Long readerId) {
        ReliabilityResponse response = reliabilityService.evaluateReliability(readerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{readerId}/loans/active")
    @Operation(summary = "Отримання активних позик читача", description = "Повертає всі активні (ще не повернуті) позики конкретного читача.")
    public ResponseEntity<List<LoanResponse>> getActiveLoansByReader(@PathVariable Long readerId) {
        List<LoanResponse> activeLoans = loanService.getActiveLoansByReader(readerId);
        return ResponseEntity.ok(activeLoans);
    }
}
