package com.evaluate.ai.langchain.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface WebSearchAgent {

    @SystemMessage("You are a web search agent that can search the web for information on a given topic. You will be provided with a topic and you should return the details of that topic based on your web search.")
    @UserMessage("Get the details of topic: {{topic}} from web")
    @Agent(name = "Web Search Agent", description = "An agent that can search the web for information on a given topic.")
    String getDetailsOfTopic(@V("topic") String topic);
}
