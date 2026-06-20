package com.evaluate.ai.langchain.repository;

import com.evaluate.ai.langchain.entity.UserQuestionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserQuestionHistoryRepository extends JpaRepository<UserQuestionHistory, UUID> {

    boolean existsByUser_IdAndQuestion_Id(UUID userId, UUID questionId);

    Optional<UserQuestionHistory> findByUser_IdAndQuestion_Id(UUID userId, UUID questionId);

    // Find all history entries for a user (ordered by askedAt asc)
    java.util.List<UserQuestionHistory> findAllByUser_IdOrderByAskedAtAsc(UUID userId);
}

