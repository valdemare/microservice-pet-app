package com.quickbite.notificationservice.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name:notification_queue}")
    private String mainQueueName;

    // Имена для DLQ
    public static final String DLX_NAME = "notification.dlx";
    public static final String DLQ_NAME = "notification.dlq";
    public static final String DLQ_ROUTING_KEY = "notification.dlq.routing.key";

    // 1. Объявляем Dead Letter Exchange
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME);
    }

    // 2. Объявляем Dead Letter Queue
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    // 3. Связываем DLQ с DLX
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY);
    }

    // 4. Основная очередь с перенаправлением в DLX
    @Bean
    public Queue mainQueue() {
        return QueueBuilder.durable(mainQueueName)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();

        DefaultClassMapper classMapper = new DefaultClassMapper();
        // Разрешаем маппинг любых входящих типов в наш локальный DTO
        classMapper.setTrustedPackages("*");

        Map<String, Class<?>> idClassMapping = new HashMap<>();
        // Указываем: если пришел __TypeId__ из order-service, сопоставь его с нашим DTO
        idClassMapping.put("com.quickbite.orderservice.dto.OrderCreatedEvent",
                com.quickbite.notificationservice.dto.OrderCreatedEvent.class);

        classMapper.setIdClassMapping(idClassMapping);
        classMapper.setDefaultType(com.quickbite.notificationservice.dto.OrderCreatedEvent.class);
        converter.setClassMapper(classMapper);

        return converter;
    }
}