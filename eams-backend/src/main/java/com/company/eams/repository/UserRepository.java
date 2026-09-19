package com.company.eams.repository;

import com.company.eams.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}
