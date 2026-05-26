package com.evaluate.ai.langchain.model;

import lombok.Data;

@Data
public class QuestionRequest {
    String topic;
    DifficultyLevel difficultyLevel;
    int totalQuestions;
    int totalSimilarTopics;
}
