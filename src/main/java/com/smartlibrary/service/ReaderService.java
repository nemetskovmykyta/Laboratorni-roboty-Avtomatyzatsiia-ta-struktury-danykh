package com.smartlibrary.service;

import com.smartlibrary.dto.request.ReaderCreateRequest;
import com.smartlibrary.dto.response.ReaderResponse;
import com.smartlibrary.entity.Reader;
import com.smartlibrary.exception.EmailAlreadyExistsException;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.repository.ReaderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReaderService {

    private final ReaderRepository readerRepository;

    public ReaderService(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
    }

    @Transactional
    public ReaderResponse createReader(ReaderCreateRequest request) {
        if (readerRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new EmailAlreadyExistsException("Читач з email '" + request.email() + "' вже зареєстрований у системі");
        }

        Reader reader = new Reader(
                request.name().trim(),
                request.email().trim().toLowerCase(),
                request.phone() != null ? request.phone().trim() : null
        );

        Reader saved = readerRepository.save(reader);
        return ReaderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Reader findEntityById(Long id) {
        return readerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Читача з ID " + id + " не знайдено"));
    }

    @Transactional(readOnly = true)
    public ReaderResponse getReaderById(Long id) {
        return ReaderResponse.from(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<ReaderResponse> getAllReaders() {
        return readerRepository.findAll().stream()
                .map(ReaderResponse::from)
                .toList();
    }
}
