package com.mypay.cqrs.core.events

import com.mypay.cqrs.core.aggregates.AggregateID

abstract class DomainEvent(aggregateID: AggregateID, version: Int) {
    fun getType(): String {
        return javaClass.simpleName
    }
}