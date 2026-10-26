package com.eusleats.eusleats.controller;

import com.eusleats.eusleats.dto.StudentRegistrationRequest;
import com.eusleats.eusleats.entity.Student;
import com.eusleats.eusleats.service.StudentRegistrationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentRegistrationController {

    private final StudentRegistrationService registrationService;

    public StudentRegistrationController(
            StudentRegistrationService registrationService) {

        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(
            @RequestBody StudentRegistrationRequest request) {

        try {

            Student student = registrationService.registerStudent(request);

            return ResponseEntity.ok("Student registered successfully!");

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}