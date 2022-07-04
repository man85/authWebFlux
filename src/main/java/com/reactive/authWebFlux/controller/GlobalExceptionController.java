package com.reactive.authWebFlux.controller;

import com.reactive.authWebFlux.dto.ErrorDto;
import com.reactive.authWebFlux.exception.UserAlreadyExistsException;
import com.reactive.authWebFlux.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionController {


    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ErrorDto> handleUserNotFoundException(Exception ex) {
        return Mono.just(new ErrorDto(ex.getMessage()));
    }

    @ExceptionHandler({UserAlreadyExistsException.class})
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public Mono<ErrorDto> handleUserAlreadyExistsException(Exception ex) {
        return Mono.just(new ErrorDto(ex.getMessage()));
    }

}
