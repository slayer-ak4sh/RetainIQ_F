package com.telecom.churn.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "predictions")
public class Prediction {
    @Id
    private String id;

    private String customer; // ref to Customer._id
    private double probability;
    private List<Map<String, Object>> factors;
    private String summary;
    private List<Map<String, Object>> offers;
    private Map<String, Object> profileSnapshot;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
