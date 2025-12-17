package com.hotelio.booking.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XFeatureServiceV2 extends XFeatureService{

    public String ping(boolean fallback) {
        return "===ping-v2===";
    }

    @PostConstruct
    public void init() {
        log.info("XFeatureServiceV2 is initialized");
    }
}
