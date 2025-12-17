package com.hotelio.booking.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XFeatureService {

    @PostConstruct
    public void init() {
        log.info("XFeatureService is initialized");
    }
}
