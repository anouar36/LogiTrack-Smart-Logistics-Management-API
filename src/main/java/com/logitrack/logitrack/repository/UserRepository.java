package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User existsByEmailAndPasswordHash(String email, String password);

    boolean existsByEmail(@NotBlank @Email String email);
    
    Optional<User> findByEmail(String email);
}
