package com.evaluate.ai.langchain.agents;

import com.evaluate.ai.langchain.entity.GeneratedQuestion;
import com.evaluate.ai.langchain.entity.UserQuestionHistory;
import com.evaluate.ai.langchain.model.QuestionAnswerModel;
import com.evaluate.ai.langchain.model.QuestionsOutput;
import com.evaluate.ai.langchain.repository.AppUserRepository;
import com.evaluate.ai.langchain.repository.UserQuestionHistoryRepository;
import com.evaluate.ai.langchain.service.QuestionDeduplicationService;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class SaveQuestionsAgent {

    private final QuestionDeduplicationService questionDeduplicationService;
    private final UserQuestionHistoryRepository userQuestionHistoryRepository;
    private final AppUserRepository appUserRepository;

    public SaveQuestionsAgent(QuestionDeduplicationService questionDeduplicationService,
                              UserQuestionHistoryRepository userQuestionHistoryRepository,
                              AppUserRepository appUserRepository) {
        this.questionDeduplicationService = questionDeduplicationService;
        this.userQuestionHistoryRepository = userQuestionHistoryRepository;
        this.appUserRepository = appUserRepository;
    }

    @Agent(outputKey = "savedQuestions", name = "SaveQuestionsAgent", description = "Saves the generated questions to a database or file system.")
    public List<GeneratedQuestion> saveQuestions(@V("questions") QuestionsOutput questionsOutput, @V("topic") String topic, @V("userId") UUID userId) {
        var user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for id: " + userId));
        List<GeneratedQuestion> generatedQuestions = new ArrayList<>();
        for (QuestionAnswerModel questionAnswerModel : questionsOutput.questions()) {
            if (questionDeduplicationService.isDuplicate(questionAnswerModel.question())) {
                log.info("Skipping duplicate question for topic '{}': {}", topic, questionAnswerModel.question());
                continue;
            }

            GeneratedQuestion generatedQuestion = GeneratedQuestion.builder()
                    .questionText(questionAnswerModel.question())
                    .optionA(questionAnswerModel.options().get("A"))
                    .optionB(questionAnswerModel.options().get("B"))
                    .optionC(questionAnswerModel.options().get("C"))
                    .optionD(questionAnswerModel.options().get("D"))
                    .correctAnswer(questionAnswerModel.answer())
                    .topic(topic)
                    .source("LLM")
                    .llmModel("GEMINI")
                    .build();

            GeneratedQuestion savedQuestion = questionDeduplicationService.saveQuestion(generatedQuestion);
            generatedQuestions.add(savedQuestion);
            if (userQuestionHistoryRepository.existsByUser_IdAndQuestion_Id(userId, savedQuestion.getId())) {
                log.info("History already exists for user {} and question {}", userId, savedQuestion.getId());
                continue;
            }

            UserQuestionHistory userQuestionHistory = UserQuestionHistory.builder()
                    .user(user)
                    .question(savedQuestion)
                    .build();

            userQuestionHistoryRepository.save(userQuestionHistory);
            log.info("Generated question and history saved in database: {}", savedQuestion.getId());
        }
        return generatedQuestions;
    }
}
