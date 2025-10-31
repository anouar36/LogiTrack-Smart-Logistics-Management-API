package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public User existsByEmailAndPasswordHash(String email , String password);

}
