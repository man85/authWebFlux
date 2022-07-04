package com.reactive.authWebFlux.errorhandling;

import com.reactive.authWebFlux.exception.UserAlreadyExistsException;
import com.reactive.authWebFlux.exception.UserNotFoundException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> map = new LinkedHashMap<>();
        Throwable error = super.getError(request);
        HttpStatus errorStatus;
        if (error instanceof UserAlreadyExistsException) {
            errorStatus = HttpStatus.NOT_ACCEPTABLE;
        } else if (error instanceof UserNotFoundException) {
            errorStatus = HttpStatus.NOT_FOUND;
        } else {
            errorStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        map.put("status", errorStatus.value());
        map.put("message", error.getMessage());
        return map;
    }

}

