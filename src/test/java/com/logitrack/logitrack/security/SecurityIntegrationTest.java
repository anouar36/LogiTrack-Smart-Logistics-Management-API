package com.logitrack.logitrack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.logitrack.dto.Auth.AuthResponseDto;
import com.logitrack.logitrack.dto.Auth.RefreshTokenRequestDto;
import com.logitrack.logitrack.dto.Auth.RegisterDto;
import com.logitrack.logitrack.dto.LoginRequest;
import com.logitrack.logitrack.entity.Role;
import com.logitrack.logitrack.entity.User;
import com.logitrack.logitrack.repository.RefreshTokenRepository;
import com.logitrack.logitrack.repository.RoleRepository;
import com.logitrack.logitrack.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for security module
 * Tests authentication, refresh tokens, role-based authorization, and client data isolation
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // Initialize roles if not present
        for (Role.RoleType roleType : Role.RoleType.values()) {
            if (roleRepository.findByName(roleType).isEmpty()) {
                roleRepository.save(Role.builder().name(roleType).build());
            }
        }
    }

    // ==================== AUTHENTICATION TESTS ====================

    @Test
    @Order(1)
    @DisplayName("Should successfully login with valid credentials")
    void login_WithValidCredentials_ShouldReturnTokens() throws Exception {
        // Setup: Create test user
        createTestUser(TEST_EMAIL, TEST_PASSWORD, Role.RoleType.CLIENT);

        LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.roles[0]").value("CLIENT"));
    }

    @Test
    @Order(2)
    @DisplayName("Should reject login with invalid password")
    void login_WithInvalidPassword_ShouldReturn401() throws Exception {
        createTestUser(TEST_EMAIL, TEST_PASSWORD, Role.RoleType.CLIENT);

        LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, "wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("Should reject login with non-existent email")
    void login_WithNonExistentEmail_ShouldReturn401() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", TEST_PASSWORD);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== TOKEN ACCESS TESTS ====================

    @Test
    @Order(4)
    @DisplayName("Should access protected endpoint with valid token")
    void accessProtectedEndpoint_WithValidToken_ShouldSucceed() throws Exception {
        createTestUser("admin@example.com", TEST_PASSWORD, Role.RoleType.ADMIN);
        String accessToken = loginAndGetAccessToken("admin@example.com", TEST_PASSWORD);

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    @DisplayName("Should reject access to protected endpoint without token")
    void accessProtectedEndpoint_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @Order(6)
    @DisplayName("Should reject access with invalid token")
    void accessProtectedEndpoint_WithInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/inventory")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== REFRESH TOKEN TESTS ====================

    @Test
    @Order(7)
    @DisplayName("Should successfully refresh access token")
    void refreshToken_WithValidRefreshToken_ShouldReturnNewTokens() throws Exception {
        createTestUser(TEST_EMAIL, TEST_PASSWORD, Role.RoleType.CLIENT);
        AuthResponseDto authResponse = loginAndGetFullResponse(TEST_EMAIL, TEST_PASSWORD);

        RefreshTokenRequestDto refreshRequest = new RefreshTokenRequestDto(authResponse.getRefreshToken());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                // New refresh token should be different (rotation)
                .andExpect(jsonPath("$.refreshToken").value(org.hamcrest.Matchers.not(authResponse.getRefreshToken())));
    }

    @Test
    @Order(8)
    @DisplayName("Should reject revoked refresh token")
    void refreshToken_WithRevokedToken_ShouldReturn403() throws Exception {
        createTestUser(TEST_EMAIL, TEST_PASSWORD, Role.RoleType.CLIENT);
        AuthResponseDto authResponse = loginAndGetFullResponse(TEST_EMAIL, TEST_PASSWORD);

        // Logout to revoke the token
        RefreshTokenRequestDto logoutRequest = new RefreshTokenRequestDto(authResponse.getRefreshToken());
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());

        // Try to use revoked token
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isForbidden());
    }

    // ==================== ROLE-BASED AUTHORIZATION TESTS ====================    @Test
    @Order(9)
    @DisplayName("ADMIN should access admin-only endpoints")
    void adminEndpoint_WithAdminRole_ShouldSucceed() throws Exception {
        createTestUser("admin@example.com", TEST_PASSWORD, Role.RoleType.ADMIN);
        String accessToken = loginAndGetAccessToken("admin@example.com", TEST_PASSWORD);

        mockMvc.perform(get("/api/inventory")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    @DisplayName("CLIENT should be denied access to admin endpoints")
    void adminEndpoint_WithClientRole_ShouldReturn403() throws Exception {
        createTestUser("client@example.com", TEST_PASSWORD, Role.RoleType.CLIENT);
        String accessToken = loginAndGetAccessToken("client@example.com", TEST_PASSWORD);

        mockMvc.perform(get("/api/inventory")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @Order(11)
    @DisplayName("WAREHOUSE_MANAGER should access inventory endpoints")
    void inventoryEndpoint_WithWarehouseManagerRole_ShouldSucceed() throws Exception {
        createTestUser("manager@example.com", TEST_PASSWORD, Role.RoleType.WAREHOUSE_MANAGER);
        String accessToken = loginAndGetAccessToken("manager@example.com", TEST_PASSWORD);

        mockMvc.perform(get("/api/inventory")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(12)
    @DisplayName("CLIENT should be denied access to inventory endpoints")
    void inventoryEndpoint_WithClientRole_ShouldReturn403() throws Exception {
        createTestUser("client@example.com", TEST_PASSWORD, Role.RoleType.CLIENT);
        String accessToken = loginAndGetAccessToken("client@example.com", TEST_PASSWORD);

        mockMvc.perform(get("/api/inventory")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    @Order(13)
    @DisplayName("Should successfully register new user")
    void register_WithValidData_ShouldSucceed() throws Exception {
        RegisterDto registerDto = new RegisterDto("New User", "newuser@example.com", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New User"));
    }

    @Test
    @Order(14)
    @DisplayName("Should reject registration with existing email")
    void register_WithExistingEmail_ShouldReturn409() throws Exception {
        createTestUser(TEST_EMAIL, TEST_PASSWORD, Role.RoleType.CLIENT);

        RegisterDto registerDto = new RegisterDto("Duplicate User", TEST_EMAIL, "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isConflict());
    }

    // ==================== HELPER METHODS ====================

    private User createTestUser(String email, String password, Role.RoleType roleType) {
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleType));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .active(true)
                .roles(roles)
                .build();

        return userRepository.save(user);
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponseDto.class
        );

        return response.getAccessToken();
    }

    private AuthResponseDto loginAndGetFullResponse(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponseDto.class
        );
    }
}
