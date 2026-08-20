package com.gauravlad.shadowbase_backend.traffic;

import com.gauravlad.shadowbase_backend.dto.TrafficReplayResult;
import com.gauravlad.shadowbase_backend.service.TrafficReplayService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traffic")
public class TrafficController {

    private final TrafficGenerator trafficGenerator;
    private final TrafficReplayService trafficReplayService;

    public TrafficController(
            TrafficGenerator trafficGenerator,
            TrafficReplayService trafficReplayService) {

        this.trafficGenerator = trafficGenerator;
        this.trafficReplayService = trafficReplayService;
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

    @PostMapping("/replay/{environmentId}/{count}")
    public TrafficReplayResult replayTraffic(
            @PathVariable Long environmentId,
            @PathVariable int count) {

        return trafficReplayService.replayTraffic(
                environmentId,
                count);
    }

    @GetMapping("/history/{environmentId}")
    public List<TrafficEvent> getTrafficHistory(
            @PathVariable Long environmentId) {

        return trafficReplayService
                .getTrafficHistory(environmentId);
    }
}