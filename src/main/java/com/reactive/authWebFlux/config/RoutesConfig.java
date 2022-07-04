package com.reactive.authWebFlux.config;

import com.reactive.authWebFlux.domain.User;
import com.reactive.authWebFlux.dto.UserDto;
import com.reactive.authWebFlux.service.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@AllArgsConstructor
@Configuration
public class RoutesConfig {

    private final ModelMapper modelMapper;
    private final UserService userService;

    @Bean
    RouterFunction<ServerResponse> views() {
        return RouterFunctions.route(
                        GET("/"),
                        (req) ->
                                ServerResponse
                                        .ok()
                                        .contentType(MediaType.TEXT_HTML)
                                        .render("home"))
                .and(RouterFunctions.route(
                        GET("/signup"),
                        (req) ->
                                ServerResponse
                                        .ok()
                                        .contentType(MediaType.TEXT_HTML)
                                        .render("signup")));
    }

    @Bean
    RouterFunction<ServerResponse> users() {
        return RouterFunctions.route(
                        GET("/api/users"),
                        (req) -> ServerResponse
                                .ok()
                                .body(userService.findAll().flatMap(usr ->
                                                Mono.just(modelMapper.map(usr, UserDto.class))
                                        ),
                                        UserDto.class)
                )
                .and(RouterFunctions.route(
                        GET("/api/users/{userId}"),
                        (req) -> ServerResponse
                                .ok()
                                .body(userService.findById(Long.parseLong(req.pathVariable("userId")))
                                        .flatMap(usr ->
                                                Mono.just(modelMapper.map(usr, UserDto.class))), UserDto.class)))
                .and(RouterFunctions.route(
                        POST("/api/users"),
                        (ServerRequest req) -> {
                            Mono<User> user = req.bodyToMono(User.class);
                            return ServerResponse
                                    .ok()
                                    .body(user.flatMap(userService::addUser)
                                            .flatMap(usr ->
                                                    Mono.just(modelMapper.map(usr, UserDto.class))), UserDto.class);
                        }));
    }


}