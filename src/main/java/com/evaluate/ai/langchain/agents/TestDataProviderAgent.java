package com.evaluate.ai.langchain.agents;

import dev.langchain4j.agentic.Agent;

public interface TestDataProviderAgent {

    @Agent(outputKey = "topicDetails", name = "Test Data Provider Agent", description = "An agent that provides test data.")
    default public String getTestData() {
        return """
                The topic is Java programming language. Java is a high-level, class-based, object-oriented programming language that is designed to 
                have as few implementation dependencies as possible. It was originally developed by James Gosling at Sun Microsystems and released in 
                1995. Java applications are typically compiled to bytecode that can run on any Java Virtual Machine (JVM) regardless of the underlying 
                computer architecture. The syntax of Java is similar to C and C++, but it has fewer low-level facilities than either of them. Java is widely 
                used for building enterprise-scale applications, mobile applications (especially Android apps), and large systems development.;
                """;
    }
}
