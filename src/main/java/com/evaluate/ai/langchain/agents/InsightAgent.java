package com.evaluate.ai.langchain.agents;

import com.evaluate.ai.langchain.model.StringListOutput;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface InsightAgent {

    @UserMessage("""
            Based on the following learning analytics, generate {{analysis}} concise and actionable learning insights.
            
            Learning Analytics:
            - Overall Accuracy: {{overall_accuracy}}
            - Top Topics: {{top_topics}}
            - Weak Topics: {{weak_topics}}
            
            Guidelines:
            - Each insight should be a single concise sentence.
            - Highlight strengths where appropriate.
            - Prioritize improvement suggestions for weak topics.
            - Do not repeat information.
            """)
    StringListOutput chat(
            @V("analysis") String analysis,
            @V("overall_accuracy") String overallAccuracy,
            @V("top_topics") String topTopics,
            @V("weak_topics") String weakTopics
    );
}
