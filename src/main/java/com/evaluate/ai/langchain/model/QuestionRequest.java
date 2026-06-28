package com.evaluate.ai.langchain.model;

import lombok.Data;

@Data
public class QuestionRequest {
    String topic;
    DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;
    int totalQuestions = 5;
    int totalSimilarTopics = 5;
}
