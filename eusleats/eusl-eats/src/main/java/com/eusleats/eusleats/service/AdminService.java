package com.eusleats.eusleats.service;

import com.eusleats.eusleats.dto.AdminStoreResponse;
import com.eusleats.eusleats.entity.FoodStore;
import com.eusleats.eusleats.entity.User;
import com.eusleats.eusleats.repository.FoodStoreRepository;
import com.eusleats.eusleats.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final FoodStoreRepository foodStoreRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AdminService(
            UserRepository userRepository,
            FoodStoreRepository foodStoreRepository) {

        this.userRepository = userRepository;
        this.foodStoreRepository = foodStoreRepository;
    }

    public User login(String email, String password) {

        User user = userRepository.findByEmail(email == null ? "" : email)
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        if (password == null
                || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("This account is not an admin account");
        }

        return user;
    }

    /** status = null or "ALL" returns every store. */
    public List<AdminStoreResponse> listStores(String status) {

        List<FoodStore> stores;

        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            stores = foodStoreRepository.findAll();
        } else {
            stores = foodStoreRepository.findByApprovalStatus(parseStatus(status));
        }

        return stores.stream().map(this::toResponse).toList();
    }

    public AdminStoreResponse updateStoreStatus(Integer storeId, String status) {

        FoodStore store = foodStoreRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        store.setApprovalStatus(parseStatus(status));

        return toResponse(foodStoreRepository.save(store));
    }

    private FoodStore.ApprovalStatus parseStatus(String status) {

        try {
            return FoodStore.ApprovalStatus.valueOf(
                    status == null ? "" : status.trim().toUpperCase());

        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Status must be PENDING, APPROVED or REJECTED");
        }
    }

    private AdminStoreResponse toResponse(FoodStore store) {

        User owner = store.getUser();

        return new AdminStoreResponse(
                store.getStoreId(),
                store.getStoreName(),
                store.getLocation(),
                store.getDescription(),
                store.getApprovalStatus() == null
                        ? "PENDING"
                        : store.getApprovalStatus().name(),
                owner == null ? "" : owner.getFullName(),
                owner == null ? "" : owner.getEmail(),
                owner == null ? "" : owner.getPhone()
        );
    }
}
