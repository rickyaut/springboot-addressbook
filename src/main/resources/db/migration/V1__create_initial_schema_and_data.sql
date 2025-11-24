-- Create initial schema and data for PostgreSQL
CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS address_entry (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    street VARCHAR(255),
    user_id BIGINT,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

-- Insert initial users
INSERT INTO app_user (id, username) VALUES (1, 'user1') ON CONFLICT (id) DO NOTHING;
INSERT INTO app_user (id, username) VALUES (2, 'user2') ON CONFLICT (id) DO NOTHING;
INSERT INTO app_user (id, username) VALUES (3, 'user3') ON CONFLICT (id) DO NOTHING;
INSERT INTO app_user (id, username) VALUES (4, 'user4') ON CONFLICT (id) DO NOTHING;
INSERT INTO app_user (id, username) VALUES (5, 'user5') ON CONFLICT (id) DO NOTHING;