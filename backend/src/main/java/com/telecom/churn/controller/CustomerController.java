package com.telecom.churn.controller;

import com.telecom.churn.entity.Customer;
import com.telecom.churn.entity.User;
import com.telecom.churn.repository.CustomerRepository;
import com.telecom.churn.repository.InteractionRepository;
import com.telecom.churn.repository.PredictionRepository;
import com.telecom.churn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final InteractionRepository interactionRepository;
    private final PredictionRepository predictionRepository;
    private final PasswordEncoder passwordEncoder;

    private double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(v.toString());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal User user) {
        if (user.getCustomer() == null) {
            return ResponseEntity.status(404).body(Map.of("message", "No customer profile linked to this account yet"));
        }
        return customerRepository.findById(user.getCustomer())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(Map.of("message", "Linked customer record not found")));
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateMyProfile(@AuthenticationPrincipal User user,
                                              @RequestBody Map<String, Object> body) {
        if (user.getCustomer() == null) {
            return ResponseEntity.status(404).body(Map.of("message", "No customer profile linked to this account yet"));
        }
        Customer c = customerRepository.findById(user.getCustomer()).orElse(null);
        if (c == null) return ResponseEntity.status(404).body(Map.of("message", "Customer profile not found"));
        if (body.containsKey("name")) c.setName((String) body.get("name"));
        if (body.containsKey("phone")) c.setPhone((String) body.get("phone"));
        if (body.containsKey("location")) c.setLocation((String) body.get("location"));
        return ResponseEntity.ok(customerRepository.save(c));
    }

    @PostMapping("/me/password")
    public ResponseEntity<?> changeMyPassword(@AuthenticationPrincipal User user,
                                               @RequestBody Map<String, String> body) {
        if (user.getCustomer() == null) {
            return ResponseEntity.status(404).body(Map.of("message", "No customer profile linked to this account yet"));
        }
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current and new password are required"));
        }
        Customer c = customerRepository.findById(user.getCustomer()).orElse(null);
        if (c == null || c.getPassword() == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Customer profile not found"));
        }
        if (!passwordEncoder.matches(currentPassword, c.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Current password is incorrect"));
        }
        c.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(c);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    @GetMapping
    public List<Customer> listCustomers() {
        return customerRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return customerRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Customer not found")));
    }

    @PostMapping
    public ResponseEntity<?> addCustomer(@RequestBody Map<String, Object> body) {
        Customer c = new Customer();
        c.setName((String) body.get("name"));
        c.setEmail((String) body.get("email"));
        c.setPhone(body.getOrDefault("phone", "").toString());
        c.setLocation(body.getOrDefault("location", "").toString());
        if (body.get("password") != null) {
            c.setPassword(passwordEncoder.encode(body.get("password").toString()));
        }
        return ResponseEntity.ok(customerRepository.save(c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Customer c = customerRepository.findById(id).orElse(null);
        if (c == null) return ResponseEntity.status(404).body(Map.of("message", "Customer not found"));
        applyUpdates(c, body);
        return ResponseEntity.ok(customerRepository.save(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeCustomer(@PathVariable String id) {
        Customer c = customerRepository.findById(id).orElse(null);
        if (c == null) return ResponseEntity.status(404).body(Map.of("error", "Customer not found"));
        interactionRepository.deleteByCustomer(c.getId());
        predictionRepository.deleteByCustomer(c.getId());
        userRepository.deleteByCustomerAndRole(c.getId(), "customer");
        customerRepository.delete(c);
        return ResponseEntity.ok(Map.of("message", "Customer deleted successfully"));
    }

    @PostMapping("/{id}/portal-login")
    public ResponseEntity<?> createPortalLogin(@PathVariable String id, @RequestBody Map<String, String> body) {
        Customer c = customerRepository.findById(id).orElse(null);
        if (c == null) return ResponseEntity.status(404).body(Map.of("message", "Customer not found"));
        String email = body.get("email");
        String password = body.get("password");
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
        }
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "A login with this email already exists"));
        }
        if (userRepository.findByCustomer(c.getId()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "This customer already has a portal login"));
        }
        User u = new User();
        u.setName(c.getName());
        u.setEmail(email.toLowerCase());
        u.setPassword(passwordEncoder.encode(password));
        u.setRole("customer");
        u.setLocation(c.getLocation());
        u.setCustomer(c.getId());
        userRepository.save(u);
        return ResponseEntity.status(201).body(Map.of(
            "message", "Portal login created successfully",
            "user", Map.of("id", u.getId(), "name", u.getName(), "email", u.getEmail(), "role", u.getRole())
        ));
    }

    private void applyUpdates(Customer c, Map<String, Object> body) {
        if (body.containsKey("name")) c.setName((String) body.get("name"));
        if (body.containsKey("location")) c.setLocation((String) body.get("location"));
        if (body.containsKey("region")) c.setRegion((String) body.get("region"));
        if (body.containsKey("phone")) c.setPhone((String) body.get("phone"));
        if (body.containsKey("email")) c.setEmail((String) body.get("email"));
        if (body.containsKey("tenure")) c.setTenure((int) toDouble(body.get("tenure")));
        if (body.containsKey("monthlyCharges")) c.setMonthlyCharges(toDouble(body.get("monthlyCharges")));
        if (body.containsKey("totalCharges")) c.setTotalCharges(toDouble(body.get("totalCharges")));
        if (body.containsKey("contract")) c.setContract((String) body.get("contract"));
        if (body.containsKey("internetService")) c.setInternetService((String) body.get("internetService"));
        if (body.containsKey("paymentMethod")) c.setPaymentMethod((String) body.get("paymentMethod"));
        if (body.containsKey("techSupport")) c.setTechSupport((String) body.get("techSupport"));
        if (body.containsKey("onlineSecurity")) c.setOnlineSecurity((String) body.get("onlineSecurity"));
        if (body.containsKey("onlineBackup")) c.setOnlineBackup((String) body.get("onlineBackup"));
        if (body.containsKey("deviceProtection")) c.setDeviceProtection((String) body.get("deviceProtection"));
        if (body.containsKey("streamingTV")) c.setStreamingTV((String) body.get("streamingTV"));
        if (body.containsKey("streamingMovies")) c.setStreamingMovies((String) body.get("streamingMovies"));
        if (body.containsKey("paperlessBilling")) c.setPaperlessBilling((String) body.get("paperlessBilling"));
        if (body.containsKey("gender")) c.setGender((String) body.get("gender"));
        if (body.containsKey("seniorCitizen")) c.setSeniorCitizen(body.get("seniorCitizen").toString());
        if (body.containsKey("partner")) c.setPartner((String) body.get("partner"));
        if (body.containsKey("dependents")) c.setDependents((String) body.get("dependents"));
        if (body.containsKey("multipleLines")) c.setMultipleLines((String) body.get("multipleLines"));
        if (body.containsKey("churnProbability") && body.get("churnProbability") != null) {
            c.setChurnProbability(toDouble(body.get("churnProbability")));
        }
        if (body.containsKey("assignedEmployee")) c.setAssignedEmployee((String) body.get("assignedEmployee"));
    }
}
