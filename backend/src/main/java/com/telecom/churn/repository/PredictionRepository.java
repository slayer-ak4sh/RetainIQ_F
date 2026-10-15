package com.telecom.churn.repository;

import com.telecom.churn.entity.Prediction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PredictionRepository extends MongoRepository<Prediction, String> {
    List<Prediction> findByCustomerOrderByCreatedAtDesc(String customerId);
    List<Prediction> findAllByOrderByCreatedAtDesc();
    void deleteByCustomer(String customerId);
    long countByCustomer(String customerId);
}
