-- =========================================================
-- Training Log - AI Fitness Trainer
-- Run this in phpMyAdmin / MySQL Workbench before deploying.
-- =========================================================

CREATE DATABASE IF NOT EXISTS training_log
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE training_log;

-- ---------------------------------------------------------
-- Users
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    username      VARCHAR(50)   NOT NULL UNIQUE,
    password_hash VARCHAR(64)   NOT NULL,   -- SHA-256 hex digest
    age           INT,
    goal          VARCHAR(30),
    google_id     VARCHAR(64)   UNIQUE,     -- Google 'sub' claim, NULL for email/password accounts
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------
-- Profile (height/weight, editable separately from signup)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS profiles (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL UNIQUE,
    height_cm  DECIMAL(5,2),
    weight_kg  DECIMAL(5,2),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- Workout log - one row per completed session
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS workout_log (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    level      VARCHAR(20) NOT NULL,       -- beginner / intermediate / advanced
    calories   INT NOT NULL DEFAULT 0,
    log_date   DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- Water log - one row per user per day
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS water_log (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id  INT NOT NULL,
    log_date DATE NOT NULL,
    glasses  INT NOT NULL DEFAULT 0,
    UNIQUE KEY uniq_user_day (user_id, log_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- Already have a training_log database from before? Run this
-- single line instead of the CREATE TABLE above to add the
-- missing column without losing your existing users/data:
--   ALTER TABLE users ADD COLUMN google_id VARCHAR(64) UNIQUE;
-- ---------------------------------------------------------
