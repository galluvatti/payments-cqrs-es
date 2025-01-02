package com.mypay.paymentgateway.infrastructure

import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.cqrs.core.infrastructure.EventProducer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class PaymentEventProducer(
    @Autowired @Value("\${spring.kafka.topic}") private val topic: String,
    @Autowired private val kafkaTemplate: KafkaTemplate<String, Any>

) : EventProducer {
    private val logger = LoggerFactory.getLogger(PaymentEventProducer::class.java)

    override fun produce(event: DomainEvent) {
        logger.info("Sending event $event to Kafka topic $topic")
        kafkaTemplate.send(topic, event)
    }
}