package com.evaluate.ai.langchain.controller;

import com.evaluate.ai.langchain.model.QuestionRequest;
import com.evaluate.ai.langchain.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/langchain")
@RestController
public class AiLangchainController {

    private final QuestionService questionService;

    public AiLangchainController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/chat")
    public ResponseEntity<?> chatWithAi(@RequestBody QuestionRequest questionRequest) {
        return ResponseEntity.ok(questionService.generateQuestions(questionRequest));
    }
}
