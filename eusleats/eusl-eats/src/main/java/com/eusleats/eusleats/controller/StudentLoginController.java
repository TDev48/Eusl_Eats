package com.eusleats.eusleats.controller;

import com.eusleats.eusleats.dto.LoginRequest;
import com.eusleats.eusleats.dto.StudentLoginResponse;
import com.eusleats.eusleats.entity.Student;
import com.eusleats.eusleats.entity.User;
import com.eusleats.eusleats.repository.StudentRepository;
import com.eusleats.eusleats.security.TokenService;
import com.eusleats.eusleats.service.StudentLoginService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentLoginController {

    private final StudentLoginService loginService;
    private final StudentRepository studentRepository;
    private final TokenService tokenService;

    public StudentLoginController(
            StudentLoginService loginService,
            StudentRepository studentRepository,
            TokenService tokenService) {

        this.loginService = loginService;
        this.studentRepository = studentRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try {
            User user = loginService.login(request);

            Student student = studentRepository
                    .findByUser_UserId(user.getUserId())
                    .orElseThrow(() ->
                            new RuntimeException("Student profile not found"));

            return ResponseEntity.ok(
                    new StudentLoginResponse(
                            student.getStudentId(),
                            student.getUniversityId(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getPhone(),
                            student.getFaculty(),
                            tokenService.create("STUDENT", student.getStudentId())
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
