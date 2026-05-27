package com.evaluate.ai.langchain.model;

public record QuestionAnswerResponse(
        QuestionsOutput generatedQuestions,
        SimilarTopicList similarTopics
) {
}
