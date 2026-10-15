package com.telecom.churn.config;

import com.telecom.churn.entity.User;
import com.telecom.churn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password:Admin@123}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        String email = "admin@telecom.com";
        if (userRepository.existsByEmail(email)) return;

        User admin = new User();
        admin.setName("Super Admin");
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole("admin");
        userRepository.save(admin);
        System.out.println("Admin seeded → email: " + email);
    }
}
