package com.reactive.authWebFlux.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        return httpSecurity
                .formLogin().and()
                .httpBasic().disable()
                .authorizeExchange()
                .pathMatchers("/", "/login", "/signup").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/users").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")
                .anyExchange().authenticated()
                .and()
                .build();
    }
}
