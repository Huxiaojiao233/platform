package com.iiesoftware.platform.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.iiesoftware.platform.config.PlatformProperties;
import com.iiesoftware.platform.model.Task;
import com.iiesoftware.platform.model.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class DockerRunner {

    private static final Logger log = LoggerFactory.getLogger(DockerRunner.class);

    private final DockerClient docker;
    private final PlatformProperties properties;
    private final TaskManager taskManager;

    @Autowired
    public DockerRunner(DockerClient docker, PlatformProperties properties, TaskManager taskManager) {
        this.docker = docker;
        this.properties = properties;
        this.taskManager = taskManager;
    }

    @Async
    public CompletableFuture<Integer> runTask(Task task) {
        String taskId = task.getTaskId();
        CompletableFuture<Integer> future = new CompletableFuture<>();

        try {
            taskManager.updateTaskStatus(taskId, TaskStatus.RUNNING);

            // 获取路径
            String algoPath = Paths.get(task.getAlgorithm().getScriptPath()).toAbsolutePath().toString();
            String inputPath = getAbsolutePath(properties.getPaths().getDatasets(), task.getDataset());
            String outputPath = getAbsolutePath(properties.getPaths().getTasks(), taskId + "/result");

            log.info("Task {} - Algorithm path: {}", taskId, algoPath);
            log.info("Task {} - Input path: {}", taskId, inputPath);
            log.info("Task {} - Output path: {}", taskId, outputPath);

            // 验证路径是否存在
            File algoFile = new File(algoPath);
            if (!algoFile.exists()) {
                 throw new IllegalArgumentException("Algorithm file does not exist: " + algoPath);
            }
            File inputFile = new File(inputPath);
             if (!inputFile.exists()) {
                 throw new IllegalArgumentException("Input path does not exist: " + inputPath);
            }

            // 确保输出目录存在
            File outputDir = new File(outputPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
                log.info("Created output directory: {}", outputPath);
            }

            String imageName = task.getImage().getTag();

            // 检查Docker镜像是否存在
            checkDockerImage(imageName);

            Volume algoVol = new Volume("/workspace/algorithm.py");
            Volume inputVol = new Volume("/data/input");
            Volume outputVol = new Volume("/data/output");

            log.info("Creating container for task {} with image {}...", taskId, imageName);

            // 确保目录权限 (macOS 上通常不需要，但在某些 Docker 环境可能需要)
            // 修改绑定挂载逻辑
            CreateContainerResponse container = docker.createContainerCmd(imageName)
                    .withBinds(
                            new Bind(algoPath, new Volume("/workspace/algorithm.py")),
                            new Bind(inputPath, new Volume("/data/input")),
                            new Bind(outputPath, new Volume("/data/output"))
                    )
                    .withCmd("/workspace/algorithm.py")
                    .exec();

            String containerId = container.getId();
            log.info("Container {} created for task {}", containerId, taskId);

            // 启动容器
            docker.startContainerCmd(containerId).exec();
            log.info("Container {} started", containerId);

            // 收集日志
            StringBuilder logs = new StringBuilder();
            docker.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .exec(new LogContainerResultCallback() {
                        @Override
                        public void onNext(Frame frame) {
                            String logLine = new String(frame.getPayload());
                            logs.append(logLine);
                            log.info("Container {}: {}", containerId, logLine.trim());
                        }
                    });

            // 等待容器结束
            log.info("Waiting for container {} to finish...", containerId);
            Integer exitCode = docker.waitContainerCmd(containerId)
                    .start()
                    .awaitStatusCode(properties.getDocker().getTimeoutMinutes(), TimeUnit.MINUTES);

            log.info("Container {} finished with exit code: {}", containerId, exitCode);

            // 保存日志
            taskManager.saveTaskLogs(taskId, logs.toString());

            // 删除容器
            docker.removeContainerCmd(containerId).withForce(true).exec();
            log.info("Container {} removed", containerId);

            // 更新任务状态
            TaskStatus status = (exitCode != null && exitCode == 0) ? TaskStatus.SUCCESS : TaskStatus.FAILED;
            taskManager.updateTaskStatus(taskId, status);
            taskManager.updateTaskExitCode(taskId, exitCode);

            log.info("Task {} finished with status: {}", taskId, status);
            future.complete(exitCode);

        } catch (Exception e) {
            log.error("Task {} failed with exception", taskId, e);
            saveErrorLog(taskId, "ERROR: " + e.getMessage() + "\n" + getStackTraceAsString(e));
            taskManager.updateTaskStatus(taskId, TaskStatus.FAILED);
            future.completeExceptionally(e);
        }

        return future;
    }

    private String getAbsolutePath(String basePath, String subPath) {
        Path path = Paths.get(basePath, subPath);
        return path.toAbsolutePath().normalize().toString();
    }

    private void validatePaths(String... paths) {
        for (String path : paths) {
            File file = new File(path);
            if (!file.exists()) {
                throw new IllegalArgumentException("Path does not exist: " + path);
            }
        }
    }

    private void checkDockerImage(String imageName) {
        try {
            docker.inspectImageCmd(imageName).exec();
            log.info("Docker image {} found", imageName);
        } catch (Exception e) {
            log.warn("Docker image {} not found locally, attempting to pull...", imageName);
            try {
                docker.pullImageCmd(imageName).start().awaitCompletion();
                log.info("Docker image {} pulled successfully", imageName);
            } catch (Exception pullEx) {
                log.error("Failed to pull docker image {}", imageName, pullEx);
                throw new RuntimeException("Docker image not found and failed to pull: " + imageName, e);
            }
        }
    }

    private void saveErrorLog(String taskId, String error) {
        try {
            taskManager.saveTaskLogs(taskId, error);
        } catch (Exception e) {
            log.error("Failed to save error log for task {}", taskId, e);
        }
    }

    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}