package com.eusleats.eusleats.dto;

public class StudentProfileResponse {

    private Integer studentId;
    private String universityId;
    private String fullName;
    private String email;
    private String phone;
    private String faculty;

    public StudentProfileResponse(
            Integer studentId,
            String universityId,
            String fullName,
            String email,
            String phone,
            String faculty) {

        this.studentId = studentId;
        this.universityId = universityId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.faculty = faculty;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public String getUniversityId() {
        return universityId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getFaculty() {
        return faculty;
    }
}