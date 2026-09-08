package com.indicadoresar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IndicadoresArApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndicadoresArApplication.class, args);
    }
}
