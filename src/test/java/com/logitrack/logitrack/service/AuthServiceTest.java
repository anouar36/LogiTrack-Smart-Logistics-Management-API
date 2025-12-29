package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Auth.AuthResponseDto;
import com.logitrack.logitrack.dto.Auth.JwtUserData;
import com.logitrack.logitrack.dto.Auth.RegisterDto;
import com.logitrack.logitrack.dto.LoginRequest;
import com.logitrack.logitrack.dto.ResClientDTO;
import com.logitrack.logitrack.entity.Client;
import com.logitrack.logitrack.entity.RefreshToken;
import com.logitrack.logitrack.entity.Role;
import com.logitrack.logitrack.entity.User;
import com.logitrack.logitrack.exception.AuthenticationException;
import com.logitrack.logitrack.exception.UserAlreadyExistsException;
import com.logitrack.logitrack.repository.ClientRepository;
import com.logitrack.logitrack.repository.RoleRepository;
import com.logitrack.logitrack.repository.UserRepository;
import com.logitrack.logitrack.security.CustomUserDetails;
import com.logitrack.logitrack.security.JwtService;
import com.logitrack.logitrack.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private Client mockClient;
    private Role clientRole;
    private RegisterDto registerDto;
    private LoginRequest loginRequest;
    private RefreshToken mockRefreshToken;

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

        // RefreshToken Setup
        mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("mock-refresh-token");
        mockRefreshToken.setUser(mockUser);

        // DTO Setup
        registerDto = new RegisterDto();
        registerDto.setEmail("test@example.com");
        registerDto.setPasswordHash("rawPassword");
        registerDto.setName("Test Client");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("rawPassword");
    }

    // --- Login Tests ---

    @Test
    @DisplayName("login: Should return AuthResponseDto when credentials are valid")
    void login_Success() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        Authentication authentication = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateAccessToken(any(JwtUserData.class))).thenReturn("mock-access-token");
        when(refreshTokenService.createRefreshToken(mockUser.getId())).thenReturn(mockRefreshToken);
        when(jwtService.getAccessTokenExpiration()).thenReturn(3600L);

        // When
        AuthResponseDto result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("mock-access-token", result.getAccessToken());
        assertEquals("mock-refresh-token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        verify(refreshTokenService).revokeAllUserTokens(mockUser);
    }

    @Test
    @DisplayName("login: Should throw AuthenticationException when credentials invalid")
    void login_Failure() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("login: Should throw AuthenticationException when user is inactive")
    void login_InactiveUser() {
        // Given
        mockUser.setActive(false);
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        Authentication authentication = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When & Then
        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));
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