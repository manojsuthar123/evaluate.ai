package com.evaluate.ai.langchain.agents;

import com.evaluate.ai.langchain.model.SimilarTopicList;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SimilarTopicsExtractorAgent {

    @Agent(value = "Similar Topics Extractor Agent", description = "An agent that can extract similar topics based on the details of a topic and the total number of similar topics to be extracted.")
    @UserMessage(value = "Based on the following topic details: {{topicDetails}}, extract {{totalTopics}} similar topics. The output should be in the form of a list of similar topics.")
    SimilarTopicList extractSimilarTopics(@V("topicDetails") String topicDetails, @V("totalTopics") int totalTopics);
}
