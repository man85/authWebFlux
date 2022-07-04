package com.reactive.authWebFlux.service;


import com.reactive.authWebFlux.domain.User;
import com.reactive.authWebFlux.exception.UserAlreadyExistsException;
import com.reactive.authWebFlux.exception.UserNotFoundException;
import com.reactive.authWebFlux.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class UserService implements ReactiveUserDetailsService {

    private static final int USER_ROLE_ID = 1;

    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsernameWithQuery(username).switchIfEmpty(
                Mono.error(new UsernameNotFoundException(username))
        ).cast(UserDetails.class);
    }

    public Mono<User> findById(Long userId) {
        return userId == null ? Mono.error(new RuntimeException("User id not presented")) :
                userRepository.findById(userId).switchIfEmpty(Mono.error(new UserNotFoundException(userId)));
    }

    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    public Mono<User> addUser(final User user) {
        user.setRoleId(USER_ROLE_ID);
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.findByUsernameWithQuery(user.getUsername()).flatMap((el) ->
                Mono.<User>error(new UserAlreadyExistsException(user.getUsername()))
        ).switchIfEmpty(
                Mono.defer(() -> userRepository.save(user))
        );
    }
}
