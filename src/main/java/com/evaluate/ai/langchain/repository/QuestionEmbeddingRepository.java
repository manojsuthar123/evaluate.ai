package com.evaluate.ai.langchain.repository;

import com.evaluate.ai.langchain.entity.QuestionEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface QuestionEmbeddingRepository extends JpaRepository<QuestionEmbedding, UUID> {

    @Query(value = """
            SELECT question_id, embedding <=> CAST(:embedding AS vector) AS distance
            FROM question_embeddings
            ORDER BY distance
            LIMIT 5
            """, nativeQuery = true)
    List<Object[]> findSimilarQuestions(@Param("embedding") String embedding);
}

