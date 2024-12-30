package com.mypay.cqrs.core.infrastructure

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent

interface EventStore {
    fun saveEvents(aggregateID: AggregateID, events: Iterable<DomainEvent>, expectedVersion: Int)
    fun getEvents(aggregateID: AggregateID)
}