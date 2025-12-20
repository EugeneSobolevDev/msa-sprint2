package com.hotelio.booking.controller;

import com.hotelio.booking.service.XFeatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PingController {
    private final XFeatureService xFeatureService;


    @GetMapping("/ping")
    public String ping(@RequestHeader(value = "X-Fallback", required = false) String fallbackHeader) {
        boolean fallback = "true".equals(fallbackHeader);
        log.info(xFeatureService.ping(fallback));
        return xFeatureService.ping(fallback);
    }
}
