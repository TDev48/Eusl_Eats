package com.eusleats.eusleats.service;

import com.eusleats.eusleats.dto.LoginRequest;
import com.eusleats.eusleats.entity.User;
import com.eusleats.eusleats.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentLoginService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public StudentLoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(LoginRequest request) {

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid email or password");
        }

        // Make sure this is a student account
        if (user.getRole() != User.Role.STUDENT) {
            throw new RuntimeException("This account is not a student account");
        }

        return user;
    }
}