package com.eusleats.eusleats.config;

import com.eusleats.eusleats.entity.User;
import com.eusleats.eusleats.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first admin account on start-up if it does not exist yet.
 * Set app.admin.email / app.admin.password in application.properties
 * (or the ADMIN_EMAIL / ADMIN_PASSWORD environment variables) and change
 * the default password before using the system for real.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(
            UserRepository userRepository,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${app.admin.password}") String adminPassword) {

        this.userRepository = userRepository;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {

        try {
            if (userRepository.findByEmail(adminEmail).isPresent()) {
                return;
            }

            User admin = new User();
            admin.setFullName("System Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(new BCryptPasswordEncoder().encode(adminPassword));
            admin.setRole(User.Role.ADMIN);

            userRepository.save(admin);

            log.warn("Admin account created: {} (change the default password!)",
                    adminEmail);

        } catch (Exception e) {
            // Do not stop the whole application if the database rejects it
            // (for example, the users.role column does not allow 'ADMIN').
            log.error("Could not create the admin account: {}", e.getMessage());
        }
    }
}
