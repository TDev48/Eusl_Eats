package com.eusleats.eusleats.service;

import com.eusleats.eusleats.dto.StoreLoginRequest;
import com.eusleats.eusleats.entity.User;
import com.eusleats.eusleats.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StoreLoginService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public StoreLoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(StoreLoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        ));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        if (user.getRole() != User.Role.STORE) {

            throw new RuntimeException(
                    "This account is not a store account"
            );
        }

        return user;
    }
}