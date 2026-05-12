package com.dataforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DataForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataForgeApplication.class, args);
    }
}
