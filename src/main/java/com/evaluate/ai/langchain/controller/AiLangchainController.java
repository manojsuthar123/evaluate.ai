package com.evaluate.ai.langchain.controller;

import com.evaluate.ai.langchain.agents.QuestionBuilderAgent;
import com.evaluate.ai.langchain.agents.SimilarTopicsExtractorAgent;
import com.evaluate.ai.langchain.agents.TestDataProviderAgent;
import com.evaluate.ai.langchain.listener.CustomAgentListener;
import com.evaluate.ai.langchain.model.DifficultyLevel;
import com.evaluate.ai.langchain.model.QuestionsOutput;
import com.evaluate.ai.langchain.model.SimilarTopicList;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/langchain")
@RestController
public class AiLangchainController {

    private final ChatModel geminiChatModel;
    private final ChatModel ollamaChatModel;
    private final CustomAgentListener customAgentListener;

    public AiLangchainController(@Qualifier("geminiChatModel") ChatModel geminiChatModel, ChatModel ollamaChatModel, CustomAgentListener customAgentListener) {
        this.geminiChatModel = geminiChatModel;
        this.ollamaChatModel = ollamaChatModel;
        this.customAgentListener = customAgentListener;
    }

    @GetMapping("/chat")
    public ResponseEntity<?> chatWithAi(@RequestParam String query) {
        WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                .apiKey(System.getenv("TAVILY_API_KEY"))
                .build();

        ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .maxResults(3)
                .build();

        /*WebSearchAgent webSearchAgent = AiServices.builder(WebSearchAgent.class)
                .chatModel(chatModel)
                .contentRetriever(webSearchContentRetriever)
                .build();*/

        /*WebSearchAgent webSearchAgent = AgenticServices.agentBuilder(WebSearchAgent.class)
                .chatModel(ollamaChatModel)
                .contentRetriever(webSearchContentRetriever)
                .outputKey("topicDetails")
                .listener(customAgentListener)
                .build();*/

        TestDataProviderAgent testDataProviderAgent = AgenticServices.agentBuilder(TestDataProviderAgent.class)
                .chatModel(ollamaChatModel)
                .outputKey("topicDetails")
                .listener(customAgentListener)
                .build();

        QuestionBuilderAgent questionBuilderAgent = AgenticServices.agentBuilder(QuestionBuilderAgent.class)
                .chatModel(ollamaChatModel)
                .outputKey("questions")
                .listener(customAgentListener)
                .build();

        UntypedAgent questionAnswerAgent = AgenticServices.sequenceBuilder()
                .subAgents(testDataProviderAgent, questionBuilderAgent)
                .outputKey("questions")
                .listener(customAgentListener)
                .build();

        SimilarTopicsExtractorAgent similarTopicsExtractorAgent = AgenticServices.agentBuilder(SimilarTopicsExtractorAgent.class)
                .chatModel(ollamaChatModel)
                .outputKey("similarTopics")
                .listener(customAgentListener)
                .build();

        UntypedAgent parallelAgent = AgenticServices.sequenceBuilder()
                .subAgents(questionAnswerAgent, similarTopicsExtractorAgent)
                .output(agenticScope -> {
                    QuestionsOutput questions = (QuestionsOutput) agenticScope.readState("questions");
                    SimilarTopicList similarTopics = (SimilarTopicList) agenticScope.readState("similarTopics");
                    return Map.of(
                            "questions", questions,
                            "similarTopics", similarTopics
                    );
                })
                .listener(customAgentListener)
                .build();

        Map<String, Object> input = Map.of(
                "topic", query,
                "difficultyLevel", DifficultyLevel.MEDIUM.name(),
                "totalQuestions", 3,
                "totalTopics", 5
        );

        Map<String, Object> response = (Map<String, Object>) parallelAgent.invoke(input);

        return ResponseEntity.ok(response);
    }
}
