package com.eusleats.eusleats.dto;

public record AdminLoginResponse(
        Integer userId,
        String fullName,
        String email,
        String token) {
}
