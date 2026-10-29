-- =====================================================================
-- Smart Traffic Monitoring and Violation Management System
-- Relational Database Schema for MySQL Server
-- =====================================================================

CREATE DATABASE IF NOT EXISTS traffic_db;
USE traffic_db;

-- ---------------------------------------------------------------------
-- Table 1: vehicles
-- Stores registered vehicle and driver/owner information together.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    registration_number VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type VARCHAR(30) NOT NULL,
    driver_name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) NOT NULL,
    contact_number VARCHAR(30),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_vehicle_reg (registration_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table 2: violations
-- Stores manual traffic violations linked to registered vehicles.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS violations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    violation_type VARCHAR(60) NOT NULL,
    date_time VARCHAR(30) NOT NULL,
    fine_amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_violations_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
        ON DELETE CASCADE,
    INDEX idx_violation_vehicle (vehicle_id),
    INDEX idx_violation_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table 3: accidents
-- Stores recorded traffic accidents and their severity.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS accidents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    location VARCHAR(150) NOT NULL,
    date_time VARCHAR(30) NOT NULL,
    severity VARCHAR(30) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_accident_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table 4: traffic_observations
-- Stores proportional traffic signal timing calculations.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS traffic_observations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date_time VARCHAR(30) NOT NULL,
    north_count INT NOT NULL,
    south_count INT NOT NULL,
    east_count INT NOT NULL,
    west_count INT NOT NULL,
    total_vehicles INT NOT NULL,
    congestion_level VARCHAR(20) NOT NULL,
    north_green INT NOT NULL,
    south_green INT NOT NULL,
    east_green INT NOT NULL,
    west_green INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
