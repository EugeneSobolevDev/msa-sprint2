package com.hotelio.booking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelio.booking.service.XFeatureService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnProperty(
            name = {"ENABLE_FEATURE_X"}
    )
    public XFeatureService xFeatureService() {
        return new XFeatureService();
    }
}
