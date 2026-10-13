CREATE DATABASE IF NOT EXISTS event_db;
USE event_db;

CREATE TABLE IF NOT EXISTS events (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    venue           VARCHAR(100) NOT NULL,
    event_date      DATE NOT NULL,
    price           DECIMAL(10,2) NOT NULL,
    total_seats     INT NOT NULL,
    available_seats INT NOT NULL
);

CREATE TABLE IF NOT EXISTS bookings (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    event_id      INT NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL,
    seats         INT NOT NULL,
    total_amount  DECIMAL(10,2) NOT NULL,
    booked_on     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status        VARCHAR(15) DEFAULT 'BOOKED',
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

INSERT INTO events (name, venue, event_date, price, total_seats, available_seats) VALUES
('Tech Fest 2026',   'Main Auditorium', '2026-11-20', 250.00, 200, 200),
('Music Night',      'Open Air Theatre', '2026-12-05', 500.00, 300, 300),
('Startup Pitch Day','Seminar Hall',     '2026-12-15', 150.00, 100, 100);
