
CREATE DATABASE IF NOT EXISTS medication_organizer
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE medication_organizer;

-- =====================================================
-- USERS
-- Application user accounts.
-- =====================================================

CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email VARCHAR(48) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(16) NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- =====================================================
-- USER ROLES
-- A user can have multiple roles.
-- =====================================================

CREATE TABLE user_roles (
    user_id BIGINT UNSIGNED NOT NULL,
    role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER',

    PRIMARY KEY (user_id, role),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- USER PREFERENCES
-- Display and accessibility preferences for each user.
-- =====================================================

CREATE TABLE user_preferences (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL UNIQUE,
    simplified_mode BOOLEAN NOT NULL DEFAULT FALSE,
    text_scale SMALLINT UNSIGNED NOT NULL DEFAULT 100,
    high_contrast BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT fk_user_preference_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- ORGANIZER MODELS
-- Catalog of available medication organizer models.
-- =====================================================

CREATE TABLE organizer_models (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_code VARCHAR(128) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    qr_code_value VARCHAR(255) NULL UNIQUE,
    description TEXT NULL,
    days_covered TINYINT UNSIGNED NULL,
    compartments_per_day TINYINT UNSIGNED NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- =====================================================
-- COMPARTMENT DEFINITIONS
-- Defines the compartments belonging to an organizer model.
-- =====================================================

CREATE TABLE compartment_definitions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    organizer_model_id BIGINT UNSIGNED NOT NULL,
    position_index SMALLINT UNSIGNED NOT NULL,
    label VARCHAR(32) NOT NULL,
    day_of_week TINYINT UNSIGNED NOT NULL
        COMMENT 'ISO weekday: Monday=1 ... Sunday=7',
    time_of_day VARCHAR(32) NOT NULL
        COMMENT 'MORNING, NOON, EVENING, NIGHT, OTHER',

    PRIMARY KEY (id),

    CONSTRAINT uq_compartment_position
        UNIQUE (organizer_model_id, position_index),

    CONSTRAINT fk_compartment_definition_model
        FOREIGN KEY (organizer_model_id)
        REFERENCES organizer_models(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================
-- USER ORGANIZERS
-- Organizers assigned to individual users.
-- =====================================================

CREATE TABLE user_organizers (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    organizer_model_id BIGINT UNSIGNED NOT NULL,
    display_name VARCHAR(32) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    KEY idx_user_organizers_user (user_id),
    KEY idx_user_organizers_model (organizer_model_id),

    CONSTRAINT fk_user_organizer_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_organizer_model
        FOREIGN KEY (organizer_model_id)
        REFERENCES organizer_models(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================
-- MEDICATIONS
-- Medications registered by users.
-- =====================================================

CREATE TABLE medications (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(128) NOT NULL,
    strength_value DECIMAL(6, 2) NOT NULL,
    strength_unit VARCHAR(32) NOT NULL,
    form VARCHAR(32) NULL,
    dose_amount DECIMAL(6,2) NOT NULL,
    dose_unit VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    KEY idx_medications_user (user_id),

    CONSTRAINT fk_medication_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- MEDICATION SCHEDULES
-- A medication can have multiple schedule versions.
-- =====================================================

CREATE TABLE medication_schedules (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    medication_id BIGINT UNSIGNED NOT NULL,
    valid_from DATE NOT NULL,
    valid_until DATE NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    replaced_at TIMESTAMP(6) NULL,

    PRIMARY KEY (id),

    KEY idx_schedule_medication (medication_id),

    CONSTRAINT fk_schedule_medication
        FOREIGN KEY (medication_id)
        REFERENCES medications(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================
-- SCHEDULE DAYS
-- Days of the week on which a schedule applies.
-- =====================================================

CREATE TABLE schedule_days (
    schedule_id BIGINT UNSIGNED NOT NULL,
    day_of_week TINYINT UNSIGNED NOT NULL
        COMMENT 'ISO weekday: Monday=1 ... Sunday=7',

    PRIMARY KEY (schedule_id, day_of_week),

    CONSTRAINT fk_schedule_days_schedule
        FOREIGN KEY (schedule_id)
        REFERENCES medication_schedules(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- SCHEDULE TIMES
-- Times at which doses are scheduled.
-- =====================================================

CREATE TABLE schedule_times (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    schedule_id BIGINT UNSIGNED NOT NULL,
    scheduled_time TIME NOT NULL,
    time_of_day VARCHAR(24) NULL
        COMMENT 'Optional display grouping',

    PRIMARY KEY (id),

    CONSTRAINT uq_schedule_time
        UNIQUE (schedule_id, scheduled_time),

    CONSTRAINT fk_schedule_times_schedule
        FOREIGN KEY (schedule_id)
        REFERENCES medication_schedules(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- DOSE OCCURRENCES
-- Individual scheduled doses, with snapshot values.
-- =====================================================

CREATE TABLE dose_occurrences (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    medication_id BIGINT UNSIGNED NOT NULL,
    schedule_id BIGINT UNSIGNED NOT NULL,
    user_organizer_id BIGINT UNSIGNED NULL,
    compartment_definition_id BIGINT UNSIGNED NULL,
    scheduled_date DATE NOT NULL,
    scheduled_time TIME NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    dose_amount_snapshot DECIMAL(6,2) NOT NULL,
    dose_unit_snapshot VARCHAR(32) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uq_occurrence_schedule_datetime
        UNIQUE (schedule_id, scheduled_date, scheduled_time),

    KEY idx_occurrence_user (user_id),
    KEY idx_occurrence_medication (medication_id),
    KEY idx_occurrence_organizer (user_organizer_id),
    KEY idx_occurrence_compartment (compartment_definition_id),

    CONSTRAINT fk_occurrence_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_occurrence_medication
        FOREIGN KEY (medication_id)
        REFERENCES medications(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_occurrence_schedule
        FOREIGN KEY (schedule_id)
        REFERENCES medication_schedules(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_occurrence_organizer
        FOREIGN KEY (user_organizer_id)
        REFERENCES user_organizers(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_occurrence_compartment
        FOREIGN KEY (compartment_definition_id)
        REFERENCES compartment_definitions(id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- INTAKE RECORDS
-- User-reported events related to dose intake.
-- =====================================================

CREATE TABLE intake_records (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    dose_occurrence_id BIGINT UNSIGNED NOT NULL,
    status VARCHAR(24) NOT NULL
        COMMENT 'User-reported intake or outcome status',
    occurred_at TIMESTAMP(6) NULL
        COMMENT 'When the user says the intake or outcome occurred',
    recorded_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    note VARCHAR(255) NULL,

    PRIMARY KEY (id),

    KEY idx_intake_occurrence (dose_occurrence_id),

    CONSTRAINT fk_intake_occurrence
        FOREIGN KEY (dose_occurrence_id)
        REFERENCES dose_occurrences(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================
-- PREPARATION SESSIONS
-- Sessions for preparing an organizer over a period.
-- =====================================================

CREATE TABLE preparation_sessions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    user_organizer_id BIGINT UNSIGNED NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    completed_at TIMESTAMP(6) NULL,

    PRIMARY KEY (id),

    KEY idx_preparation_user (user_id),
    KEY idx_preparation_organizer (user_organizer_id),

    CONSTRAINT fk_preparation_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_preparation_organizer
        FOREIGN KEY (user_organizer_id)
        REFERENCES user_organizers(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================
-- COMPARTMENT PREPARATIONS
-- Preparation status of each compartment in a session.
-- =====================================================

CREATE TABLE compartment_preparations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    preparation_session_id BIGINT UNSIGNED NOT NULL,
    compartment_definition_id BIGINT UNSIGNED NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'UNCHECKED',
    checked_at TIMESTAMP(6) NULL,
    note VARCHAR(255) NULL,

    PRIMARY KEY (id),

    CONSTRAINT uq_prepared_compartment
        UNIQUE (preparation_session_id, compartment_definition_id),

    CONSTRAINT fk_compartment_prep_session
        FOREIGN KEY (preparation_session_id)
        REFERENCES preparation_sessions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_compartment_prep_definition
        FOREIGN KEY (compartment_definition_id)
        REFERENCES compartment_definitions(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;
