package com.quickbite.orderservice.scheduler;

import com.quickbite.orderservice.entity.OutboxEntity;
import com.quickbite.orderservice.config.RabbitMQConfig;
import com.quickbite.orderservice.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Component
public class OutboxScheduler {

    private static final int MAX_RETRIES = 5;
    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final TransactionTemplate transactionTemplate;

    public OutboxScheduler(OutboxRepository outboxRepository,
                           RabbitTemplate rabbitTemplate,
                           TransactionTemplate transactionTemplate) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.transactionTemplate=transactionTemplate;
    }

    // Запуск каждые 3 секунды (3000 мс)
    @Scheduled(fixedDelay = 3000)
    public void processOutboxMessages() {
        List<OutboxEntity> pendingMessages = transactionTemplate.execute(status ->
                outboxRepository.findPendingForUpdate(100)
        );
        if (pendingMessages.isEmpty()) {
            return;
        }

        log.info("Найдено {} неотправленных сообщений в Outbox. Начинаем отправку...", pendingMessages.size());

        for (OutboxEntity outbox : pendingMessages) {
            try {
                // Создаем AMQP-сообщение с нужными свойствами
                MessageProperties properties = new MessageProperties();
                properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);

                // Передаем сырую JSON-строку
                Message message = new Message(outbox.getPayload().getBytes(), properties);

                // Отправляем в RabbitMQ
                rabbitTemplate.send("", RabbitMQConfig.QUEUE_NAME, message);

                // Помечаем как успешно отправленное
                outbox.setStatus(OutboxEntity.OutboxStatus.PROCESSED);
                outboxRepository.save(outbox);

                log.info("Событие outboxId={} успешно отправлено в RabbitMQ", outbox.getId());

            } catch (Exception e) {
                log.error("Ошибка при отправке события outboxId={} в RabbitMQ", outbox.getId(), e);
                // Обработка ошибок и защита от Poison Pill
                transactionTemplate.executeWithoutResult(status -> {
                    outbox.incrementRetryCount();
                    if (outbox.getRetryCount() >= MAX_RETRIES) {
                        outbox.setStatus(OutboxEntity.OutboxStatus.FAILED);
                        log.error("Событие outboxId={} превысило лимит попыток ({}) и помечено FAILED",
                                outbox.getId(), MAX_RETRIES);
                    }
                    outboxRepository.save(outbox);
                });
            }
        }
    }
}