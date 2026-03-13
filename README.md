# 算法运行平台

该平台允许用户上传算法、选择运行镜像并执行任务。

## 快速开始

### 1. 启动数据库
使用 Docker Compose 启动 PostgreSQL 数据库：

```bash
docker-compose up -d
```

### 2. 运行应用
使用 Maven 运行 Spring Boot 应用：

```bash
mvn spring-boot:run
```

或者构建 jar 包运行：

```bash
mvn clean package
java -jar target/Platform-1.0-SNAPSHOT.jar
```

### 3. 访问界面
打开浏览器访问：[http://localhost:8080](http://localhost:8080)

## 功能
- **算法管理**：上传 Python 脚本，查看已注册算法。
- **任务运行**：选择算法、镜像和数据集，提交运行任务。
- **日志查看**：实时查看任务运行日志。

## 配置
修改 `src/main/resources/application.yml` 来配置数据库连接和其他设置。
