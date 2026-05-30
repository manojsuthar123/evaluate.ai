package com.evaluate.ai.langchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question_embeddings")
public class QuestionEmbedding {

    @Id
    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "question_id")
    private GeneratedQuestion question;

    // PostgreSQL pgvector column declared as vector(384) in the schema.
    @Column(name = "embedding", columnDefinition = "vector(384)")
    private float[] embedding;
}

