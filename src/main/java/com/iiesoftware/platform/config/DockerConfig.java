package com.iiesoftware.platform.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerConfig {

    @Bean
    public DockerClient dockerClient(PlatformProperties properties) {
        return DockerClientBuilder.getInstance(properties.getDocker().getSocketPath()).build();
    }
}