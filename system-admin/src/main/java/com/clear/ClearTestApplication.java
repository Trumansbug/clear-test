package com.clear;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.clear.mapper")
public class ClearTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClearTestApplication.class, args);
    }
} 