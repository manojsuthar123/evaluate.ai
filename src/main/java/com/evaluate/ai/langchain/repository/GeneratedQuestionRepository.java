package com.evaluate.ai.langchain.repository;

import com.evaluate.ai.langchain.entity.GeneratedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GeneratedQuestionRepository extends JpaRepository<GeneratedQuestion, UUID> {

    Optional<GeneratedQuestion> findByQuestionHash(String questionHash);
}

