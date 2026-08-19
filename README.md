# evaluate.ai 🤖📚

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.15.0-blue.svg)](https://github.com/langchain4j/langchain4j)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17%2B-blue.svg)](https://www.postgresql.org/)
[![pgvector](https://img.shields.io/badge/pgvector-Supported-success.svg)](https://github.com/pgvector/pgvector)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**evaluate.ai** is an open-source, AI-powered platform for automated assessment, intelligent question generation, vector-based RAG (Retrieval-Augmented Generation), and detailed candidate/user performance analytics. 

Built on Java 21, Spring Boot 4, and the LangChain4j Agentic framework, `evaluate.ai` orchestrates specialized AI agents to generate context-aware multiple-choice assessments, index custom documents via `pgvector`, and deliver AI-driven performance insights.

---

## 🌟 Key Features

- 🤖 **Multi-Agent Orchestration**: Sequenced and parallel agent execution using LangChain4j's `AgenticServices` for context retrieval, question building, related topic extraction, and database persistence.
- 📚 **Retrieval-Augmented Generation (RAG)**: Integrates Apache Tika document parsing with `AllMiniLmL6V2` embeddings and PostgreSQL `pgvector` for semantic document search and context-grounded test generation.
- 🎯 **Automated Question Generation & Scoring**: Generates structured multiple-choice questions (MCQs) tailored to specific topics and difficulty levels (`EASY`, `MEDIUM`, `HARD`), auto-verifies user submissions, and tracks historical attempts.
- 📊 **Deep Performance Analytics**: Computes overall accuracy, streak tracking (current & longest streaks), daily score trends, topic mastery, strengths/weaknesses breakdown, radar points, heatmap activity, and achievement badges.
- 💡 **AI Insights & Recommendations**: Employs an LLM-powered `InsightAgent` to summarize performance metrics into actionable study recommendations and analytical feedback.
- ⚡ **Dual LLM Provider Support**:
  - **Local Development (`local`)**: Integrates seamlessly with **Ollama** (`llama3.2`) for offline, cost-free local generation.
  - **Production Ready (`prod`)**: Leverages **Google Gemini AI** (`gemini-2.5-flash` / `gemini-2.0-flash`) with search grounding capabilities.
- 🐳 **Seamless Developer Setup**: Powered by Spring Boot Docker Compose integration to automatically manage PostgreSQL 17 + `pgvector` container lifecycles.

---

## 🏗️ Multi-Agent Architecture

`evaluate.ai` leverages a multi-agent workflow powered by LangChain4j:

<img width="1191" height="1081" alt="Evaluate io" src="https://github.com/user-attachments/assets/e5da7599-cdb4-4621-a7ae-5ddf73d2c933" />

## Application Preview
<img width="1435" height="680" alt="Screenshot 2026-08-20 at 12 40 29 AM" src="https://github.com/user-attachments/assets/ba2b6043-90b3-4d9b-a04f-f7704a4e7b45" />


### Agent Roles:
1. **TopicDetailsAggregatorAgent**: Retrieves semantic context from the vector database using PgVector content retrieval.
2. **QuestionBuilderAgent**: Formulates high-quality questions, options (A-D), and correct answers based on aggregated topic context.
3. **SimilarTopicsExtractorAgent**: Extracts complementary and related topics to recommend further learning paths.
4. **SaveQuestionsAgent**: Persists generated questions to PostgreSQL and generates vector embeddings.
5. **InsightAgent**: Analyzes candidate evaluation stats to generate tailored qualitative feedback and actionable recommendations.

---

## 🛠️ Technology Stack

| Component | Technology / Library |
| :--- | :--- |
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0.6, Spring Data JPA, Spring WebMVC |
| **AI Framework** | LangChain4j 1.15.0 (`langchain4j-agentic`, `langchain4j-google-ai-gemini`, `langchain4j-ollama`) |
| **RAG & Embeddings** | Apache Tika, `AllMiniLmL6V2EmbeddingModel` (ONNX, 384 dimensions) |
| **Vector Store** | PostgreSQL 17 + `pgvector` |
| **Database** | PostgreSQL |
| **Containerization** | Docker, Docker Compose (Spring Boot Docker Compose support) |
| **Dev Tools** | Lombok, Spring Boot DevTools, BootUI |

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: 21 or higher
- **Docker & Docker Desktop / Docker Engine**: For running PostgreSQL with `pgvector`
- **Ollama** *(for local profile)*: [Download Ollama](https://ollama.ai/) and pull `llama3.2`:
  ```bash
  ollama pull llama3.2
  ```
- **Google Gemini API Key** *(for prod profile)*: Obtain a key from [Google AI Studio](https://aistudio.google.com/).

---

### Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/manojsuthar123/evaluate.ai.git
   cd evaluate.ai
   ```

2. **Start Ollama** (if running under the `local` profile):
   ```bash
   ollama serve
   ```

3. **Run the Application with Gradle**:
   Spring Boot Docker Compose will automatically spin up the `pgvector/pgvector:pg17` PostgreSQL database container on port `5432`.

   - **Running locally with Ollama (Default profile: `local`)**:
     ```bash
     ./gradlew bootRun
     ```

   - **Running in Production mode with Google Gemini (`prod` profile)**:
     ```bash
     export GOOGLE_GENAI_API_KEY="your-api-key-here"
     ./gradlew bootRun --args='--spring.profiles.active=prod'
     ```

---

## 🔌 REST API Documentation

The base path for LangChain AI endpoints is `/api/langchain`. Below are the core API endpoints:

### 1. Generate Assessment Questions
- **Endpoint**: `POST /api/langchain/question/generate`
- **Request Body**:
  ```json
  {
    "topic": "Java Concurrency",
    "difficultyLevel": "MEDIUM",
    "totalQuestions": 3,
    "totalSimilarTopics": 2,
    "userId": "00000000-0000-0000-0000-000000000000"
  }
  ```
- **Response**:
  ```json
  {
    "questions": [
      {
        "id": "c1f7a2b9-...",
        "questionText": "What is the primary difference between Runnable and Callable in Java?",
        "optionA": "Runnable returns a result, Callable does not.",
        "optionB": "Callable can throw checked exceptions and return a result.",
        "optionC": "Runnable can only be used with ThreadPoolExecutor.",
        "optionD": "Callable cannot be submitted to an ExecutorService.",
        "correctAnswer": "B",
        "topic": "Java Concurrency",
        "source": "LLM",
        "llmModel": "dev.langchain4j.model.ollama.OllamaChatModel"
      }
    ],
    "similarTopics": {
      "topics": ["Java Executors", "Virtual Threads"]
    }
  }
  ```

---

### 2. Submit Answers for Scoring
- **Endpoint**: `POST /api/langchain/question/submit?userId={userId}`
- **Request Body**:
  ```json
  [
    {
      "questionId": "c1f7a2b9-...",
      "userAnswer": "B"
    }
  ]
  ```
- **Response**:
  ```json
  [
    {
      "questionId": "c1f7a2b9-...",
      "userAnswer": "B",
      "correctAnswer": "B",
      "isCorrect": true
    }
  ]
  ```

---

### 3. User Performance & Dashboard Analytics
- **Endpoint**: `GET /api/users/{userId}/performance`
- **Response**:
  ```json
  {
    "overview": {
      "totalQuizzesAttempted": 5,
      "totalQuestionsAttempted": 25,
      "totalCorrectAnswers": 20,
      "totalIncorrectAnswers": 5,
      "overallAccuracy": 80.0,
      "currentStreak": 3,
      "longestStreak": 5
    },
    "strengths": [
      { "topic": "Java Concurrency", "accuracy": 85.0 }
    ],
    "weaknesses": [
      { "topic": "Garbage Collection", "accuracy": 50.0 }
    ],
    "achievements": ["First Quiz", "7-Day Streak"],
    "aiInsights": ["You excel at concurrent data structures."],
    "recommendations": ["Review G1 and ZGC garbage collector tuning flags."]
  }
  ```

---

### 4. RAG Document Ingestion & Vector Search
- **Embed Document**: `GET /api/langchain/embed-documents` (Accepts `multipart/form-data` file)
- **Search Documents**: `GET /api/langchain/search-documents?query={query}`
- **AI Insights Assistant**: `GET /api/langchain/insights?query={query}`

---

## 🗄️ Database Schema

The database uses PostgreSQL with `pgvector` and `pgcrypto` extensions. Key tables include:

- `users`: Core user accounts (`id`, `name`, `created_at`).
- `generated_questions`: Stores generated MCQs (`id`, `question_text`, `normalized_question`, `question_hash`, options A-D, `correct_answer`, `topic`, `llm_model`).
- `user_question_history`: Records candidate response attempts (`user_id`, `question_id`, `user_answer`, `is_correct`, `asked_at`).
- `question_embeddings`: Stores 384-dimensional vector embeddings for question deduplication.
- `document_embeddings`: Managed by PgVector for RAG document chunk storage.

---

## 📂 Project Structure

```
evaluate.ai/
├── docker/
│   └── init.sql                 # Database initialization script (pgvector, tables)
├── docker-compose.yml           # PostgreSQL 17 + pgvector service definition
├── REST_API.md                  # REST API quick reference guide
├── build.gradle                 # Dependencies (Spring Boot 4, LangChain4j, PgVector)
├── src/
│   ├── main/
│   │   ├── java/com/evaluate/ai/
│   │   │   ├── langchain/
│   │   │   │   ├── agents/      # LangChain4j Agent definitions
│   │   │   │   ├── config/      # Spring Beans (Ollama, Gemini, PgVector, Database)
│   │   │   │   ├── controller/  # REST API Controllers (AiLangchain, Performance)
│   │   │   │   ├── entity/      # JPA Data Entities
│   │   │   │   ├── model/       # DTOs & Request/Response records
│   │   │   │   ├── rag/         # RAG Service & Document Ingestor (Apache Tika)
│   │   │   │   ├── repository/  # Spring Data JPA Repositories
│   │   │   │   └── service/     # Business logic & agent execution flows
│   │   └── resources/
│   │       ├── application.yml  # Application properties & active profiles
│   │       └── application-local.yml
```

---

## 🤝 Contributing

Contributions are welcome! Feel free to report issues, submit pull requests, or suggest new features for agentic evaluation and RAG search.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
