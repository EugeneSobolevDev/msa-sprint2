package com.hotelio.booking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelio.booking.service.XFeatureService;
import com.hotelio.booking.service.XFeatureServiceV2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(
            name = {"ENABLE_FEATURE_X"
            })
    public XFeatureService xFeatureService() {
        return new XFeatureServiceV2();
    }
}
