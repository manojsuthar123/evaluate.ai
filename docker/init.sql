---CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pgcrypto;


CREATE TABLE users
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE generated_questions
(
    id                  UUID PRIMARY KEY,
    question_text       TEXT        NOT NULL,
    normalized_question TEXT        NOT NULL,
    question_hash       VARCHAR(64) NOT NULL UNIQUE,

    option_a            TEXT,
    option_b            TEXT,
    option_c            TEXT,
    option_d            TEXT,

    correct_answer      VARCHAR(5),

    topic               VARCHAR(255),
    source              VARCHAR(255),

    llm_model           VARCHAR(100),

    created_at          TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_question_history
(
    id          UUID PRIMARY KEY,

    user_id     UUID REFERENCES users (id),
    question_id UUID REFERENCES generated_questions (id),

    user_answer VARCHAR(5),
    is_correct  BOOLEAN,

    asked_at    TIMESTAMP DEFAULT NOW(),

    UNIQUE (user_id, question_id)
);

CREATE TABLE question_embeddings (
                                     question_id UUID PRIMARY KEY REFERENCES generated_questions(id),

                                     embedding vector(384)
);