package com.evaluate.ai.langchain.listener;

import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.observability.AgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomAgentListener implements AgentListener {

    @Override
    public void afterAgentInvocation(AgentResponse agentResponse) {
        log.info("Agent invoked: {}", agentResponse.agentName());
        log.info("Agent output: {}", agentResponse.output());
        log.info("Agent arguments: {}", agentResponse.agent().arguments().toArray());
        if (agentResponse.chatResponse() != null && agentResponse.chatResponse().tokenUsage() != null) {
            log.info("Agent total token used:{}", agentResponse.chatResponse().tokenUsage().totalTokenCount());
        }
    }
}
