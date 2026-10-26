package com.eusleats.eusleats.controller;

import com.eusleats.eusleats.dto.AdminLoginResponse;
import com.eusleats.eusleats.entity.User;
import com.eusleats.eusleats.security.TokenService;
import com.eusleats.eusleats.service.AdminService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final TokenService tokenService;

    public AdminController(AdminService adminService, TokenService tokenService) {
        this.adminService = adminService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

        try {
            User user = adminService.login(
                    request.get("email"), request.get("password"));

            return ResponseEntity.ok(
                    new AdminLoginResponse(
                            user.getUserId(),
                            user.getFullName(),
                            user.getEmail(),
                            tokenService.create("ADMIN", user.getUserId())
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET /api/admin/stores            -> all stores
    // GET /api/admin/stores?status=PENDING
    @GetMapping("/stores")
    public ResponseEntity<?> listStores(
            @RequestParam(required = false) String status) {

        try {
            return ResponseEntity.ok(adminService.listStores(status));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/admin/stores/{storeId}/status   body: {"status":"APPROVED"}
    @PutMapping("/stores/{storeId}/status")
    public ResponseEntity<?> updateStoreStatus(
            @PathVariable Integer storeId,
            @RequestBody Map<String, String> request) {

        try {
            return ResponseEntity.ok(
                    adminService.updateStoreStatus(storeId, request.get("status")));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
