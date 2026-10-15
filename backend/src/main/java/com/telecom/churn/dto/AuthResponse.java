package com.telecom.churn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDto user;

    @Data
    @AllArgsConstructor
    public static class UserDto {
        private String id;
        private String name;
        private String email;
        private String role;
        private String location;
        private String region;
        private String customer;
    }
}
