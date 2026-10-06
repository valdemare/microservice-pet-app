package com.quickbite.analytics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;

@Configuration
public class KafkaRetryConfig {

    @Bean
    public RetryTopicConfiguration myRetryTopicConfiguration(KafkaTemplate<String, Object> template) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                // 1. Указываем интервал задержки (Backoff): начальная пауза 1000мс, множитель 2.0
                .exponentialBackoff(1000, 2.0, 10000)
                // 2. Указываем максимальное количество попыток (1 первая + 2 повтора)
                .maxAttempts(3)
                // 3. Указываем суффикс для топика сбойных сообщений
                .dltSuffix(".DLT")
                // 4. Стратегия: отправлять в DLT при исчерпании всех ретраев
                .dltProcessingFailureStrategy(DltStrategy.FAIL_ON_ERROR)
                // 5. Указываем топики, для которых применяется этот Retry
                .includeTopic("orders-stream")
                .create(template);
    }
}