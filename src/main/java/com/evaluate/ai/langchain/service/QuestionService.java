package com.evaluate.ai.langchain.service;

import com.evaluate.ai.langchain.agents.QuestionBuilderAgent;
import com.evaluate.ai.langchain.agents.SaveQuestionsAgent;
import com.evaluate.ai.langchain.agents.SimilarTopicsExtractorAgent;
import com.evaluate.ai.langchain.agents.TopicDetailsAggregatorAgent;
import com.evaluate.ai.langchain.entity.AppUser;
import com.evaluate.ai.langchain.entity.GeneratedQuestion;
import com.evaluate.ai.langchain.listener.CustomAgentListener;
import com.evaluate.ai.langchain.model.*;
import com.evaluate.ai.langchain.rag.RagService;
import com.evaluate.ai.langchain.repository.AppUserRepository;
import com.evaluate.ai.langchain.repository.GeneratedQuestionRepository;
import com.evaluate.ai.langchain.repository.UserQuestionHistoryRepository;
import com.evaluate.ai.langchain.utils.Constant;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class QuestionService {

    private final ChatModel ollamaChatModel;
    private final CustomAgentListener customAgentListener;
    private final RagService ragService;
    private final SaveQuestionsAgent saveQuestionsAgent;
    private final UserQuestionHistoryRepository userQuestionHistoryRepository;
    private final GeneratedQuestionRepository generatedQuestionRepository;
    private final AppUserRepository appUserRepository;
    private final DataSource dataSource;

    public QuestionService(ChatModel ollamaChatModel, CustomAgentListener customAgentListener, RagService ragService, SaveQuestionsAgent saveQuestionsAgent, UserQuestionHistoryRepository userQuestionHistoryRepository, GeneratedQuestionRepository generatedQuestionRepository, AppUserRepository appUserRepository, DataSource dataSource) {
        this.ollamaChatModel = ollamaChatModel;
        this.customAgentListener = customAgentListener;
        this.ragService = ragService;
        this.saveQuestionsAgent = saveQuestionsAgent;
        this.userQuestionHistoryRepository = userQuestionHistoryRepository;
        this.generatedQuestionRepository = generatedQuestionRepository;
        this.appUserRepository = appUserRepository;
        this.dataSource = dataSource;
    }

    public QuestionAnswerResponse generateQuestions(QuestionRequest questionRequest) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UUID userId = user.getId();

        UntypedAgent questionAnswerAgent = AgenticServices.sequenceBuilder()
                .subAgents(this.getTopicDetailsAggregatorAgent(), this.getValidatedQuestionsAgent(questionRequest))
                .outputKey(Constant.OUTPUT_KEY_QUESTIONS)
                .listener(customAgentListener)
                .build();

        SimilarTopicsExtractorAgent similarTopicsExtractorAgent = AgenticServices.agentBuilder(SimilarTopicsExtractorAgent.class)
                .chatModel(ollamaChatModel)
                .outputKey("similarTopics")
                .listener(customAgentListener)
                .build();

        UntypedAgent parallelAgent = AgenticServices.sequenceBuilder()
                .subAgents(questionAnswerAgent, similarTopicsExtractorAgent, saveQuestionsAgent)
                .output(agenticScope -> {
                    List<GeneratedQuestion> savedQuestions = (List<GeneratedQuestion>) agenticScope.readState("savedQuestions");
                    SimilarTopicList similarTopics = (SimilarTopicList) agenticScope.readState("similarTopics");
                    return new QuestionAnswerResponse(savedQuestions, similarTopics);
                })
                .listener(customAgentListener)
                .build();

        Map<String, Object> input = Map.of(
                "userId", userId,
                "topic", questionRequest.getTopic(),
                "difficultyLevel", questionRequest.getDifficultyLevel().name(),
                "totalQuestions", questionRequest.getTotalQuestions(),
                "totalTopics", questionRequest.getTotalSimilarTopics()
        );

        return (QuestionAnswerResponse) parallelAgent.invoke(input);
    }

    /**
     * This agent retrieves details about the topic using both web search and RAG, then aggregates the information to provide a comprehensive context for question generation.
     */
    private TopicDetailsAggregatorAgent getTopicDetailsAggregatorAgent() {
        /*TestDataProviderAgent testDataProviderAgent = AgenticServices.agentBuilder(TestDataProviderAgent.class)
                .chatModel(ollamaChatModel)
                .outputKey("topicDetails")
                .listener(customAgentListener)
                .build();*/

        /*WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                .apiKey(System.getenv("TAVILY_API_KEY"))
                .build();

        ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .maxResults(3)
                .build();*/

        //QueryRouter queryRouter = new DefaultQueryRouter(webSearchContentRetriever, ragService.embeddingStoreContentRetriever());
        QueryRouter queryRouter = new DefaultQueryRouter(ragService.embeddingStoreContentRetriever());

        // Each retrieved segment should include "file_name" and "index" metadata values in the prompt
        ContentInjector contentInjector = DefaultContentInjector.builder()
                // .promptTemplate(...) // Formatting can also be changed
                .metadataKeysToInclude(List.of("file_name", "index"))
                .build();

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(queryRouter)
                .contentInjector(contentInjector)
                .build();

        return AgenticServices.agentBuilder(TopicDetailsAggregatorAgent.class)
                .chatModel(ollamaChatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .outputKey("topicDetails")
                .listener(customAgentListener)
                .build();
    }

    /**
     * This agent generates questions and validates them against duplicates. If a duplicate is detected, it discards the question and continues generating until it has a set of unique questions.
     */
    private QuestionBuilderAgent getValidatedQuestionsAgent(QuestionRequest questionRequest) {
        QuestionBuilderAgent questionBuilderAgent = AgenticServices.agentBuilder(QuestionBuilderAgent.class)
                .chatModel(ollamaChatModel)
                .outputKey(Constant.OUTPUT_KEY_QUESTIONS)
                .listener(customAgentListener)
                .build();
        return questionBuilderAgent;
        /*return AgenticServices.loopBuilder()
                .outputKey("questions")
                .subAgents(questionBuilderAgent)
                .exitCondition(agenticScope -> {
                    QuestionsOutput questions = (QuestionsOutput) agenticScope.readState("questions");
                    for (QuestionAnswerModel questionAnswerModel : questions.questions()) {
                        if (questionDeduplicationService.isDuplicate(questionAnswerModel.question())) {
                            log.info("Duplicate question detected: {}", questionAnswerModel.question());
                            return false;
                        } else {
                            GeneratedQuestion generatedQuestion = GeneratedQuestion.builder()
                                    .questionText(questionAnswerModel.question())
                                    .optionA(questionAnswerModel.options().get("A"))
                                    .optionB(questionAnswerModel.options().get("B"))
                                    .optionC(questionAnswerModel.options().get("C"))
                                    .optionD(questionAnswerModel.options().get("D"))
                                    .correctAnswer(questionAnswerModel.answer())
                                    .topic(questionRequest.getTopic())
                                    .source("LLM")
                                    .llmModel(ollamaChatModel.getClass().getName())
                                    .build();
                            questionDeduplicationService.saveQuestion(generatedQuestion);
                            log.info("Generated question saved in database: {}", generatedQuestion);
                        }
                    }
                    return true;
                })
                .listener(customAgentListener)
                .build();*/
    }

    public List<SubmitQuestionResponse> submitQuestion(List<SubmitQuestionRequest> submitQuestionRequests) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UUID userId = user.getId();

        List<SubmitQuestionResponse> submitQuestionResponses = new ArrayList<>();
        for (SubmitQuestionRequest submitQuestionRequest : submitQuestionRequests) {
            generatedQuestionRepository.findById(submitQuestionRequest.questionId())
                    .ifPresent(generatedQuestion -> {
                        userQuestionHistoryRepository.findByUser_IdAndQuestion_Id(userId, submitQuestionRequest.questionId())
                                .ifPresent(existingHistory -> {
                                    boolean isAnswerCorrect = submitQuestionRequest.userAnswer().equalsIgnoreCase(generatedQuestion.getCorrectAnswer());
                                    existingHistory.setUserAnswer(submitQuestionRequest.userAnswer());
                                    existingHistory.setCorrect(isAnswerCorrect);
                                    userQuestionHistoryRepository.save(existingHistory);
                                    submitQuestionResponses.add(new SubmitQuestionResponse(submitQuestionRequest.questionId(),
                                            submitQuestionRequest.userAnswer(),
                                            generatedQuestion.getCorrectAnswer(),
                                            isAnswerCorrect));
                                });
                    });
        }
        return submitQuestionResponses;
    }

    public String getInsights(String query) {
        /*ContentRetriever databaseContentRetriever = SqlDatabaseContentRetriever.builder()
                .dataSource(dataSource)
                .chatModel(ollamaChatModel)
                .build();
*/
        Assistant assistantChat = AiServices.builder(Assistant.class)
                .chatModel(ollamaChatModel)
                .build();
        return assistantChat.chat(query);
    }
}
