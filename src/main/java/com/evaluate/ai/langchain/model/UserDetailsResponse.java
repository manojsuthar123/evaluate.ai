package com.evaluate.ai.langchain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDetailsResponse {
    private UUID id;
    private String name;
    private String username;
    private String email;
    private String jwtToken;
    private LocalDateTime createdAt;
}
