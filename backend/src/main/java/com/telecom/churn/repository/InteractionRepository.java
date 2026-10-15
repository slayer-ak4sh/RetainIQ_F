package com.telecom.churn.repository;

import com.telecom.churn.entity.Interaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InteractionRepository extends MongoRepository<Interaction, String> {
    List<Interaction> findByCustomerOrderByOccurredAtDesc(String customerId);
    List<Interaction> findAllByOrderByOccurredAtDesc();
    void deleteByCustomer(String customerId);
}
