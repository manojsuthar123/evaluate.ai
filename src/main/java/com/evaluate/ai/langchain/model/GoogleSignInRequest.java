package com.evaluate.ai.langchain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleSignInRequest {
    private String name;
    private String email;
    private String provider;
}

