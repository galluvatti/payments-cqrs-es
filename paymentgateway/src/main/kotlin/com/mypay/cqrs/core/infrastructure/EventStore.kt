package com.mypay.cqrs.core.infrastructure

import com.github.michaelbull.result.Result
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.errors.DomainError

interface EventStore {
    fun saveEvents(aggregateID: AggregateID, events: Iterable<DomainEvent>, expectedVersion: Int): Result<Unit, DomainError>
    fun getEvents(aggregateID: AggregateID): List<DomainEvent>
}