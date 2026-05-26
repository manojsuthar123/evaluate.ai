package com.evaluate.ai.langchain.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TopicDetailsAggregatorAgent {

    @Agent(name = "TopicDetailsAggregatorAgent", description = "An agent that aggregates details of a topic from various sources.")
    @UserMessage(value = "Please provide detailed information about the topic: {{topic}}")
    @SystemMessage(value = "Your task is to gather and summarize detailed information about the given topic from various sources. Use your knowledge and any available tools to find relevant information, and provide a comprehensive summary.")
    String getDetailsOfTopic(@V("topic") String topic);
}
