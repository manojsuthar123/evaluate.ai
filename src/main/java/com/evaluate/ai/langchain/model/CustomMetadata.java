package com.evaluate.ai.langchain.model;

import org.springframework.web.bind.annotation.RequestPart;

public record CustomMetadata(
        @RequestPart String documentName,
        @RequestPart String tags,
        @RequestPart String language,
        @RequestPart String category
) {
}
