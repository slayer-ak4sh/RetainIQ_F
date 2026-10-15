package com.telecom.churn.repository;

import com.telecom.churn.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);
    Optional<User> findByIdAndRole(String id, String role);
    Optional<User> findByCustomer(String customerId);
    boolean existsByEmail(String email);
    void deleteByCustomerAndRole(String customerId, String role);
}
