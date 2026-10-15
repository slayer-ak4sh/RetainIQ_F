package com.telecom.churn.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Data
@Document(collection = "customers")
public class Customer {
    @Id
    private String id;

    private String name;
    private String location = "";
    private String region = "";
    private int tenure = 0;
    private double monthlyCharges = 0;
    private double totalCharges = 0;
    private String contract = "Month-to-month";
    private String internetService = "Fiber optic";
    private String paymentMethod = "Electronic check";
    private String techSupport = "No";
    private String onlineSecurity = "No";
    private String onlineBackup = "No";
    private String deviceProtection = "No";
    private String streamingTV = "No";
    private String streamingMovies = "No";
    private String paperlessBilling = "Yes";
    private String gender = "Female";
    private String seniorCitizen = "0";
    private String partner = "No";
    private String dependents = "No";
    private String multipleLines = "No";
    private Double churnProbability;
    private String password;
    private String role = "customer";

    @Indexed(unique = true, sparse = true)
    private String email;
    private String phone = "";
    private String assignedEmployee;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
