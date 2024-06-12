package com.zerobase.customboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CustomBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomBoardApplication.class, args);
    }

}
