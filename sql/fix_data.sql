-- 修正 algo-runner 镜像的 tag
UPDATE images SET tag = 'algo-runner:1.0' WHERE name = 'algo-runner' AND tag = '1.0';

-- 确保其他镜像数据也正确（可选）
UPDATE images SET tag = 'python:3.8' WHERE name = 'Python 3.8';
UPDATE images SET tag = 'python:3.9' WHERE name = 'Python 3.9';
UPDATE images SET tag = 'node:14' WHERE name = 'Node.js 14';
UPDATE images SET tag = 'ubuntu:20.04' WHERE name = 'Ubuntu 20.04';
