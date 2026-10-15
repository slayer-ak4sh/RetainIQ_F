package com.telecom.churn.controller;

import com.telecom.churn.entity.Customer;
import com.telecom.churn.entity.Prediction;
import com.telecom.churn.entity.User;
import com.telecom.churn.repository.CustomerRepository;
import com.telecom.churn.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionRepository predictionRepository;
    private final CustomerRepository customerRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @GetMapping
    public ResponseEntity<?> allPredictions() {
        List<Prediction> predictions = predictionRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> result = predictions.stream()
            .map(p -> {
                Customer c = customerRepository.findById(p.getCustomer()).orElse(null);
                if (c == null) return null;
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id", p.getId());
                m.put("customer", Map.of("_id", c.getId(), "name", c.getName(), "location", c.getLocation()));
                m.put("probability", p.getProbability());
                m.put("factors", p.getFactors());
                m.put("summary", p.getSummary());
                m.put("offers", p.getOffers());
                m.put("createdAt", p.getCreatedAt());
                return m;
            })
            .filter(p -> p != null)
            .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> runPrediction(@RequestBody Map<String, Object> body) {
        String customerId = (String) body.get("customerId");
        Map<String, Object> profile = new java.util.LinkedHashMap<>(body);
        profile.remove("customerId");

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = restTemplate.postForObject(
                aiServiceUrl + "/predict", profile, Map.class);

            if (customerId != null && data != null) {
                customerRepository.findById(customerId).ifPresent(c -> {
                    updateCustomerFromProfile(c, profile, data);
                    customerRepository.save(c);
                });

                Prediction pred = new Prediction();
                pred.setCustomer(customerId);
                pred.setProbability(((Number) data.get("probability")).doubleValue());
                pred.setFactors((List<Map<String, Object>>) data.get("factors"));
                pred.setSummary((String) data.get("summary"));
                pred.setOffers((List<Map<String, Object>>) data.get("offers"));
                pred.setProfileSnapshot(profile);
                predictionRepository.save(pred);
            }
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("error", "AI service unavailable", "detail", e.getMessage()));
        }
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<?> predictionHistory(@PathVariable String customerId,
                                                @AuthenticationPrincipal User user) {
        if ("customer".equals(user.getRole()) && !customerId.equals(user.getCustomer())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        return ResponseEntity.ok(predictionRepository.findByCustomerOrderByCreatedAtDesc(customerId));
    }

    private double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(v.toString());
    }

    private void updateCustomerFromProfile(Customer c, Map<String, Object> profile, Map<String, Object> data) {
        c.setChurnProbability(toDouble(data.get("probability")));
        if (profile.containsKey("tenure")) c.setTenure((int) toDouble(profile.get("tenure")));
        if (profile.containsKey("monthlyCharges")) c.setMonthlyCharges(toDouble(profile.get("monthlyCharges")));
        if (profile.containsKey("totalCharges")) c.setTotalCharges(toDouble(profile.get("totalCharges")));
        if (profile.containsKey("contract")) c.setContract((String) profile.get("contract"));
        if (profile.containsKey("internetService")) c.setInternetService((String) profile.get("internetService"));
        if (profile.containsKey("paymentMethod")) c.setPaymentMethod((String) profile.get("paymentMethod"));
        if (profile.containsKey("techSupport")) c.setTechSupport((String) profile.get("techSupport"));
        if (profile.containsKey("onlineSecurity")) c.setOnlineSecurity((String) profile.get("onlineSecurity"));
        if (profile.containsKey("onlineBackup")) c.setOnlineBackup((String) profile.get("onlineBackup"));
        if (profile.containsKey("deviceProtection")) c.setDeviceProtection((String) profile.get("deviceProtection"));
        if (profile.containsKey("streamingTV")) c.setStreamingTV((String) profile.get("streamingTV"));
        if (profile.containsKey("streamingMovies")) c.setStreamingMovies((String) profile.get("streamingMovies"));
        if (profile.containsKey("paperlessBilling")) c.setPaperlessBilling((String) profile.get("paperlessBilling"));
        if (profile.containsKey("gender")) c.setGender((String) profile.get("gender"));
        if (profile.containsKey("seniorCitizen")) c.setSeniorCitizen(profile.get("seniorCitizen").toString());
        if (profile.containsKey("partner")) c.setPartner((String) profile.get("partner"));
        if (profile.containsKey("dependents")) c.setDependents((String) profile.get("dependents"));
        if (profile.containsKey("multipleLines")) c.setMultipleLines((String) profile.get("multipleLines"));
    }
}
