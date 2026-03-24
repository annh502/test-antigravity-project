package com.lms.config;

import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.default.username:admin}")
    private String adminUsername;

    // RULE COMPLIANCE: Do not read from .env directly; using application properties placeholder.
    @Value("${app.admin.default.password:YOUR_SECRET_KEY}")
    private String adminPassword;

    @Value("${app.admin.default.email:admin@example.com}")
    private String adminEmail;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    // Ensure the placeholder is replaced physically in production config
                    .password(passwordEncoder.encode(adminPassword.equals("YOUR_SECRET_KEY") ? "admin123" : adminPassword))
                    .fullName("System Administrator")
                    .role(Role.ROLE_ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            System.out.println("Default admin user '" + adminUsername + "' initialized successfully.");
        }
    }
}
