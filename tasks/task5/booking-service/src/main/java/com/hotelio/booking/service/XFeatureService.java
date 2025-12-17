package com.hotelio.booking.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class XFeatureService {

    public String ping(boolean fallback) {
        if (fallback) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Service is currently unavailable"
            );
        }
        return "=ping-v1=";
    }

    @PostConstruct
    public void init() {
        log.info("XFeatureServiceV1 is initialized");
    }
}
