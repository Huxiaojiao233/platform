package com.iiesoftware.platform.service;

import com.iiesoftware.platform.config.PlatformProperties;
import com.iiesoftware.platform.model.Task;
import com.iiesoftware.platform.model.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskManager {

    private final PlatformProperties properties;
    private final ConcurrentHashMap<String, Task> taskCache = new ConcurrentHashMap<>();

    @Autowired
    public TaskManager(PlatformProperties properties) {
        this.properties = properties;
    }

    public Task createTask(String algorithm, String dataset) {
        String taskId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Task task = new Task(taskId, algorithm, dataset);

        // 使用绝对路径创建任务目录
        Path taskPath = Paths.get(properties.getPaths().getTasks()).toAbsolutePath().resolve(taskId);
        Path resultPath = taskPath.resolve("result");

        try {
            Files.createDirectories(resultPath);
            logDirectoryInfo(taskPath);
            writeStatus(taskId, TaskStatus.PENDING);
            taskCache.put(taskId, task);
            task.setStatus(TaskStatus.PENDING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create task directory: " + taskPath, e);
        }

        return task;
    }

    private void logDirectoryInfo(Path path) {
        System.out.println("Created task directory: " + path.toAbsolutePath().normalize());
        System.out.println("Directory exists: " + Files.exists(path));
        System.out.println("Directory writable: " + Files.isWritable(path));
    }

    public void updateTaskStatus(String taskId, TaskStatus status) {
        Task task = taskCache.get(taskId);
        if (task != null) {
            task.setStatus(status);
            if (status == TaskStatus.SUCCESS || status == TaskStatus.FAILED || status == TaskStatus.TIMEOUT) {
                task.setCompletedAt(LocalDateTime.now());
            }
        }

        try {
            writeStatus(taskId, status);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update task status", e);
        }
    }

    public void updateTaskExitCode(String taskId, int exitCode) {
        Task task = taskCache.get(taskId);
        if (task != null) {
            task.setExitCode(exitCode);
        }
    }

    public void saveTaskLogs(String taskId, String logs) {
        Task task = taskCache.get(taskId);
        if (task != null) {
            task.setLogs(logs);
        }

        try {
            writeLog(taskId, logs);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save task logs", e);
        }
    }

    private void writeStatus(String taskId, TaskStatus status) throws IOException {
        Path taskDir = Paths.get(properties.getPaths().getTasks()).toAbsolutePath().resolve(taskId);
        Path statusFile = taskDir.resolve("status.txt");
        Files.write(statusFile, status.name().getBytes());
    }

    private void writeLog(String taskId, String log) throws IOException {
        Path taskDir = Paths.get(properties.getPaths().getTasks()).toAbsolutePath().resolve(taskId);
        Path logFile = taskDir.resolve("logs.txt");
        Files.write(logFile, log.getBytes());
    }

    public List<Task> listAllTasks() {
        Path tasksDir = Paths.get(properties.getPaths().getTasks()).toAbsolutePath();
        File[] taskDirs = tasksDir.toFile().listFiles(File::isDirectory);

        List<Task> tasks = new ArrayList<>();
        if (taskDirs != null) {
            for (File taskDir : taskDirs) {
                String taskId = taskDir.getName();
                Task task = taskCache.getOrDefault(taskId, loadTaskFromDisk(taskId));
                if (task != null) {
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public Task getTask(String taskId) {
        return taskCache.getOrDefault(taskId, loadTaskFromDisk(taskId));
    }

    private Task loadTaskFromDisk(String taskId) {
        // 从磁盘加载任务信息
        try {
            Path taskDir = Paths.get(properties.getPaths().getTasks()).toAbsolutePath().resolve(taskId);
            Path statusFile = taskDir.resolve("status.txt");
            if (Files.exists(statusFile)) {
                String status = new String(Files.readAllBytes(statusFile));
                Task task = new Task(taskId, "unknown", "unknown");
                task.setStatus(TaskStatus.valueOf(status));

                // 尝试读取日志
                Path logFile = taskDir.resolve("logs.txt");
                if (Files.exists(logFile)) {
                    task.setLogs(new String(Files.readAllBytes(logFile)));
                }

                return task;
            }
        } catch (Exception e) {
            // 忽略加载错误
        }
        return null;
    }

    public String getTaskResultPath(String taskId) {
        return Paths.get(properties.getPaths().getTasks())
                .toAbsolutePath()
                .resolve(taskId)
                .resolve("result")
                .normalize()
                .toString();
    }
}