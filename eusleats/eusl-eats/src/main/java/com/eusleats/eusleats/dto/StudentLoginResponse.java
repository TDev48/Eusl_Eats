package com.eusleats.eusleats.dto;

public record StudentLoginResponse(
        Integer studentId,
        String universityId,
        String fullName,
        String email,
        String phone,
        String faculty,
        String token) {
}
