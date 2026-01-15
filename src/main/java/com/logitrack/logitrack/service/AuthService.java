package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Auth.RegisterDto;
import com.logitrack.logitrack.dto.ResClientDTO;
import com.logitrack.logitrack.dto.User.UserResponseDTO;
import com.logitrack.logitrack.entity.Client;
import com.logitrack.logitrack.entity.Role;
import com.logitrack.logitrack.entity.User;
import com.logitrack.logitrack.exception.UserAlreadyExistsException;
import com.logitrack.logitrack.repository.ClientRepository;
import com.logitrack.logitrack.repository.RoleRepository;
import com.logitrack.logitrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ResClientDTO register(RegisterDto dto) {
        // 1. التحقق من وجود الإيميل محلياً
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        // 2. جلب دور CLIENT
        Role clientRole = roleRepository.findByName(Role.RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Default role CLIENT not found. Please initialize roles."));

        Set<Role> roles = new HashSet<>();
        roles.add(clientRole);

        // 3. إنشاء المستخدم محلياً (Local DB)
        // ملاحظة: هذا يحفظ المستخدم في Postgres فقط لربطه بالـ Business Logic
        // يجب عليك أيضاً التأكد من أن المستخدم يتم إنشاؤه في Keycloak
        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPasswordHash())) // نحتفظ به مشفراً محلياً
                .active(true)
                .roles(roles)
                .build();

        userRepository.save(user);

        // 4. إنشاء الـ Client المرتبط بالمستخدم
        Client client = Client.builder()
                .name(dto.getName())
                .user(user)
                .build();

        clientRepository.save(client);

        // 5. إرجاع النتيجة
        UserResponseDTO userDTO = new UserResponseDTO(user.getEmail(), "[PROTECTED]", user.isActive());
        return new ResClientDTO(client.getId(), client.getName(), userDTO);
    }

    // تم حذف login, refreshToken, logout لأن AuthController أصبح يتكلف بها عبر Keycloak API
}