package com.enterprise.poodle.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

/**
 * Configuration for LLM (Language Learning Model) providers
 * Currently supports Google Gemini API via REST
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(LLMConfig.LLMProperties.class)
public class LLMConfig {

    private final LLMProperties llmProperties;

    public LLMConfig(LLMProperties llmProperties) {
        this.llmProperties = llmProperties;
    }

    @Bean
    public RestTemplate geminiRestTemplate(RestTemplateBuilder builder) {
        if (llmProperties.getApiKey() == null || llmProperties.getApiKey().isEmpty()) {
            log.warn("GEMINI_API_KEY not set - LLM features will use placeholder responses");
        }
        return builder.build();
    }

    /**
     * LLM Configuration Properties
     */
    @lombok.Data
    @ConfigurationProperties(prefix = "app.llm")
    public static class LLMProperties {
        private String provider = "gemini";
        private String apiKey;
        private String model = "gemini-2.5-flash";
    }
}
