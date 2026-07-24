package com.quickbite.orderservice.config;

import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "order.created.queue";
    /*
    // Создаем очередь в RabbitMQ с именем order.created.queue
    @Bean
    public Queue orderQueue() {
        return new Queue(QUEUE_NAME, true);
    }*/

    // Настраиваем сериализацию Java-объектов в JSON формат для RabbitMQ
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}