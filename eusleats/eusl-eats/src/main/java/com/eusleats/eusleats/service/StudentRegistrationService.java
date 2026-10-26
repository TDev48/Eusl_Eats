package com.eusleats.eusleats.service;

import com.eusleats.eusleats.dto.StudentRegistrationRequest;
import com.eusleats.eusleats.entity.Student;
import com.eusleats.eusleats.entity.User;
import com.eusleats.eusleats.repository.StudentRepository;
import com.eusleats.eusleats.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentRegistrationService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public StudentRegistrationService(
            UserRepository userRepository,
            StudentRepository studentRepository) {

        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    public Student registerStudent(StudentRegistrationRequest request) {

        if (request.getFullName() == null || request.getFullName().isBlank()
                || request.getUniversityId() == null || request.getUniversityId().isBlank()) {
            throw new RuntimeException("Name and university ID are required");
        }

        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new RuntimeException("Please enter a valid email address");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }

        // Check whether email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Check whether university ID already exists
        if (studentRepository.findByUniversityId(request.getUniversityId()).isPresent()) {
            throw new RuntimeException("University ID already registered");
        }

        // Create User
        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setPhone(request.getPhone());
        user.setRole(User.Role.STUDENT);

        // Save User first
        User savedUser = userRepository.save(user);

        // Create Student
        Student student = new Student();

        student.setUser(savedUser);
        student.setUniversityId(request.getUniversityId());
        student.setFaculty(request.getFaculty());

        // Save Student
        return studentRepository.save(student);
    }
}