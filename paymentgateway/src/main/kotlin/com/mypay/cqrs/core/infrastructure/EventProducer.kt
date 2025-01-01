package com.mypay.cqrs.core.infrastructure

import com.mypay.cqrs.core.events.DomainEvent

interface EventProducer {
    fun produce(event: DomainEvent)
}