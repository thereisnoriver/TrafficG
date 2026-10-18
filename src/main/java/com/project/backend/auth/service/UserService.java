package com.project.backend.auth.service;
import com.project.backend.auth.domain.User;
import com.project.backend.auth.dto.*;
import com.project.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    public User signUp(UserSignUpRequestDto userSignUpRequestDto) {
        // 1. 중복 ID 체크
        if (userRepository.findByAccountId(userSignUpRequestDto.getAccountId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account ID already exists");
        }

        String encodePassword = passwordEncoder.encode(userSignUpRequestDto.getPassword());

        User user = User.builder()
                        .accountId(userSignUpRequestDto.getAccountId())
                        .password(encodePassword)
                        .name(userSignUpRequestDto.getName())
                        .email(userSignUpRequestDto.getEmail())
                        .phoneNumber(userSignUpRequestDto.getPhoneNumber())
                        .address(userSignUpRequestDto.getAddress())
                .build();

        return userRepository.save(user);
    }

    public UserLoginResponseDto login(UserLoginRequestDto userLoginRequestDto) {
        String accountId = userLoginRequestDto.getAccountId();
        String password = userLoginRequestDto.getPassword();
        Optional<User> optionalUser = userRepository.findByAccountId(accountId);

        return  optionalUser.filter(user -> user.matchPassword(password, passwordEncoder))
                .map(user -> UserLoginResponseDto.builder()
                        .id(user.getId())
                        .accountId(user.getAccountId())
                        .name(user.getName())
                        .build())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid account ID or password"));
    }

    public UserLoginResponseDto currentUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired"));
        return UserLoginResponseDto.builder()
                .id(user.getId())
                .accountId(user.getAccountId())
                .name(user.getName())
                .build();
    }

}
