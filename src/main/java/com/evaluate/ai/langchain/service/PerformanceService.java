package com.evaluate.ai.langchain.service;

import com.evaluate.ai.langchain.agents.InsightAgent;
import com.evaluate.ai.langchain.entity.UserQuestionHistory;
import com.evaluate.ai.langchain.model.PerformanceResponse;
import com.evaluate.ai.langchain.model.StringListOutput;
import com.evaluate.ai.langchain.repository.AppUserRepository;
import com.evaluate.ai.langchain.repository.GeneratedQuestionRepository;
import com.evaluate.ai.langchain.repository.UserQuestionHistoryRepository;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    private final UserQuestionHistoryRepository userQuestionHistoryRepository;
    private final GeneratedQuestionRepository generatedQuestionRepository;
    private final AppUserRepository appUserRepository;
    private final ChatModel chatModel;

    public PerformanceService(UserQuestionHistoryRepository userQuestionHistoryRepository, GeneratedQuestionRepository generatedQuestionRepository, AppUserRepository appUserRepository, ChatModel chatModel) {
        this.userQuestionHistoryRepository = userQuestionHistoryRepository;
        this.generatedQuestionRepository = generatedQuestionRepository;
        this.appUserRepository = appUserRepository;
        this.chatModel = chatModel;
    }

    public PerformanceResponse.PerformanceResult getPerformance(UUID userId) {
        // validate user
        appUserRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<UserQuestionHistory> histories = userQuestionHistoryRepository.findAllByUser_IdOrderByAskedAtAsc(userId);

        int totalQuestions = histories.size();
        int totalCorrect = (int) histories.stream().filter(h -> Boolean.TRUE.equals(h.getCorrect())).count();
        int totalIncorrect = totalQuestions - totalCorrect;

        // Distinct quiz days as a proxy for quiz attempts (no quiz entity present)
        Set<LocalDate> distinctDays = histories.stream()
                .filter(h -> h.getAskedAt() != null)
                .map(h -> h.getAskedAt().toLocalDate())
                .collect(Collectors.toCollection(TreeSet::new));

        int totalQuizzesAttempted = distinctDays.size();

        // Per-day accuracy
        Map<LocalDate, List<UserQuestionHistory>> byDay = histories.stream()
                .filter(h -> h.getAskedAt() != null)
                .collect(Collectors.groupingBy(h -> h.getAskedAt().toLocalDate()));

        List<Double> perDayAccuracy = byDay.values().stream().map(list -> {
            long correct = list.stream().filter(h -> Boolean.TRUE.equals(h.getCorrect())).count();
            return list.isEmpty() ? 0.0 : (100.0 * correct / list.size());
        }).toList();

        double averageQuizScore = perDayAccuracy.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double highestScore = perDayAccuracy.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double lowestScore = perDayAccuracy.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        // streaks (consecutive days in distinctDays)
        List<LocalDate> sortedDays = new ArrayList<>(distinctDays);
        Collections.sort(sortedDays);
        int longestStreak = 0;
        int currentStreak = 0;
        int tempStreak = 0;
        LocalDate previousDate = null;
        for (LocalDate d : sortedDays) {
            if (previousDate == null || d.equals(previousDate.plusDays(1))) {
                tempStreak++;
            } else {
                tempStreak = 1;
            }
            longestStreak = Math.max(longestStreak, tempStreak);
            previousDate = d;
        }
        // current streak: count back from last day
        if (!sortedDays.isEmpty()) {
            LocalDate today = LocalDate.now();
            LocalDate last = sortedDays.getLast();
            int cs = 0;
            LocalDate d = last;
            while (distinctDays.contains(d)) {
                cs++;
                d = d.minusDays(1);
            }
            currentStreak = cs;
        }

        // Topic-wise aggregation
        Map<String, List<UserQuestionHistory>> byTopic = histories.stream()
                .filter(h -> h.getQuestion() != null && h.getQuestion().getTopic() != null)
                .collect(Collectors.groupingBy(h -> h.getQuestion().getTopic()));

        List<PerformanceResponse.TopicAnalysis> topicAnalyses = new ArrayList<>();
        for (Map.Entry<String, List<UserQuestionHistory>> e : byTopic.entrySet()) {
            String topic = e.getKey();
            List<UserQuestionHistory> list = e.getValue();
            int questionsAttempted = list.size();
            int correct = (int) list.stream().filter(h -> Boolean.TRUE.equals(h.getCorrect())).count();
            int incorrect = questionsAttempted - correct;
            double accuracy = questionsAttempted == 0 ? 0.0 : (100.0 * correct / questionsAttempted);
            double avgScore = accuracy;
            Double avgTime = null;
            LocalDateTime lastAttemptAt = list.stream().map(UserQuestionHistory::getAskedAt).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);

            Map<String, PerformanceResponse.DifficultyStats> diff = Collections.emptyMap();

            topicAnalyses.add(new PerformanceResponse.TopicAnalysis(topic, 0, questionsAttempted, correct, incorrect, accuracy, avgScore, avgTime, lastAttemptAt, diff));
        }

        // Strengths and weaknesses
        List<PerformanceResponse.StrengthTopic> strengths = new ArrayList<>();
        List<PerformanceResponse.WeakTopic> weaknesses = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        for (PerformanceResponse.TopicAnalysis t : topicAnalyses) {
            // attempts count over topic
            int attempts = t.questionsAttempted();
            double accuracy = t.accuracy();

            // Recent accuracy (last 30 days)
            List<UserQuestionHistory> recent = histories.stream()
                    .filter(h -> h.getQuestion() != null && Objects.equals(h.getQuestion().getTopic(), t.topic()))
                    .filter(h -> h.getAskedAt() != null && h.getAskedAt().toLocalDate().isAfter(thirtyDaysAgo.minusDays(1)))
                    .toList();
            int recentCorrect = (int) recent.stream().filter(h -> Boolean.TRUE.equals(h.getCorrect())).count();
            double recentAcc = recent.isEmpty() ? accuracy : (100.0 * recentCorrect / recent.size());

            // previous period accuracy (30-60 days)
            LocalDate sixtyDaysAgo = LocalDate.now().minusDays(60);
            List<UserQuestionHistory> prev = histories.stream()
                    .filter(h -> h.getQuestion() != null && Objects.equals(h.getQuestion().getTopic(), t.topic()))
                    .filter(h -> h.getAskedAt() != null && (h.getAskedAt().toLocalDate().isAfter(sixtyDaysAgo.minusDays(1)) && h.getAskedAt().toLocalDate().isBefore(thirtyDaysAgo.plusDays(1))))
                    .toList();
            int prevCorrect = (int) prev.stream().filter(h -> Boolean.TRUE.equals(h.getCorrect())).count();
            double prevAcc = prev.isEmpty() ? recentAcc : (100.0 * prevCorrect / prev.size());

            double growth = prevAcc == 0 ? recentAcc - prevAcc : ((recentAcc - prevAcc) / (prevAcc == 0 ? 1 : prevAcc)) * 100.0;

            if (attempts >= 3 && accuracy > 80.0 && recentAcc >= 70.0) {
                double confidenceScore = Math.min(100.0, accuracy * 0.9 + growth * 0.1);
                double mastery = Math.min(100.0, accuracy);
                strengths.add(new PerformanceResponse.StrengthTopic(t.topic(), confidenceScore, accuracy, growth, mastery));
            }

            if (accuracy < 60.0 && attempts >= 3) {
                List<String> commonMistakes = histories.stream()
                        .filter(h -> h.getQuestion() != null && Objects.equals(h.getQuestion().getTopic(), t.topic()))
                        .filter(h -> Boolean.FALSE.equals(h.getCorrect()))
                        .map(h -> h.getQuestion() != null ? h.getQuestion().getQuestionText() : "")
                        .limit(5)
                        .collect(Collectors.toList());
                int recommended = Math.min(10, Math.max(3, attempts / 2));
                weaknesses.add(new PerformanceResponse.WeakTopic(t.topic(), accuracy, commonMistakes, (int) t.incorrectAnswers(), "Beginner", recommended));
            }
        }

        // Trends - daily for last 30 days
        Map<String, List<PerformanceResponse.TrendRecord>> trends = new HashMap<>();
        List<PerformanceResponse.TrendRecord> daily = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            List<UserQuestionHistory> dayList = byDay.getOrDefault(d, Collections.emptyList());
            int questions = dayList.size();
            int correct = (int) dayList.stream().filter(h -> Boolean.TRUE.equals(h.getCorrect())).count();
            double accuracy = questions == 0 ? 0.0 : (100.0 * correct / questions);
            double avgScoreDay = accuracy;
            long timeSpent = 0L;
            daily.add(new PerformanceResponse.TrendRecord(d, avgScoreDay, accuracy, questions, timeSpent));
        }
        trends.put("daily", daily);

        // Score history & accuracy history - per day
        List<PerformanceResponse.ScorePoint> scoreHistory = daily.stream()
                .map(r -> new PerformanceResponse.ScorePoint(r.date().atStartOfDay(), r.averageScore(), ""))
                .collect(Collectors.toList());

        List<PerformanceResponse.AccuracyPoint> accuracyHistory = daily.stream()
                .map(r -> new PerformanceResponse.AccuracyPoint(r.date().atStartOfDay(), r.accuracy()))
                .collect(Collectors.toList());

        // Difficulty analysis - not available in schema, return empty
        List<PerformanceResponse.DifficultyAnalysis> difficultyAnalysis = Collections.emptyList();

        // Radar: pick top topics by attempts and map mastery to accuracy
        List<PerformanceResponse.RadarPoint> radar = topicAnalyses.stream()
                .sorted(Comparator.comparingInt(PerformanceResponse.TopicAnalysis::questionsAttempted).reversed())
                .limit(8)
                .map(t -> new PerformanceResponse.RadarPoint(t.topic(), t.accuracy()))
                .collect(Collectors.toList());

        // Heatmap: map last 90 days
        Map<LocalDate, PerformanceResponse.TrendRecord> heatmap = new LinkedHashMap<>();
        for (int i = 89; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            List<UserQuestionHistory> dayList = byDay.getOrDefault(d, Collections.emptyList());
            int questions = dayList.size();
            int correct = (int) dayList.stream().filter(h -> Boolean.TRUE.equals(h.getCorrect())).count();
            double accuracy = questions == 0 ? 0.0 : (100.0 * correct / questions);
            heatmap.put(d, new PerformanceResponse.TrendRecord(d, accuracy, accuracy, questions, 0L));
        }

        // Dashboard cards
        String bestTopic = strengths.stream().findFirst().map(PerformanceResponse.StrengthTopic::topic).orElse("-");
        String weakestTopic = weaknesses.stream().findFirst().map(PerformanceResponse.WeakTopic::topic).orElse("-");
        PerformanceResponse.DashboardCards dashboard = new PerformanceResponse.DashboardCards(
                totalQuestions == 0 ? 0.0 : (100.0 * totalCorrect / totalQuestions),
                currentStreak,
                bestTopic,
                weakestTopic,
                averageQuizScore,
                0.0,
                totalQuizzesAttempted,
                totalQuestions
        );

        // Achievements
        List<String> achievements = new ArrayList<>();
        if (totalQuestions > 0) achievements.add("First Quiz");
        if (totalQuestions >= 100) achievements.add("100 Questions Solved");
        if (currentStreak >= 7) achievements.add("7-Day Streak");
        if (Math.abs(highestScore - 100.0) < 0.001) achievements.add("Perfect Score");

        // AI Insights and recommendations via langchain4j Assistant
        InsightAgent insightAgent = AiServices.builder(InsightAgent.class).chatModel(chatModel).build();

        StringListOutput generatedInsights = insightAgent.chat("insights", String.format("%.2f%%", dashboard.overallAccuracy()), radar.stream().map(PerformanceResponse.RadarPoint::topic).limit(5).collect(Collectors.joining(", ")), weakestTopic);
        List<String> aiInsights = new ArrayList<>(generatedInsights.dataList());

        StringListOutput generatedRecommendations = insightAgent.chat("recommendations", String.format("%.2f%%", dashboard.overallAccuracy()), radar.stream().map(PerformanceResponse.RadarPoint::topic).limit(5).collect(Collectors.joining(", ")), weakestTopic);
        List<String> recommendations = new ArrayList<>(generatedRecommendations.dataList());

        PerformanceResponse.Overview overview = new PerformanceResponse.Overview(
                totalQuizzesAttempted,
                totalQuestions,
                totalCorrect,
                totalIncorrect,
                totalQuestions == 0 ? 0.0 : (100.0 * totalCorrect / totalQuestions),
                averageQuizScore,
                highestScore,
                lowestScore,
                0.0,
                0L,
                currentStreak,
                longestStreak,
                totalQuizzesAttempted,
                0
        );

        return new PerformanceResponse.PerformanceResult(
                overview,
                dashboard,
                strengths,
                weaknesses,
                topicAnalyses,
                trends,
                scoreHistory,
                accuracyHistory,
                difficultyAnalysis,
                radar,
                heatmap,
                achievements,
                aiInsights,
                recommendations
        );
    }

}

