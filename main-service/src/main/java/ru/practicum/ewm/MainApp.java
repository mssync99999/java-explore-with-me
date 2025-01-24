package ru.practicum.ewm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
import org.springframework.boot.web.client.RestTemplateBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.webclient.StatsClient;


import java.time.LocalDateTime;
import java.util.List;*/

@Slf4j
@SpringBootApplication
public class MainApp {
    public static void main(String[] args) {

        SpringApplication.run(MainApp.class, args);
/*
        StatsClient statsClient = new StatsClient("http://localhost:9090", new RestTemplateBuilder());

        EndpointHitDto endpointHitDto1 = EndpointHitDto.builder()
                .app("main-service")
                .uri("/events/10")
                .ip("111.222.100.111")
                .timestamp(LocalDateTime.now())
                .build();

        EndpointHitDto endpointHitDto2 = EndpointHitDto.builder()
                .app("main-service")
                .uri("/events/10")
                .ip("111.222.100.112")
                .timestamp(LocalDateTime.now())
                .build();

        EndpointHitDto endpointHitDto3 = EndpointHitDto.builder()
                .app("main-service")
                .uri("/events/20")
                .ip("111.222.100.112")
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.create(endpointHitDto1);
        statsClient.create(endpointHitDto2);
        statsClient.create(endpointHitDto3);

        Object listObj = statsClient.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                List.of("/events/10", "/events/20"),
                Boolean.FALSE).getBody();

        log.info("statsClient.getStats(): {}", listObj); */


    }


}