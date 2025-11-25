package com.example.demo.events;

import java.util.Map;

/**
 * Simple event abstraction. For now we log events.
 * Replace with AWS SNS/SQS, Kafka, or other broker in production.
 */
public interface EventPublisher {
    void publish(String eventType, Map<String, Object> payload);
}
