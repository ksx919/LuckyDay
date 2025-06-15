package com.luckyDay.recommend;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.luckyDay.recommend", "com.luckyDay.common"})
@EnableDubbo
public class RecommendApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecommendApplication.class, args);
    }
}
