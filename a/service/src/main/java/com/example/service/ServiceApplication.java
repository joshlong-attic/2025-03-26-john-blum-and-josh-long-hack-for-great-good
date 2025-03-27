package com.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@SpringBootApplication
public class ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

    @Bean
    ToolCallbackProvider toolCallbackProvider(DogAdoptionScheduler dogAdoptionScheduler) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(dogAdoptionScheduler)
                .build();
    }
}

@Service
class DogAdoptionScheduler {

    private final ObjectMapper om;

    DogAdoptionScheduler(ObjectMapper om) {
        this.om = om;
    }

    @Tool(description = "schedule a pickup for the dog ")
    String scheduleDogForPickup(@ToolParam(description = "the id of the dog") int dogId,
                                @ToolParam(description = "the name of the dog") String dogName) throws Exception {
        var instant = this.om.writeValueAsString(
                Instant.now().plus(3, ChronoUnit.DAYS));
        System.out.println("scheduling pickup for dog " + dogId + " with name " + dogName + " at "
                + instant);
        return instant;

    }
}