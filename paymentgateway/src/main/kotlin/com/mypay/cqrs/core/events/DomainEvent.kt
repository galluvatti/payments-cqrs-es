package com.mypay.cqrs.core.events

import com.mypay.cqrs.core.aggregates.AggregateID

abstract class DomainEvent(val aggregateID: AggregateID, var version: Int) {
    val eventType: String = this.javaClass.simpleName
    override fun toString(): String {
        return "DomainEvent(aggregateID=$aggregateID, version=$version, eventType='$eventType')"
    }
}