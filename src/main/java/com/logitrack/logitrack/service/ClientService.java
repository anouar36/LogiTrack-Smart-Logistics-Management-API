package com.logitrack.logitrack.service;

import com.logitrack.logitrack.entity.Client;
import com.logitrack.logitrack.repository.ClientRepository;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import org.hibernate.jdbc.Expectation;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;


    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow( () -> new RuntimeException("this client is not fount id:"+id));
    }

}
