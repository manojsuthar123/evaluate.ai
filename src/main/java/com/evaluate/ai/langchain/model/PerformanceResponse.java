package com.evaluate.ai.langchain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTOs for user performance analytics response. Designed to be frontend-friendly.
 */
public class PerformanceResponse {

    public record Overview(
            int totalQuizzesAttempted,
            int totalQuestionsAnswered,
            int totalCorrectAnswers,
            int totalIncorrectAnswers,
            double overallAccuracy,
            double averageQuizScore,
            double highestScore,
            double lowestScore,
            double averageCompletionTimeSeconds,
            long totalLearningTimeSeconds,
            int currentStreak,
            int longestStreak,
            int completedQuizzes,
            int incompleteQuizzes
    ) {}

    public record TopicAnalysis(
            String topic,
            int totalQuizzes,
            int questionsAttempted,
            int correctAnswers,
            int incorrectAnswers,
            double accuracy,
            double averageScore,
            Double averageTimeSeconds,
            LocalDateTime lastAttemptAt,
            Map<String, DifficultyStats> difficultyBreakdown
    ) {}

    public record DifficultyStats(int questionsAttempted, int correct, int incorrect, double accuracy, Double averageTimeSeconds) {}

    public record StrengthTopic(
            String topic,
            double confidenceScore,
            double accuracy,
            double growthRate,
            double masteryPercentage
    ) {}

    public record WeakTopic(
            String topic,
            double accuracy,
            List<String> commonMistakes,
            int incorrectAnswers,
            String suggestedDifficulty,
            int recommendedPracticeCount
    ) {}

    public record TrendRecord(LocalDate date, double averageScore, double accuracy, int questionsSolved, long timeSpentSeconds) {}

    public record ScorePoint(LocalDateTime date, double score, String topic) {}

    public record AccuracyPoint(LocalDateTime date, double accuracy) {}

    public record DifficultyAnalysis(String difficultyLevel, int questionsAttempted, int correct, int incorrect, double accuracy, Double averageTimeSeconds) {}

    public record RadarPoint(String topic, double mastery) {}

    public record DashboardCards(double overallAccuracy, int currentStreak, String bestTopic, String weakestTopic, double averageScore, double learningHours, int completedQuizzes, int totalQuestions) {}

    public record PerformanceResult(
            Overview overview,
            DashboardCards dashboard,
            List<StrengthTopic> strengths,
            List<WeakTopic> weaknesses,
            List<TopicAnalysis> topics,
            Map<String, List<TrendRecord>> trends, // daily/weekly/monthly
            List<ScorePoint> scoreHistory,
            List<AccuracyPoint> accuracyHistory,
            List<DifficultyAnalysis> difficultyAnalysis,
            List<RadarPoint> radar,
            Map<LocalDate, TrendRecord> heatmap,
            List<String> achievements,
            List<String> aiInsights,
            List<String> recommendations
    ) {}

}

