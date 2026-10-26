package com.eusleats.eusleats.repository;

import com.eusleats.eusleats.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    Optional<Student> findByUniversityId(String universityId);

    Optional<Student> findByUser_UserId(Integer userId);
}