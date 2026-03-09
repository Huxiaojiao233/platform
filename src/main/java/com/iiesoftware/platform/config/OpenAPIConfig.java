package com.iiesoftware.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("算法执行平台 API")
                        .description("基于Docker的算法执行平台REST API接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("IIE Software")
                                .email("support@iiesoftware.com")
                                .url("https://www.iiesoftware.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(getServers());
    }

    private List<Server> getServers() {
        List<Server> servers = new ArrayList<>();

        Server localServer = new Server();
        localServer.setUrl("http://localhost:" + serverPort);
        localServer.setDescription("本地开发服务器");
        servers.add(localServer);

        return servers;
    }
}