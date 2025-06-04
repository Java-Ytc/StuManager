package com.example.stumanager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.stumanager.mapper")
@SpringBootApplication
public class StuManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StuManagerApplication.class, args);
    }

}
