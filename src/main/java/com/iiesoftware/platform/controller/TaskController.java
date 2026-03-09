package com.iiesoftware.platform.controller;

import com.iiesoftware.platform.model.Task;
import com.iiesoftware.platform.model.TaskStatus;
import com.iiesoftware.platform.service.DockerRunner;
import com.iiesoftware.platform.service.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskManager taskManager;
    private final DockerRunner dockerRunner;

    @Autowired
    public TaskController(TaskManager taskManager, DockerRunner dockerRunner) {
        this.taskManager = taskManager;
        this.dockerRunner = dockerRunner;
    }

    @PostMapping("/run")
    public ResponseEntity<Task> runTask(@RequestParam String algorithm,
                                        @RequestParam String dataset,
                                        @RequestParam(defaultValue = "main.py") String entryPoint) {
        Task task = taskManager.createTask(algorithm, dataset);

        // 异步执行
        dockerRunner.runTask(task.getTaskId(), algorithm, dataset, entryPoint)
                .thenAccept(exitCode -> {
                    // 异步完成后的处理
                });

        return ResponseEntity.accepted().body(task);
    }

    @GetMapping
    public ResponseEntity<List<Task>> listAllTasks() {
        return ResponseEntity.ok(taskManager.listAllTasks());
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTask(@PathVariable String taskId) {
        Task task = taskManager.getTask(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/{taskId}/status")
    public ResponseEntity<TaskStatus> getTaskStatus(@PathVariable String taskId) {
        Task task = taskManager.getTask(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task.getStatus());
    }

    @GetMapping("/{taskId}/logs")
    public ResponseEntity<String> getTaskLogs(@PathVariable String taskId) {
        Task task = taskManager.getTask(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task.getLogs());
    }
}