package com.telecom.churn.controller;

import com.telecom.churn.entity.Customer;
import com.telecom.churn.entity.Interaction;
import com.telecom.churn.entity.User;
import com.telecom.churn.repository.CustomerRepository;
import com.telecom.churn.repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionRepository interactionRepository;
    private final CustomerRepository customerRepository;

    @GetMapping
    public List<Map<String, Object>> listAll() {
        return interactionRepository.findAllByOrderByOccurredAtDesc().stream()
            .map(i -> {
                Customer c = customerRepository.findById(i.getCustomer()).orElse(null);
                if (c == null) return null;
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id", i.getId());
                m.put("_id", i.getId());
                m.put("customer", Map.of("_id", c.getId(), "name", c.getName(), "location", c.getLocation()));
                m.put("type", i.getType());
                m.put("title", i.getTitle());
                m.put("notes", i.getNotes());
                m.put("severity", i.getSeverity());
                m.put("occurredAt", i.getOccurredAt());
                m.put("createdAt", i.getCreatedAt());
                return m;
            })
            .filter(i -> i != null)
            .toList();
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<?> listByCustomer(@PathVariable String customerId,
                                             @AuthenticationPrincipal User user) {
        if ("customer".equals(user.getRole()) && !customerId.equals(user.getCustomer())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        return ResponseEntity.ok(interactionRepository.findByCustomerOrderByOccurredAtDesc(customerId));
    }

    @PostMapping
    public ResponseEntity<?> addInteraction(@RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal User user) {
        Interaction item = new Interaction();
        if ("customer".equals(user.getRole())) {
            if (user.getCustomer() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No customer profile linked to your account"));
            }
            item.setCustomer(user.getCustomer());
            item.setType("complaint");
            item.setTitle((String) body.get("title"));
            item.setNotes(body.getOrDefault("notes", "").toString());
            item.setSeverity("medium");
        } else {
            item.setCustomer((String) body.get("customer"));
            item.setType(body.getOrDefault("type", "other").toString());
            item.setTitle((String) body.get("title"));
            item.setNotes(body.getOrDefault("notes", "").toString());
            item.setSeverity(body.getOrDefault("severity", "medium").toString());
        }
        return ResponseEntity.status(201).body(interactionRepository.save(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeInteraction(@PathVariable String id) {
        interactionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
