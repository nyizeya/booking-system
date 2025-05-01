package co.codigo.bookingsystem.common.exceptions.handlers;

import co.codigo.bookingsystem.common.exceptions.*;
import com.codigo.bookingsystem.common.exceptions.*;
import co.codigo.bookingsystem.web.dtos.response.MessageResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

}