package com.evaluate.ai.langchain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class QuestionRequest {
    String topic;
    DifficultyLevel difficultyLevel;
    int totalQuestions;
    int totalSimilarTopics;
    UUID userId;
}
