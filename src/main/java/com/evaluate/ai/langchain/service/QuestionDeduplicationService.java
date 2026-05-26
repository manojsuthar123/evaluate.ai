package com.evaluate.ai.langchain.service;

import com.evaluate.ai.langchain.entity.GeneratedQuestion;
import com.evaluate.ai.langchain.entity.QuestionEmbedding;
import com.evaluate.ai.langchain.repository.GeneratedQuestionRepository;
import com.evaluate.ai.langchain.repository.QuestionEmbeddingRepository;
import com.evaluate.ai.langchain.utils.QuestionUtils;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionDeduplicationService {
    private final GeneratedQuestionRepository questionRepository;
    private final QuestionEmbeddingRepository embeddingRepository;
    private final EmbeddingModel embeddingModel;

    private static final double SIMILARITY_THRESHOLD = 0.10;

    public boolean isDuplicate(String questionText) {

        // Step 1 — normalize
        String normalized = QuestionUtils.normalize(questionText);

        // Step 2 — exact hash match
        String hash = QuestionUtils.sha256(normalized);

        boolean exactExists =
                questionRepository.findByQuestionHash(hash).isPresent();

        if (exactExists) {
            return true;
        }

        // Step 3 — semantic similarity
        Embedding embedding =
                embeddingModel.embed(questionText).content();

        String vectorString = toPgVector(embedding.vector());

        List<Object[]> results =
                embeddingRepository.findSimilarQuestions(vectorString);

        for (Object[] row : results) {

            Double distance = ((Number) row[1]).doubleValue();

            if (distance < SIMILARITY_THRESHOLD) {
                return true;
            }
        }

        return false;
    }

    public GeneratedQuestion saveQuestion(
            GeneratedQuestion question
    ) {

        String normalized =
                QuestionUtils.normalize(question.getQuestionText());

        question.setNormalizedQuestion(normalized);

        String hash =
                QuestionUtils.sha256(normalized);

        question.setQuestionHash(hash);

        GeneratedQuestion saved =
                questionRepository.save(question);

        Embedding embedding =
                embeddingModel.embed(question.getQuestionText())
                        .content();

        QuestionEmbedding qe = QuestionEmbedding.builder()
                .question(saved)
                .embedding(embedding.vector())
                .build();

        embeddingRepository.save(qe);

        return saved;
    }

    private String toPgVector(float[] vector) {

        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < vector.length; i++) {

            sb.append(vector[i]);

            if (i < vector.length - 1) {
                sb.append(",");
            }
        }

        sb.append("]");

        return sb.toString();
    }
}
