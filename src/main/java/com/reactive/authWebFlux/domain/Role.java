package com.reactive.authWebFlux.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("role")
public class Role {

    @Id
    private Integer id;

    private String roleName;

}
