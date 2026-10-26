package com.eusleats.eusleats.controller;

import com.eusleats.eusleats.dto.StudentProfileResponse;
import com.eusleats.eusleats.entity.Student;
import com.eusleats.eusleats.repository.StudentRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentProfileController {

    private final StudentRepository studentRepository;

    public StudentProfileController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // The logged-in student's own profile (identified by the token).
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(
            @RequestAttribute("auth.refId") Integer studentId) {

        Student student = studentRepository.findById(studentId).orElse(null);

        if (student == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                new StudentProfileResponse(
                        student.getStudentId(),
                        student.getUniversityId(),
                        student.getUser().getFullName(),
                        student.getUser().getEmail(),
                        student.getUser().getPhone(),
                        student.getFaculty()
                ));
    }
}
