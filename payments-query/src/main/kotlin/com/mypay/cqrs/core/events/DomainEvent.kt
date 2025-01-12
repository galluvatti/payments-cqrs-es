package com.mypay.cqrs.core.events

import com.mypay.paymentqueries.adapters.kafka.events.dto.AggregateID

abstract class DomainEvent(val aggregateID: AggregateID, var version: Int) {
    val eventType: String = this.javaClass.simpleName
    override fun toString(): String {
        return "DomainEvent(aggregateID=$aggregateID, version=$version, eventType='$eventType')"
    }
}