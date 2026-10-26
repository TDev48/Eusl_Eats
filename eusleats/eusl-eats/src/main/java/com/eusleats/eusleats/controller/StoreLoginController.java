package com.eusleats.eusleats.controller;

import com.eusleats.eusleats.dto.StoreLoginRequest;
import com.eusleats.eusleats.dto.StoreLoginResponse;
import com.eusleats.eusleats.entity.FoodStore;
import com.eusleats.eusleats.entity.User;
import com.eusleats.eusleats.repository.FoodStoreRepository;
import com.eusleats.eusleats.security.TokenService;
import com.eusleats.eusleats.service.StoreLoginService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
public class StoreLoginController {

    private final StoreLoginService loginService;
    private final FoodStoreRepository foodStoreRepository;
    private final TokenService tokenService;

    public StoreLoginController(
            StoreLoginService loginService,
            FoodStoreRepository foodStoreRepository,
            TokenService tokenService) {

        this.loginService = loginService;
        this.foodStoreRepository = foodStoreRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody StoreLoginRequest request) {

        try {
            User user = loginService.login(request);

            FoodStore store =
                    foodStoreRepository.findByUser_UserId(user.getUserId());

            if (store == null) {
                throw new RuntimeException("Store profile not found");
            }

            return ResponseEntity.ok(
                    new StoreLoginResponse(
                            user.getUserId(),
                            store.getStoreId(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getPhone(),
                            store.getStoreName(),
                            store.getLocation(),
                            store.getDescription(),
                            store.getApprovalStatus().name(),
                            tokenService.create("STORE", store.getStoreId())
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // The logged-in store's current details (so a store can see when the
    // admin has approved it without logging in again).
    @GetMapping("/me")
    public ResponseEntity<?> me(
            @RequestAttribute("auth.refId") Integer storeId) {

        FoodStore store = foodStoreRepository.findById(storeId).orElse(null);

        if (store == null) {
            return ResponseEntity.notFound().build();
        }

        User user = store.getUser();

        return ResponseEntity.ok(
                new StoreLoginResponse(
                        user.getUserId(),
                        store.getStoreId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhone(),
                        store.getStoreName(),
                        store.getLocation(),
                        store.getDescription(),
                        store.getApprovalStatus().name(),
                        null
                ));
    }
}
