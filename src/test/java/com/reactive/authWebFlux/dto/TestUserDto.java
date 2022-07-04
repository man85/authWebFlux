package com.reactive.authWebFlux.dto;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TestUserDto {
    private Long id;
    private String username;
    private String password;
}
