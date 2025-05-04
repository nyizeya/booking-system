package co.codigo.bookingsystem.common.exceptions.handlers;

import co.codigo.bookingsystem.common.exceptions.*;
import co.codigo.bookingsystem.web.dtos.response.MessageResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<MessageResponseDTO> handleInvalidOperationException(InvalidOperationException e) {
        return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> errorList = new ArrayList<>();
        e.getBindingResult().getAllErrors().forEach(error -> errorList.add(error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(new MessageResponseDTO(errorList.get(0)));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<MessageResponseDTO> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<MessageResponseDTO> handleBusinessRuleException(BusinessRuleException e) {
        return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(ConcurrencyException.class)
    public ResponseEntity<MessageResponseDTO> handleConcurrencyException(ConcurrencyException e) {
        return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<MessageResponseDTO> handleConflictException(ConflictException e) {
        return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(InsufficientCreditsException.class)
    public ResponseEntity<MessageResponseDTO> handleInsufficientCreditsException(InsufficientCreditsException e) {
        return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<MessageResponseDTO> handleInsufficientBalanceException(InsufficientBalanceException e) {
        return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MessageResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Database constraint violation: " + ex.getMessage();
        return ResponseEntity.badRequest().body(new MessageResponseDTO(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body("Validation failed: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.internalServerError().body("Internal server error: " + ex.getMessage());
    }

    private String getConstraintMessage(DataIntegrityViolationException ex) {
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            return ex.getCause().getMessage();
        }
        return "Unknown database constraint violation.";
    }

}