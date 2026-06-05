package com.evaluate.ai.langchain.model;

import java.util.UUID;

public record SubmitQuestionRequest(UUID questionId, String userAnswer) {
}
