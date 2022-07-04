package com.reactive.authWebFlux.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(final long id) {
        super("User not found for id=" + id);
    }
}
