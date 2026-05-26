package com.evaluate.ai.langchain.controller;

import com.evaluate.ai.langchain.model.QuestionRequest;
import com.evaluate.ai.langchain.rag.DocumentLoader;
import com.evaluate.ai.langchain.rag.RagService;
import com.evaluate.ai.langchain.service.QuestionService;
import dev.langchain4j.data.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/langchain")
@RestController
public class AiLangchainController {

    private final QuestionService questionService;
    private final RagService ragService;

    public AiLangchainController(QuestionService questionService, RagService ragService) {
        this.questionService = questionService;
        this.ragService = ragService;
    }

    @GetMapping("/chat")
    public ResponseEntity<?> chatWithAi(@RequestBody QuestionRequest questionRequest) {
        return ResponseEntity.ok(questionService.generateQuestions(questionRequest));
    }

    @GetMapping("/embed-documents")
    public ResponseEntity<?> embedDocuments() {
        // Load all *.txt documents from resources/static/docs directory
        List<Document> documents = DocumentLoader.getMultipleDocumentsWithGlob("/static/docs");
        ragService.embedDocsInPgVector(documents);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/search-documents")
    public String searchDocuments(@RequestParam String query) {
        return ragService.searchDocuments(query);
    }
}
