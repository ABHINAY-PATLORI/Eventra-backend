# Initialize database with required setup
-- Create database if not exists
CREATE DATABASE IF NOT EXISTS college_events;

-- Use the database
USE college_events;

-- Create a health check query table (optional)
CREATE TABLE IF NOT EXISTS health_check (
    id INT PRIMARY KEY AUTO_INCREMENT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert initial health check
INSERT INTO health_check (status, created_at) VALUES ('Database initialized', NOW());
