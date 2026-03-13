package com.iiesoftware.platform.controller;

import com.iiesoftware.platform.model.Task;
import com.iiesoftware.platform.model.TaskStatus;
import com.iiesoftware.platform.service.DockerRunner;
import com.iiesoftware.platform.service.TaskManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "任务管理", description = "算法任务的创建、查询和管理接口")
public class TaskController {

    private final TaskManager taskManager;
    private final DockerRunner dockerRunner;

    @Autowired
    public TaskController(TaskManager taskManager, DockerRunner dockerRunner) {
        this.taskManager = taskManager;
        this.dockerRunner = dockerRunner;
    }

    @PostMapping("/run")
    @Operation(summary = "运行算法任务", description = "创建并执行一个新的算法任务")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "任务已接受并开始执行",
                    content = @Content(schema = @Schema(implementation = Task.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Task> runTask(
            @Parameter(description = "算法ID", required = true)
            @RequestParam Long algorithmId,

            @Parameter(description = "镜像ID", required = true)
            @RequestParam Long imageId,

            @Parameter(description = "数据集名称", required = true, example = "dataA")
            @RequestParam String dataset) {

        Task task = taskManager.createTask(algorithmId, imageId, dataset);

        // 异步执行
        dockerRunner.runTask(task)
                .thenAccept(exitCode -> {
                    // 异步完成后的处理
                });

        return ResponseEntity.accepted().body(task);
    }

    @GetMapping
    @Operation(summary = "获取所有任务", description = "返回所有任务的列表")
    @ApiResponse(responseCode = "200", description = "成功获取任务列表")
    public ResponseEntity<List<Task>> listAllTasks() {
        return ResponseEntity.ok(taskManager.listAllTasks());
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "获取任务详情", description = "根据任务ID获取特定任务的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取任务详情"),
            @ApiResponse(responseCode = "404", description = "任务不存在")
    })
    public ResponseEntity<Task> getTask(
            @Parameter(description = "任务ID", required = true, example = "20240321-143022")
            @PathVariable String taskId) {

        Task task = taskManager.getTask(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/{taskId}/status")
    @Operation(summary = "获取任务状态", description = "获取特定任务的当前状态")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取任务状态"),
            @ApiResponse(responseCode = "404", description = "任务不存在")
    })
    public ResponseEntity<TaskStatus> getTaskStatus(
            @Parameter(description = "任务ID", required = true) @PathVariable String taskId) {

        Task task = taskManager.getTask(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task.getStatus());
    }

    @GetMapping("/{taskId}/logs")
    @Operation(summary = "获取任务日志", description = "获取特定任务的运行日志")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取任务日志"),
            @ApiResponse(responseCode = "404", description = "任务不存在")
    })
    public ResponseEntity<String> getTaskLogs(
            @Parameter(description = "任务ID", required = true) @PathVariable String taskId) {

        Task task = taskManager.getTask(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task.getLogs());
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "删除任务", description = "删除指定的任务记录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "任务已删除"),
            @ApiResponse(responseCode = "404", description = "任务不存在")
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "任务ID", required = true) @PathVariable String taskId) {

        // 实现删除任务的逻辑
        return ResponseEntity.noContent().build();
    }
}