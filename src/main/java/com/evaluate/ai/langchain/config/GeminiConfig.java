package com.evaluate.ai.langchain.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Bean("geminiChatModel")
    public ChatModel chatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(System.getenv("GOOGLE_GENAI_API_KEY"))
                .allowGoogleSearch(true)
                .logRequestsAndResponses(true)
                .maxOutputTokens(8192)
                .modelName("gemini-2.5-flash").build();
    }
}
