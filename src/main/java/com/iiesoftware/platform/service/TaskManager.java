package com.iiesoftware.platform.service;

import com.iiesoftware.platform.config.PlatformProperties;
import com.iiesoftware.platform.model.Algorithm;
import com.iiesoftware.platform.model.Image;
import com.iiesoftware.platform.model.Task;
import com.iiesoftware.platform.model.TaskStatus;
import com.iiesoftware.platform.repository.AlgorithmRepository;
import com.iiesoftware.platform.repository.ImageRepository;
import com.iiesoftware.platform.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class TaskManager {

    private final PlatformProperties properties;
    private final TaskRepository taskRepository;
    private final AlgorithmRepository algorithmRepository;
    private final ImageRepository imageRepository;

    @Autowired
    public TaskManager(PlatformProperties properties, 
                       TaskRepository taskRepository,
                       AlgorithmRepository algorithmRepository,
                       ImageRepository imageRepository) {
        this.properties = properties;
        this.taskRepository = taskRepository;
        this.algorithmRepository = algorithmRepository;
        this.imageRepository = imageRepository;
    }

    @Transactional
    public Task createTask(Long algorithmId, Long imageId, String dataset) {
        Algorithm algorithm = algorithmRepository.findById(algorithmId)
            .orElseThrow(() -> new RuntimeException("Algorithm not found"));
        Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new RuntimeException("Image not found"));

        String taskId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Task task = new Task(taskId, algorithm, image, dataset);

        // 使用绝对路径创建任务目录
        Path taskPath = Paths.get(properties.getPaths().getTasks()).toAbsolutePath().resolve(taskId);
        Path resultPath = taskPath.resolve("result");

        try {
            Files.createDirectories(resultPath);
            logDirectoryInfo(taskPath);
            // 不再需要手动写 status.txt，状态存数据库
            taskRepository.save(task);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create task directory: " + taskPath, e);
        }

        return task;
    }

    private void logDirectoryInfo(Path path) {
        System.out.println("Created task directory: " + path.toAbsolutePath().normalize());
    }

    @Transactional
    public void updateTaskStatus(String taskId, TaskStatus status) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus(status);
            if (status == TaskStatus.SUCCESS || status == TaskStatus.FAILED || status == TaskStatus.TIMEOUT) {
                task.setCompletedAt(LocalDateTime.now());
            }
            taskRepository.save(task);
        }
    }

    @Transactional
    public void updateTaskExitCode(String taskId, int exitCode) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setExitCode(exitCode);
            taskRepository.save(task);
        }
    }

    @Transactional
    public void saveTaskLogs(String taskId, String logs) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setLogs(logs);
            taskRepository.save(task);
        }
    }

    public List<Task> listAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTask(String taskId) {
        return taskRepository.findById(taskId).orElse(null);
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
