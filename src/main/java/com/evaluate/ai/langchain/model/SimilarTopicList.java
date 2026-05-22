package com.evaluate.ai.langchain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SimilarTopicList(
        @JsonProperty("topics")
        List<String> topics
) {
}
