package com.reactive.authWebFlux.controller;

import com.reactive.authWebFlux.config.MapperConfig;
import com.reactive.authWebFlux.config.WebSecurityConfig;
import com.reactive.authWebFlux.domain.User;
import com.reactive.authWebFlux.dto.TestUserDto;
import com.reactive.authWebFlux.dto.UserDto;
import com.reactive.authWebFlux.errorhandling.GlobalErrorAttributes;
import com.reactive.authWebFlux.errorhandling.GlobalErrorWebExceptionHandler;
import com.reactive.authWebFlux.repository.UserRepository;
import com.reactive.authWebFlux.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = UserController.class)
@Import({UserService.class, MapperConfig.class, WebSecurityConfig.class,
        GlobalErrorWebExceptionHandler.class, GlobalErrorAttributes.class})
public class UserControllerTest {

    private static final Integer USER_TEST_COUNT = 10;
    private static final Long NON_EXISTENT_USER_ID = 0L;

    @MockBean
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private WebTestClient webClient;

    private Mono<User> createUserWithId(final Long id) {
        return id.equals(NON_EXISTENT_USER_ID)
                ? Mono.empty()
                : Mono.just(User.builder()
                .id(id)
                .username("user" + id)
                .build());
    }

    @BeforeEach
    void initUserRepository() {
        Mockito.when(userRepository.findById(anyLong()))
                .thenAnswer(invoke -> createUserWithId(invoke.getArgument(0)));
        Mockito.when(userRepository.findAll())
                .thenReturn(Flux
                        .range(1, USER_TEST_COUNT)
                        .flatMap(i -> Mono.just(Long.valueOf(i)))
                        .flatMap(this::createUserWithId));
        Mockito.when(userRepository.findByUsernameWithQuery(anyString()))
                .thenAnswer(invock ->
                        invock.getArgument(0).toString().equals("user")
                                ? Mono.just(
                                User.builder()
                                        .username("user")
                                        .password(passwordEncoder.encode("password"))
                                        .role("ROLE_USER")
                                        .build())
                                : Mono.empty());
        Mockito.when(userRepository.save(any(User.class)))
                .thenAnswer(invoke -> Mono.just(invoke.getArgument(0)));

    }

    @Test
    void testRedirectToLoginUnauthorized() {
        webClient
                .get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    void testLoginForNonExistentUser() {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("username", "user1");
        bodyValues.add("password", "password");

        webClient
                .mutateWith(csrf())
                .post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(bodyValues))
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login?error");
    }

    @Test
    void testFailLoginWithWrongPassword() {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("username", "user");
        bodyValues.add("password", "wrong_password");

        webClient
                .mutateWith(csrf())
                .post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(bodyValues))
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login?error");
    }

    @Test
    void testSuccessfulLogin() {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("username", "user");
        bodyValues.add("password", "password");

        webClient
                .mutateWith(csrf())
                .post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(bodyValues))
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetUsersForAdmin() {
        webClient
                .get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDto.class)
                .hasSize(USER_TEST_COUNT)
                .value(users -> users.stream()
                                .allMatch(user ->
                                        user.getUsername()
                                                .contains(user.getId()
                                                        .toString())),
                        equalTo(true));
    }

    @Test
    @WithMockUser(username = "some_user", roles = {"USER"})
    void testDeniedGetUsersForUser() {
        webClient
                .get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUserNotFound() {
        webClient
                .get()
                .uri("/api/users/" + NON_EXISTENT_USER_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.message").isEqualTo(
                        "User not found for id=" + NON_EXISTENT_USER_ID)
                .jsonPath("$.status").isNotEmpty()
                .jsonPath("$.status").isEqualTo("404");
    }

    @Test
    @WithMockUser(username = "some_user", roles = {"USER"})
    void testDeniedGetUserForUser() {
        webClient
                .get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetUserForAdmin() {
        webClient
                .get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .value(UserDto::getId, equalTo(1L))
                .value(UserDto::getUsername, equalTo("user1"));
    }

    @Test
    void testSignupUserAlreadyExists() {
        webClient
                .mutateWith(csrf())
                .post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(TestUserDto.builder()
                        .username("user")
                        .password("password")
                        .build()), User.class)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody()
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.message").isEqualTo("User \"user\" already exists")
                .jsonPath("$.status").isNotEmpty()
                .jsonPath("$.status").isEqualTo("406");

    }

    @Test
    void testSignupSuccessfully() {
        TestUserDto user = TestUserDto.builder()
                .username("user1")
                .password("password")
                .build();

        webClient
                .mutateWith(csrf())
                .post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(user), User.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .isEqualTo(modelMapper.map(user, UserDto.class));
    }

}
