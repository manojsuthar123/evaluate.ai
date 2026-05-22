package com.evaluate.ai.langchain.model;

import java.util.Map;

public record QuestionAnswerModel(
        String question,
        Map<String, String> options,
        String answer
) {
}
