package com.mypay.cqrs.core.infrastructure

import com.mypay.cqrs.core.events.DomainEvent
import org.springframework.stereotype.Service

@Service
class KafkaEventProducer : EventProducer {
    override fun produce(topic: String, event: DomainEvent) {
        TODO("Not yet implemented")
    }
}