package com.gauravlad.shadowbase_backend.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DebeziumEventConsumer {

    private final ObjectMapper objectMapper;
    private final CdcReplayService cdcReplayService;

    public DebeziumEventConsumer(
            ObjectMapper objectMapper,
            CdcReplayService cdcReplayService) {

        this.objectMapper = objectMapper;
        this.cdcReplayService = cdcReplayService;
    }

    @KafkaListener(
            topics = "shadowbase.public.customers",
            groupId = "shadowbase-cdc-consumer"
    )
    public void consume(String message) {

        try {

            System.out.println(
                    "\n========== CDC EVENT =========="
            );

            System.out.println(message);

            JsonNode event =
                    objectMapper.readTree(message);

            cdcReplayService.replay(event);

        } catch (Exception e) {

            System.err.println(
                    "Failed to process CDC event: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}