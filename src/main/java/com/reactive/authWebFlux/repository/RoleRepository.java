package com.reactive.authWebFlux.repository;

import com.reactive.authWebFlux.domain.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RoleRepository extends ReactiveCrudRepository<Role, Integer> {
}
