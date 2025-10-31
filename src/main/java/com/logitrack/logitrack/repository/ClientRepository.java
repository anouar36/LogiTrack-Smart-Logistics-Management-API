package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client , Long> {
}
