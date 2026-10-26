package com.eusleats.eusleats.controller;

import com.eusleats.eusleats.dto.StoreRegistrationRequest;
import com.eusleats.eusleats.entity.FoodStore;
import com.eusleats.eusleats.service.StoreRegistrationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
public class StoreRegistrationController {

    private final StoreRegistrationService storeRegistrationService;

    public StoreRegistrationController(
            StoreRegistrationService storeRegistrationService) {

        this.storeRegistrationService = storeRegistrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerStore(
            @RequestBody StoreRegistrationRequest request) {

        try {

            FoodStore store =
                    storeRegistrationService.registerStore(request);

            return ResponseEntity.ok(
                    "Store registration submitted successfully! " +
                    "Waiting for admin approval."
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}