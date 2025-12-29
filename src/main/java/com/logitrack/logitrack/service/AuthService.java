package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Auth.AuthResponseDto;
import com.logitrack.logitrack.dto.Auth.JwtUserData;
import com.logitrack.logitrack.dto.Auth.RefreshTokenRequestDto;
import com.logitrack.logitrack.dto.Auth.RegisterDto;
import com.logitrack.logitrack.dto.LoginRequest;
import com.logitrack.logitrack.dto.ResClientDTO;
import com.logitrack.logitrack.dto.User.UserResponseDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDto login(LoginRequest loginRequest) {
        try {
            // Authenticate user with email and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Check if user is active
            if (!user.isActive()) {
                throw new AuthenticationException("User account is deactivated");
            }

            // Revoke all existing refresh tokens for this user
            refreshTokenService.revokeAllUserTokens(user);

            // Generate new tokens
            JwtUserData jwtUserData = new JwtUserData(user);

            String accessToken = jwtService.generateAccessToken(jwtUserData);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

            // Get user roles
            List<String> roles = user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList());

            return AuthResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getAccessTokenExpiration())
                    .userId(user.getId())
                    .email(user.getEmail())
                    .roles(roles)
                    .build();

        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Invalid email or password");
        }
    }

    @Transactional
    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        
        // Verify the refresh token is not expired or revoked
        refreshTokenService.verifyExpiration(refreshToken);
        
        User user = refreshToken.getUser();
        
        // Check if user is still active
        if (!user.isActive()) {
            throw new AuthenticationException("User account is deactivated");
        }
        
        // Rotate refresh token (revoke old one, create new one)
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
        
        // Generate new access token
        CustomUserDetails userDetails = new CustomUserDetails(user);
        JwtUserData jwtUserData = new JwtUserData(user);
        String accessToken = jwtService.generateAccessToken(jwtUserData);
        
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    @Transactional
    public ResClientDTO register(RegisterDto dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        // Get CLIENT role
        Role clientRole = roleRepository.findByName(Role.RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Default role CLIENT not found. Please initialize roles."));

        Set<Role> roles = new HashSet<>();
        roles.add(clientRole);

        // Create user with encrypted password
        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPasswordHash()))
                .active(true)
                .roles(roles)
                .build();

        userRepository.save(user);

        // Create client
        Client client = Client.builder()
                .name(dto.getName())
                .user(user)
                .build();

        clientRepository.save(client);

        UserResponseDTO userDTO = new UserResponseDTO(user.getEmail(), "[PROTECTED]", user.isActive());

        return new ResClientDTO(client.getId(), client.getName(), userDTO);
    }
}
