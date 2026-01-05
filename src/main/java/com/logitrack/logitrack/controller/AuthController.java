package com.logitrack.logitrack.controller;

import com.logitrack.logitrack.dto.Auth.RefreshTokenRequestDto;
import com.logitrack.logitrack.dto.Auth.RegisterDto;
import com.logitrack.logitrack.dto.LoginRequest;
import com.logitrack.logitrack.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final String KEYCLOAK_BASE_URL = "http://keycloak:8080";
    private final String KEYCLOAK_TOKEN_URL = KEYCLOAK_BASE_URL + "/realms/logitrack-realm/protocol/openid-connect/token";
    private final String KEYCLOAK_LOGOUT_URL = KEYCLOAK_BASE_URL + "/realms/logitrack-realm/protocol/openid-connect/logout";

    private final String CLIENT_ID = "logitrack-backend";
    private final String CLIENT_SECRET = "Fjv2LcxbXeQPTNIbagQuBzzBinhHdXQi";

    private final AuthService authService;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", CLIENT_ID);
        form.add("client_secret", CLIENT_SECRET);
        form.add("username", loginRequest.getUsername());
        form.add("password", loginRequest.getPassword());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(KEYCLOAK_TOKEN_URL, entity, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", "Unauthorized", "message", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Service Error", "message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDto registerDto) {
        try {
            return ResponseEntity.ok(authService.register(registerDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Registration Failed", "message", e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", CLIENT_ID);
        form.add("client_secret", CLIENT_SECRET);
        form.add("refresh_token", request.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(KEYCLOAK_TOKEN_URL, entity, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Invalid Refresh Token", "message", "Session expired, please login again"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Service Error", "message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh Token is required"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", CLIENT_ID);
        form.add("client_secret", CLIENT_SECRET);
        form.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        try {
            restTemplate.postForEntity(KEYCLOAK_LOGOUT_URL, entity, Map.class);
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Logout Failed", "message", e.getMessage()));
        }
    }
}