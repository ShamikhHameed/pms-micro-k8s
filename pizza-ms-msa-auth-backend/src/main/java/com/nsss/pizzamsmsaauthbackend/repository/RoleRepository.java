package com.nsss.pizzamsmsaauthbackend.repository;

import com.nsss.pizzamsmsaauthbackend.model.ERole;
import com.nsss.pizzamsmsaauthbackend.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(ERole name);
}
