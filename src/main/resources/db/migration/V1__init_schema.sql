CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE study_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    slug VARCHAR(160) NOT NULL,
    description VARCHAR(1000),
    thumbnail_url VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_study_sets_slug UNIQUE (slug)
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE study_units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    study_set_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    unit_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL DEFAULT 'system',
    CONSTRAINT fk_study_units_study_set FOREIGN KEY (study_set_id) REFERENCES study_sets (id) ON DELETE CASCADE,
    CONSTRAINT uk_study_units_set_order UNIQUE (study_set_id, unit_order)
);

CREATE TABLE vocabularies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_id UUID NOT NULL,
    word VARCHAR(150) NOT NULL,
    meaning VARCHAR(255) NOT NULL,
    definition VARCHAR(1000),
    example_sentence VARCHAR(1000),
    example_translation VARCHAR(1000),
    phonetic_us VARCHAR(100),
    phonetic_uk VARCHAR(100),
    pronunciation_us_url VARCHAR(500),
    pronunciation_uk_url VARCHAR(500),
    hint VARCHAR(255),
    part_of_speech VARCHAR(50) NOT NULL,
    difficulty_level VARCHAR(50) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL DEFAULT 'system',
    CONSTRAINT fk_vocabularies_unit FOREIGN KEY (unit_id) REFERENCES study_units (id) ON DELETE CASCADE,
    CONSTRAINT uk_vocabularies_unit_order UNIQUE (unit_id, display_order)
);

CREATE TABLE study_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    vocabulary_id UUID NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    correct_count INTEGER NOT NULL DEFAULT 0,
    mastered BOOLEAN NOT NULL DEFAULT FALSE,
    last_mode VARCHAR(50),
    last_answer VARCHAR(255),
    last_correct_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL DEFAULT 'system',
    CONSTRAINT fk_study_progress_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_study_progress_vocabulary FOREIGN KEY (vocabulary_id) REFERENCES vocabularies (id) ON DELETE CASCADE,
    CONSTRAINT uk_study_progress_user_vocabulary UNIQUE (user_id, vocabulary_id)
);

CREATE INDEX idx_study_sets_display_order ON study_sets (display_order);
CREATE INDEX idx_study_units_study_set_id ON study_units (study_set_id);
CREATE INDEX idx_vocabularies_unit_id ON vocabularies (unit_id);
CREATE INDEX idx_vocabularies_word ON vocabularies (word);
CREATE INDEX idx_study_progress_user_id ON study_progress (user_id);
