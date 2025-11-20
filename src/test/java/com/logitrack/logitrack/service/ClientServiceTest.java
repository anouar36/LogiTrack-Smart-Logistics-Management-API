package com.logitrack.logitrack.service;

import com.logitrack.logitrack.entity.Client;
import com.logitrack.logitrack.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client mockClient;

    @BeforeEach
    void setUp() {
        mockClient = Client.builder()
                .id(1L)
                .name("Test Company")
                .build();
    }

    @Test
    @DisplayName("getClientById: Should return client when found")
    void getClientById_Success() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(mockClient));

        // When
        Client result = clientService.getClientById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Company", result.getName());
        verify(clientRepository).findById(1L);
    }

    @Test
    @DisplayName("getClientById: Should throw RuntimeException when not found")
    void getClientById_NotFound() {
        // Given
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientService.getClientById(99L));

        assertEquals("this client is not fount id:99", exception.getMessage());
        verify(clientRepository).findById(99L);
    }
}