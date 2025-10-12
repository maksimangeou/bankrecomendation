CREATE TABLE IF NOT EXISTS recommendation_rules (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(100),
    condition TEXT,
    message TEXT,
    active BOOLEAN DEFAULT TRUE
);