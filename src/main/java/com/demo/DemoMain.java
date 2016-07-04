package com.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.demo.dao.orientdb.OrientDbConfiguration;
import com.demo.services.OperationsProcessor;
import com.demo.web.WebConfiguration;
import com.generator.GeneratorConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAutoConfiguration
@Import({
    OrientDbConfiguration.class,
    GeneratorConfiguration.class,
    WebConfiguration.class
})
@ComponentScan(basePackageClasses = OperationsProcessor.class)
@EnableScheduling
@Slf4j
public class DemoMain {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(DemoMain.class)
            .run(args);
    }
}
