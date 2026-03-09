package com.iiesoftware.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "com.iiesoftware.platform")
public class PlatformProperties {

    private Docker docker = new Docker();
    private Paths paths = new Paths();

    public static class Docker {
        private String imageName = "algo-runner:1.0";
        private int timeoutMinutes = 10;
        private String socketPath = "unix:///var/run/docker.sock";

        // getter/setter
        public String getImageName() { return imageName; }
        public void setImageName(String imageName) { this.imageName = imageName; }
        public int getTimeoutMinutes() { return timeoutMinutes; }
        public void setTimeoutMinutes(int timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
        public String getSocketPath() { return socketPath; }
        public void setSocketPath(String socketPath) { this.socketPath = socketPath; }
    }

    public static class Paths {
        private String algorithms = "./algorithms";
        private String datasets = "./datasets";
        private String tasks = "./tasks";

        // getter/setter
        public String getAlgorithms() { return algorithms; }
        public void setAlgorithms(String algorithms) { this.algorithms = algorithms; }
        public String getDatasets() { return datasets; }
        public void setDatasets(String datasets) { this.datasets = datasets; }
        public String getTasks() { return tasks; }
        public void setTasks(String tasks) { this.tasks = tasks; }
    }

    public Docker getDocker() { return docker; }
    public void setDocker(Docker docker) { this.docker = docker; }
    public Paths getPaths() { return paths; }
    public void setPaths(Paths paths) { this.paths = paths; }
}