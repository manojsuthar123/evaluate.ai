package com.evaluate.ai.langchain.agents;

import dev.langchain4j.agentic.Agent;

public interface TestDataProviderAgent {

    @Agent(outputKey = "topicDetails", name = "Test Data Provider Agent", description = "An agent that provides test data.")
    default public String getTestData() {
        return """
                The Indian economy is one of the fastest-growing major economies in the world, with a GDP growth rate averaging around 7% per annum. It has transformed from a planned to a market-oriented economy since independence in 1947 and has emerged as a significant player in global trade and commerce. The country's diverse industries, including textiles, IT, pharmaceuticals, and automotive, contribute significantly to its economic growth. India is also a major hub for foreign investment, 
                with many multinational companies setting up operations in the country due to its large and young consumer market. Additionally, the government has implemented various policies, such as Make in India, to promote domestic manufacturing and reduce dependence on imports.
                """;
    }
}
