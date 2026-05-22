package com.evaluate.ai.langchain.agents;

import com.evaluate.ai.langchain.model.QuestionsOutput;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;


public interface QuestionBuilderAgent {

    @Agent(outputKey = "questions", value = "Question Builder Agent", description = "An agent that can generate questions based on the details of a topic, the difficulty level of the questions to be generated and the total number of questions to be generated.")
    @SystemMessage(value = "You are a question builder agent that can generate questions based on the details of a topic. You will be provided with the details of a topic, the difficulty level of the questions to be generated and the total number of questions to be generated. You should generate questions based on the provided details and return them in the form of a list of QuestionAnswerModel objects and keep the options concise and to the point.")
    @UserMessage(value = "Based on the following topic details: {{topicDetails}}, generate {{totalQuestions}} questions with difficulty level {{difficultyLevel}}. Each question should have 4 options (A, B, C, D) and one correct answer. The output should be in the form of a list of QuestionAnswerModel objects.")
    QuestionsOutput generateQuestion(@V("topicDetails") String topicDetails,
                                     @V("difficultyLevel") String difficultyLevel,
                                     @V("totalQuestions") int totalQuestions);
}
