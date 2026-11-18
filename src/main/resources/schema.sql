CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL
);

INSERT INTO products (id, name)
VALUES (123, 'Mechanical Keyboard')
ON CONFLICT (id) DO NOTHING;