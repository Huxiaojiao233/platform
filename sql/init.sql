-- 创建 algorithms 表
CREATE TABLE IF NOT EXISTS algorithms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    script_path VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建 images 表
CREATE TABLE IF NOT EXISTS images (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tag VARCHAR(255) NOT NULL
);

-- 创建 tasks 表
CREATE TABLE IF NOT EXISTS tasks (
    task_id VARCHAR(255) PRIMARY KEY,
    algorithm_id INTEGER REFERENCES algorithms(id),
    image_id INTEGER REFERENCES images(id),
    dataset VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    exit_code INTEGER,
    logs TEXT
);

-- 插入默认镜像数据
INSERT INTO images (name, tag) VALUES
    ('Python 3.8', 'python:3.8'),
    ('Python 3.9', 'python:3.9'),
    ('Node.js 14', 'node:14'),
    ('Ubuntu 20.04', 'ubuntu:20.04'),
    ('algo-runner', 'algo-runner:1.0')
ON CONFLICT DO NOTHING;
