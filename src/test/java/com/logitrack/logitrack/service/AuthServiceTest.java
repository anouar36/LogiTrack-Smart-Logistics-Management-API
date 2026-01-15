package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Auth.RegisterDto;
import com.logitrack.logitrack.dto.ResClientDTO;
import com.logitrack.logitrack.entity.Client;
import com.logitrack.logitrack.entity.Role;
import com.logitrack.logitrack.entity.User;
import com.logitrack.logitrack.exception.UserAlreadyExistsException;
import com.logitrack.logitrack.repository.ClientRepository;
import com.logitrack.logitrack.repository.RoleRepository;
import com.logitrack.logitrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private Client mockClient;
    private Role clientRole;
    private RegisterDto registerDto;

    @BeforeEach
    void setUp() {
        // Role Setup
        clientRole = new Role();
        clientRole.setId(1L);
        clientRole.setName(Role.RoleType.CLIENT);

        Set<Role> roles = new HashSet<>();
        roles.add(clientRole);

        // User Setup
        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .active(true)
                .roles(roles)
                .build();

        // Client Setup
        mockClient = Client.builder()
                .id(1L)
                .name("Test Client")
                .user(mockUser)
                .build();

        // DTO Setup
        registerDto = new RegisterDto();
        registerDto.setEmail("test@example.com");
        registerDto.setPasswordHash("rawPassword");
        registerDto.setName("Test Client");
    }

    // --- Register Tests ---

    @Test
    @DisplayName("register: Should create User and Client when email is new")
    void register_Success() {
        // Given
        when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(false);
        when(roleRepository.findByName(Role.RoleType.CLIENT)).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode(registerDto.getPasswordHash())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);

        // When
        ResClientDTO result = authService.register(registerDto);

        // Then
        assertNotNull(result);
        assertEquals("Test Client", result.getName());
        assertEquals("test@example.com", result.getUser().getEmail());

        // Verify saves
        verify(userRepository).save(any(User.class));
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("register: Should throw Exception when email already exists")
    void register_UserExists() {
        // Given
        when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerDto));

        // Verify no saves occurred
        verify(userRepository, never()).save(any());
        verify(clientRepository, never()).save(any());
    }
}