package com.reactive.authWebFlux.repository;

import com.reactive.authWebFlux.domain.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    @Query("select u.*,r.role_name as role from users u join roles r on u.role_id = r.id " +
            "where LOWER(u.username)=LOWER(:username)")
    Mono<User> findByUsernameWithQuery(@Param("username") String username);
}
