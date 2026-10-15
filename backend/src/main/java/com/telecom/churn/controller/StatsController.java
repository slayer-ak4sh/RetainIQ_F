package com.telecom.churn.controller;

import com.telecom.churn.entity.Prediction;
import com.telecom.churn.repository.CustomerRepository;
import com.telecom.churn.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final CustomerRepository customerRepository;
    private final PredictionRepository predictionRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @GetMapping
    public ResponseEntity<?> getStats() {
        long customerCount = customerRepository.count();
        long predictionCount = predictionRepository.count();
        List<Map<String, Object>> trendHistory = buildTrend();

        Map<String, Object> model = null;
        try {
            model = restTemplate.getForObject(aiServiceUrl + "/model-info", Map.class);
        } catch (Exception ignored) {}

        return ResponseEntity.ok(Map.of(
            "customerCount", customerCount,
            "predictionCount", predictionCount,
            "trendHistory", trendHistory,
            "model", model != null ? model : Collections.emptyMap()
        ));
    }

    private List<Map<String, Object>> buildTrend() {
        List<Prediction> predictions = predictionRepository.findAllByOrderByCreatedAtDesc();
        if (predictions.size() < 2) return Collections.emptyList();

        Map<String, List<Double>> weeks = new TreeMap<>();
        for (Prediction p : predictions) {
            Instant ts = p.getCreatedAt();
            if (ts == null) continue;
            var zdt = ts.atZone(ZoneOffset.UTC);
            int week = zdt.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            String key = "W" + String.format("%02d", week);
            weeks.computeIfAbsent(key, k -> new ArrayList<>()).add(p.getProbability());
        }

        return weeks.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .skip(Math.max(0, weeks.size() - 8))
            .map(e -> {
                double avg = e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
                return Map.<String, Object>of("label", e.getKey(), "avgRisk", (int) Math.round(avg * 100));
            })
            .collect(Collectors.toList());
    }
}
