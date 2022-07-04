package com.reactive.authWebFlux.service;

import com.reactive.authWebFlux.domain.User;
import com.reactive.authWebFlux.exception.UserAlreadyExistsException;
import com.reactive.authWebFlux.exception.UserNotFoundException;
import com.reactive.authWebFlux.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
public class TestUserService {

    private static final Integer USER_TEST_COUNT = 10;
    private static final Long NON_EXISTENT_USER_ID = 0L;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    void testFindByUsernameNotFound() {
        Mockito.when(userRepository.findByUsernameWithQuery(anyString()))
                .thenReturn(Mono.empty());
        Mono<UserDetails> userMono = userService.findByUsername("nonExistentUser");
        StepVerifier.create(userMono)
                .expectErrorMatches(error -> error instanceof UsernameNotFoundException)
                .verify();
    }

    @Test
    void testFindByUsernameSuccessful() {
        User expectedUser = User.builder().username("user").build();
        Mockito.when(userRepository.findByUsernameWithQuery(eq("user")))
                .thenReturn(Mono.just(expectedUser));
        Mono<UserDetails> userMono = userService.findByUsername("user");
        StepVerifier.create(userMono)
                .expectNext((UserDetails) expectedUser)
                .verifyComplete();
    }

    @Test
    void testFindByIdNotFound() {
        Mockito.when(userRepository.findById(eq(NON_EXISTENT_USER_ID)))
                .thenReturn(Mono.empty());
        Mono<User> userMono = userService.findById(NON_EXISTENT_USER_ID);
        StepVerifier.create(userMono)
                .expectErrorMatches(error -> error instanceof UserNotFoundException
                        && error.getMessage().equals("User not found for id=" + NON_EXISTENT_USER_ID))
                .verify();
    }

    @Test
    void testFindByIdIdNotPresented() {
        Mono<User> userMono = userService.findById(null);
        StepVerifier.create(userMono)
                .expectErrorMatches(error -> error instanceof RuntimeException
                        && error.getMessage().equals("User id not presented"))
                .verify();
    }

    @Test
    void testFindByIdSuccessful() {
        final Long userId = 1L;
        User expectedUser = User.builder().id(userId).build();
        Mockito.when(userRepository.findById(eq(userId)))
                .thenReturn(Mono.just(expectedUser));
        Mono<User> userMono = userService.findById(userId);
        StepVerifier.create(userMono)
                .expectNext(expectedUser)
                .verifyComplete();
    }

    @Test
    void testFindAll() {
        Flux<User> expectedUsers = Flux
                .range(1, USER_TEST_COUNT)
                .flatMap(i -> Mono.just(Long.valueOf(i)))
                .flatMap(id -> Mono.just(User.builder()
                        .id(id)
                        .username("user" + id)
                        .build()));
        Mockito.when(userRepository.findAll())
                .thenReturn(expectedUsers);
        Flux<User> users = userService.findAll();
        StepVerifier.create(users)
                .recordWith(ArrayList::new)
                .expectNextCount(USER_TEST_COUNT)
                .expectRecordedMatches(usrs -> usrs.stream()
                        .allMatch(user ->
                                user.getUsername()
                                        .contains(user.getId().toString())))
                .verifyComplete();
    }

    @Test
    void testAddUserSuccessful() {
        final Long userId = 1L;
        User user = User.builder().id(userId).build();
        Mockito.when(userRepository.findByUsernameWithQuery(eq(user.getUsername())))
                .thenReturn(Mono.empty());
        Mockito.when(userRepository.save(eq(user)))
                .thenReturn(Mono.just(user));
        Mockito.when(passwordEncoder.encode(anyString())).thenReturn(anyString());
        Mono<User> addedUser = userService.addUser(user);
        StepVerifier.create(addedUser)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void testAddUserUserAlreadyExists() {
        final Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .username("user")
                .build();
        Mockito.when(userRepository.findByUsernameWithQuery(eq(user.getUsername())))
                .thenReturn(Mono.just(user));
        Mono<User> addedUser = userService.addUser(user);
        StepVerifier.create(addedUser)
                .expectErrorMatches(error -> error instanceof UserAlreadyExistsException
                        && error.getMessage().equals("User \"user\" already exists"))
                .verify();
    }


}

