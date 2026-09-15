package com.gauravlad.shadowbase_backend.service;

import com.gauravlad.shadowbase_backend.entity.CdcFailure;
import com.gauravlad.shadowbase_backend.repository.CdcFailureRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CdcDeadLetterConsumer {

    private final CdcFailureRepository cdcFailureRepository;

    public CdcDeadLetterConsumer(
            CdcFailureRepository cdcFailureRepository) {

        this.cdcFailureRepository = cdcFailureRepository;
    }

    @KafkaListener(
            topicPattern = "shadowbase\\.public\\..*\\.DLT",
            groupId = "shadowbase-cdc-dlt-consumer"
    )
    public void consume(
            ConsumerRecord<String, String> record) {

        System.out.println("========================================");
        System.out.println("CDC DLT EVENT RECEIVED");
        System.out.println("Topic: " + record.topic());
        System.out.println("Partition: " + record.partition());
        System.out.println("Offset: " + record.offset());
        System.out.println("Message: " + record.value());

        CdcFailure failure =
                new CdcFailure(
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.value(),
                        "CDC event processing failed after retries",
                        LocalDateTime.now()
                );

        cdcFailureRepository.save(failure);

        System.out.println(
                "CDC failure saved with ID: "
                        + failure.getId()
        );

        System.out.println("========================================");
    }
}
