package com.evaluate.ai.langchain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record StringListOutput(
        @JsonProperty("dataList")
        List<String> dataList
) {
}
