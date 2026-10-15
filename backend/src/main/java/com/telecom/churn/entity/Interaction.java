package com.telecom.churn.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "interactions")
public class Interaction {
    @Id
    private String id;

    private String customer; // ref to Customer._id
    private String type = "other"; // complaint|technician_visit|installation|billing|other
    private String title;
    private String notes = "";
    private String severity = "medium"; // high|medium|low
    private Instant occurredAt = Instant.now();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
