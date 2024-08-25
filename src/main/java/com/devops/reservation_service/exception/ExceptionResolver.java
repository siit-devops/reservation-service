package com.devops.reservation_service.exception;

import feign.FeignException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionResolver {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> badRequestException(BadRequestException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(exception.getMessage(), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> notFoundException(NotFoundException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(exception.getMessage(), headers, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<?> feignException(FeignException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return switch (exception.status()) {
            case 400 -> new ResponseEntity<>(exception.getMessage(), headers, HttpStatus.BAD_REQUEST);
            case 401 -> new ResponseEntity<>(exception.getMessage(), headers, HttpStatus.UNAUTHORIZED);
            case 404 -> new ResponseEntity<>(exception.getMessage(), headers, HttpStatus.NOT_FOUND);
            default -> new ResponseEntity<>(exception.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }
//
//    @ExceptionHandler(InternalServerException.class)
//    public ResponseEntity<?> internalServerException(InternalServerException exception) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.TEXT_PLAIN);
//        return new ResponseEntity<>(exception.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
}
