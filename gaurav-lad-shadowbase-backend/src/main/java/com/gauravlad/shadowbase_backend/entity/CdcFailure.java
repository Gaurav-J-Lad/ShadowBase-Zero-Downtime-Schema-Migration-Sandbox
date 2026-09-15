package com.gauravlad.shadowbase_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cdc_failures")
public class CdcFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private Integer partitionNumber;

    @Column(nullable = false)
    private Long offsetValue;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime failedAt;

    public CdcFailure() {
    }

    public CdcFailure(
            String topic,
            Integer partitionNumber,
            Long offsetValue,
            String message,
            String errorMessage,
            LocalDateTime failedAt) {

        this.topic = topic;
        this.partitionNumber = partitionNumber;
        this.offsetValue = offsetValue;
        this.message = message;
        this.errorMessage = errorMessage;
        this.failedAt = failedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getPartitionNumber() {
        return partitionNumber;
    }

    public void setPartitionNumber(Integer partitionNumber) {
        this.partitionNumber = partitionNumber;
    }

    public Long getOffsetValue() {
        return offsetValue;
    }

    public void setOffsetValue(Long offsetValue) {
        this.offsetValue = offsetValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }
}
