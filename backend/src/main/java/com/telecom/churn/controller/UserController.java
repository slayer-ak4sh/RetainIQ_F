package com.telecom.churn.controller;

import com.telecom.churn.entity.User;
import com.telecom.churn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/employees")
    public List<User> getEmployees() {
        List<User> employees = userRepository.findByRole("employee");
        employees.forEach(e -> e.setPassword(null));
        return employees;
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable String id) {
        return userRepository.findByIdAndRole(id, "employee")
            .map(e -> { e.setPassword(null); return ResponseEntity.<Object>ok(e); })
            .orElse(ResponseEntity.status(404).body(Map.of("message", "Employee not found")));
    }

    @PostMapping("/employees")
    public ResponseEntity<?> createEmployee(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already exists"));
        }
        User emp = new User();
        emp.setName(body.get("name"));
        emp.setEmail(email.toLowerCase());
        emp.setPassword(passwordEncoder.encode(body.get("password")));
        emp.setRole("employee");
        emp.setLocation(body.getOrDefault("location", ""));
        emp.setRegion(body.getOrDefault("region", ""));
        userRepository.save(emp);
        return ResponseEntity.status(201).body(Map.of(
            "message", "Employee created successfully",
            "employee", Map.of("id", emp.getId(), "name", emp.getName(),
                "email", emp.getEmail(), "role", emp.getRole(),
                "location", emp.getLocation(), "region", emp.getRegion())
        ));
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable String id) {
        return userRepository.findByIdAndRole(id, "employee")
            .map(e -> {
                userRepository.delete(e);
                return ResponseEntity.<Object>ok(Map.of("message", "Employee deleted successfully"));
            })
            .orElse(ResponseEntity.status(404).body(Map.of("message", "Employee not found")));
    }
}
