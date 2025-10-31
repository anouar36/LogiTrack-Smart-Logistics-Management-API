package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Auth.RegisterDto;
import com.logitrack.logitrack.dto.LoginRequest;
import com.logitrack.logitrack.dto.ResClientDTO;
import com.logitrack.logitrack.dto.User.UserResponseDTO;
import com.logitrack.logitrack.entity.Client;
import com.logitrack.logitrack.entity.User;
import com.logitrack.logitrack.mapper.LoginRequestMapper;
import com.logitrack.logitrack.repository.ClientRepository;
import com.logitrack.logitrack.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final LoginRequestMapper loginRequestMapper;
    private final UserRepository userRepository ;
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    public AuthService(LoginRequestMapper loginRequestMapper, UserRepository userRepository, ClientRepository clientRepository, ModelMapper modelMapper) {
        this.loginRequestMapper = loginRequestMapper;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.modelMapper = modelMapper;
    }

    public User login(LoginRequest loginRequest) {

        User user = loginRequestMapper.toEntity(loginRequest);
        return userRepository.existsByEmailAndPasswordHash(user.getEmail(), user.getPasswordHash());
    }

    public ResClientDTO register(RegisterDto dto){
        User user = User.builder().email(dto.getEmail()).passwordHash(dto.getPasswordHash()).active(true).build();
        userRepository.save(user);
        Client client = Client.builder().name(dto.getName()).user(user).build();
        clientRepository.save(client);
        UserResponseDTO userDTO = modelMapper.map(user, UserResponseDTO.class);
        ResClientDTO resClientDTO = new ResClientDTO(client.getId(), client.getName(), userDTO);
        return resClientDTO;







    }


}
