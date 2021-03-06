package com.waben.stock.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 */
@SpringBootApplication
@EnableConfigServer
@EnableEurekaClient
@RestController
public class ConfigApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigApplication.class);
    }

    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }
}
