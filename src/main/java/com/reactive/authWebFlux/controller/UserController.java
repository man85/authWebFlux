package com.reactive.authWebFlux.controller;

import com.reactive.authWebFlux.domain.User;
import com.reactive.authWebFlux.dto.UserDto;
import com.reactive.authWebFlux.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ModelMapper modelMapper;
    private final UserService userService;

    @GetMapping
    Flux<UserDto> getUsers() {
        Flux<UserDto> users = userService.findAll().doOnNext((el) ->
                        log.info(Thread.currentThread() + " Getting users " +
                                el.getUsername() + " " + Instant.now()))
                .flatMap(usr -> Mono.just(modelMapper.map(usr, UserDto.class)));
        log.info(Thread.currentThread() + " Returning from getUsers..." + Instant.now());
        return users;
    }

    @GetMapping("/{userId}")
    Mono<UserDto> getUser(@PathVariable("userId") Long userId) {
        Mono<UserDto> user = userService.findById(userId).doOnNext((el) ->
                        log.info(Thread.currentThread() + " Getting user "
                                + el.getUsername() + " " + Instant.now()))
                .flatMap(usr -> Mono.just(modelMapper.map(usr, UserDto.class)));
        log.info(Thread.currentThread() + " Returning from getUser..." + Instant.now());
        return user;
    }

    @PostMapping
    Mono<ResponseEntity<Object>> addUser(@RequestBody User user) {
        return userService.addUser(user)
                .map(usr -> ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(usr, UserDto.class)));
    }

}
