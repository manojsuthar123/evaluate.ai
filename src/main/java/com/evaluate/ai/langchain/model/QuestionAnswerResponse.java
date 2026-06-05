package com.evaluate.ai.langchain.model;

import com.evaluate.ai.langchain.entity.GeneratedQuestion;

import java.util.List;

public record QuestionAnswerResponse(
        List<GeneratedQuestion> questions,
        SimilarTopicList similarTopics
) {
}
