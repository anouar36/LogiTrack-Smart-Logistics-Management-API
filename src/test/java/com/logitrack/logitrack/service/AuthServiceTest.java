package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Auth.RegisterDto;
import com.logitrack.logitrack.dto.LoginRequest;
import com.logitrack.logitrack.dto.ResClientDTO;
import com.logitrack.logitrack.entity.Client;
import com.logitrack.logitrack.entity.User;
import com.logitrack.logitrack.exception.UserAlreadyExistsException;
import com.logitrack.logitrack.mapper.LoginRequestMapper;
import com.logitrack.logitrack.repository.ClientRepository;
import com.logitrack.logitrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private LoginRequestMapper loginRequestMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private Client mockClient;
    private RegisterDto registerDto;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // User Setup
        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .active(true)
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
        registerDto.setPasswordHash("hashedPassword");
        registerDto.setName("Test Client");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPasswordHash("hashedPassword");
    }

    // --- Login Tests ---

    @Test
    @DisplayName("login: Should return User when credentials are valid")
    void login_Success() {
        // Given
        when(loginRequestMapper.toEntity(loginRequest)).thenReturn(mockUser);
        // Assuming your repository returns the User object based on the Service return type
        when(userRepository.existsByEmailAndPasswordHash(mockUser.getEmail(), mockUser.getPasswordHash()))
                .thenReturn(mockUser);

        // When
        User result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).existsByEmailAndPasswordHash(mockUser.getEmail(), mockUser.getPasswordHash());
    }

    @Test
    @DisplayName("login: Should return null (or throw) when credentials invalid")
    void login_Failure() {
        // Given
        when(loginRequestMapper.toEntity(loginRequest)).thenReturn(mockUser);
        when(userRepository.existsByEmailAndPasswordHash(any(), any())).thenReturn(null);

        // When
        User result = authService.login(loginRequest);

        // Then
        assertNull(result);
    }

    // --- Register Tests ---

    @Test
    @DisplayName("register: Should create User and Client when email is new")
    void register_Success() {
        // Given
        when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(false);
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