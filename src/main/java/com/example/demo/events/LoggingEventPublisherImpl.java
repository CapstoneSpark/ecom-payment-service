package com.example.demo.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LoggingEventPublisherImpl implements EventPublisher {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void publish(String eventType, Map<String, Object> payload) {
        log.info("EVENT PUBLISH [{}] payload={}", eventType, payload);
    }
}
