package com.gauravlad.shadowbase_backend.traffic;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traffic")
public class TrafficController {

    private final TrafficGenerator trafficGenerator;

    public TrafficController(TrafficGenerator trafficGenerator) {
        this.trafficGenerator = trafficGenerator;
    }

    @GetMapping("/generate/{environmentId}")
    public TrafficEvent generateEvent(
            @PathVariable Long environmentId) {

        return trafficGenerator.generateEvent(environmentId);
    }

    @GetMapping("/generate/{environmentId}/{count}")
    public List<TrafficEvent> generateEvents(
            @PathVariable Long environmentId,
            @PathVariable int count) {

        return trafficGenerator.generateEvents(
                environmentId,
                count);
    }
}