package com.evaluate.ai.langchain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Strongly typed output for the QuestionBuilderAgent.
 * This follows the LangChain4j pattern for structured outputs.
 *
 * @see <a href="https://docs.langchain4j.dev/tutorials/agents#strongly-typed-inputs-and-outputs">Strongly-Typed Inputs and Outputs</a>
 */
public record QuestionsOutput(
        @JsonProperty("questions")
        List<QuestionAnswerModel> questions
) {
}

