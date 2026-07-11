package org.tss.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tss.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}