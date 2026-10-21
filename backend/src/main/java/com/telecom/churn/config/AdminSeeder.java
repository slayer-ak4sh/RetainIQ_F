package com.telecom.churn.config;

import com.telecom.churn.entity.Customer;
import com.telecom.churn.entity.User;
import com.telecom.churn.repository.CustomerRepository;
import com.telecom.churn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedUser("Super Admin",    "admin@retainiq.com",    "Admin@123", "admin",    null);
        seedUser("Employee Demo",  "employee@retainiq.com", "Emp@123",   "employee", null);
        seedCustomerUser("Customer Demo", "customer@retainiq.com", "Cust@123");
    }

    private void seedUser(String name, String email, String password, String role, String customerId) {
        if (userRepository.existsByEmail(email)) return;
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setRole(role);
        u.setCustomer(customerId);
        userRepository.save(u);
        System.out.println("Seeded [" + role + "] → " + email + " / " + password);
    }

    private void seedCustomerUser(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) return;

        Customer c = new Customer();
        c.setName(name);
        c.setEmail(email);
        c.setPhone("9999999999");
        c.setLocation("Mumbai");
        c.setRegion("West");
        c.setTenure(24);
        c.setMonthlyCharges(65.0);
        c.setTotalCharges(1560.0);
        c.setContract("One year");
        c.setInternetService("Fiber optic");
        c.setPaymentMethod("Bank transfer (automatic)");
        c.setTechSupport("Yes");
        c.setOnlineSecurity("Yes");
        c.setChurnProbability(0.12);
        Customer saved = customerRepository.save(c);

        seedUser(name, email, password, "customer", saved.getId());
    }
}
