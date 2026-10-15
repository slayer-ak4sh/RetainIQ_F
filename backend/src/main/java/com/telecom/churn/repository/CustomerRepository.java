package com.telecom.churn.repository;

import com.telecom.churn.entity.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    List<Customer> findAllByOrderByCreatedAtDesc();
}
