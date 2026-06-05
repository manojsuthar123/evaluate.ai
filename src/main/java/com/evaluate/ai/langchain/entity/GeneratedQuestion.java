package com.evaluate.ai.langchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "generated_questions")
public class GeneratedQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "normalized_question", nullable = false)
    private String normalizedQuestion;

    @Column(name = "question_hash", nullable = false, unique = true, length = 64)
    private String questionHash;

    @Column(name = "option_a")
    private String optionA;

    @Column(name = "option_b")
    private String optionB;

    @Column(name = "option_c")
    private String optionC;

    @Column(name = "option_d")
    private String optionD;

    @Column(name = "correct_answer", length = 5)
    private String correctAnswer;

    @Column(name = "topic", length = 255)
    private String topic;

    @Column(name = "source", length = 255)
    private String source;

    @Column(name = "llm_model", length = 100)
    private String llmModel;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}

