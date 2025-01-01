package com.mypay.paymentgateway.infrastructure

import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.cqrs.core.infrastructure.EventProducer
import org.springframework.stereotype.Service

@Service
class KafkaEventProducer : EventProducer {
    override fun produce(event: DomainEvent) {

    }
}