package com.eusleats.eusleats.service;

import com.eusleats.eusleats.dto.StoreRegistrationRequest;
import com.eusleats.eusleats.entity.FoodStore;
import com.eusleats.eusleats.entity.User;
import com.eusleats.eusleats.repository.FoodStoreRepository;
import com.eusleats.eusleats.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StoreRegistrationService {

    private final UserRepository userRepository;
    private final FoodStoreRepository foodStoreRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public StoreRegistrationService(
            UserRepository userRepository,
            FoodStoreRepository foodStoreRepository) {

        this.userRepository = userRepository;
        this.foodStoreRepository = foodStoreRepository;
    }

    public FoodStore registerStore(StoreRegistrationRequest request) {

        if (request.getFullName() == null || request.getFullName().isBlank()
                || request.getStoreName() == null || request.getStoreName().isBlank()) {
            throw new RuntimeException("Owner name and store name are required");
        }

        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new RuntimeException("Please enter a valid email address");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setPhone(request.getPhone());
        user.setRole(User.Role.STORE);

        User savedUser = userRepository.save(user);

        FoodStore foodStore = new FoodStore();

        foodStore.setUser(savedUser);
        foodStore.setStoreName(request.getStoreName());
        foodStore.setLocation(request.getLocation());
        foodStore.setDescription(request.getDescription());

        foodStore.setApprovalStatus(
                FoodStore.ApprovalStatus.PENDING
        );

        return foodStoreRepository.save(foodStore);
    }
}