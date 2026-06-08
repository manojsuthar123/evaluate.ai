package com.evaluate.ai.langchain.model;

import java.util.UUID;

public record SubmitQuestionResponse(UUID questionId, String userAnswer, String correctAnswer, boolean isCorrect) {
}
