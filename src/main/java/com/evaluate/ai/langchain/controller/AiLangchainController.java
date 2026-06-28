package com.evaluate.ai.langchain.controller;

import com.evaluate.ai.langchain.model.CustomMetadata;
import com.evaluate.ai.langchain.model.QuestionRequest;
import com.evaluate.ai.langchain.model.SubmitQuestionRequest;
import com.evaluate.ai.langchain.rag.DocumentLoader;
import com.evaluate.ai.langchain.rag.RagService;
import com.evaluate.ai.langchain.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/langchain")
@RestController
public class AiLangchainController {

    private final QuestionService questionService;
    private final RagService ragService;

    public AiLangchainController(QuestionService questionService, RagService ragService) {
        this.questionService = questionService;
        this.ragService = ragService;
    }

    @PostMapping("/question/generate")
    public ResponseEntity<?> generateQuestionWithAi(@RequestBody QuestionRequest questionRequest) {
        return ResponseEntity.ok(questionService.generateQuestions(questionRequest));
    }

    @PostMapping("/question/submit")
    public ResponseEntity<?> submitQuestion(@RequestBody List<SubmitQuestionRequest> submitQuestionRequests) {
        return ResponseEntity.ok(questionService.submitQuestion(submitQuestionRequests));
    }

    @GetMapping("/insights")
    public ResponseEntity<?> getInsights(@RequestParam String query) {
        return ResponseEntity.ok(questionService.getInsights(query));
    }

    @PostMapping("/embed-documents")
    public ResponseEntity<?> embedDocuments(@RequestParam("file") MultipartFile file, CustomMetadata customMetadata) throws IOException {
        // Load all *.txt documents from resources/static/docs directory
        //List<Document> documents = DocumentLoader.getMultipleDocumentsWithGlob("/static/docs");
        DocumentLoader loader = new DocumentLoader();
        ragService.embedDocsInPgVector(loader.toDocument(file), customMetadata);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/search-documents")
    public String searchDocuments(@RequestParam String query) {
        return ragService.searchDocuments(query);
    }
}
