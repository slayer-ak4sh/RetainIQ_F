package com.telecom.churn.controller;

import com.telecom.churn.dto.AuthResponse;
import com.telecom.churn.dto.LoginRequest;
import com.telecom.churn.dto.RegisterRequest;
import com.telecom.churn.entity.User;
import com.telecom.churn.repository.UserRepository;
import com.telecom.churn.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getName() == null || req.getEmail() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "name, email and password are required"));
        }
        if (req.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
        }
        if (req.getRole() != null && !req.getRole().equals("customer")) {
            return ResponseEntity.status(403).body(Map.of("message", "You cannot register with this role"));
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already exists"));
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("customer");
        user.setLocation(req.getLocation() != null ? req.getLocation() : "");
        user.setRegion(req.getRegion() != null ? req.getRegion() : "");
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "message", "Registration successful",
            "token", jwtUtil.generate(user.getId(), user.getRole()),
            "user", toDto(user)
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req.getEmail() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "email and password are required"));
        }
        User user = userRepository.findByEmail(req.getEmail().toLowerCase()).orElse(null);
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }
        return ResponseEntity.ok(new AuthResponse(
            jwtUtil.generate(user.getId(), user.getRole()),
            toDto(user)
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of("user", toDto(user)));
    }

    private AuthResponse.UserDto toDto(User u) {
        return new AuthResponse.UserDto(u.getId(), u.getName(), u.getEmail(),
                u.getRole(), u.getLocation(), u.getRegion(), u.getCustomer());
    }
}
