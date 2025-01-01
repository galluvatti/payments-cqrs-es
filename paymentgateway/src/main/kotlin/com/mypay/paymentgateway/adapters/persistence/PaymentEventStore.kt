package com.mypay.paymentgateway.adapters.persistence

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.cqrs.core.infrastructure.EventStore
import org.springframework.stereotype.Repository

@Repository
class PaymentEventStore : EventStore {
    override fun saveEvents(aggregateID: AggregateID, events: Iterable<DomainEvent>, expectedVersion: Int) {
        TODO("Not yet implemented")
    }

    override fun getEvents(aggregateID: AggregateID) {
        TODO("Not yet implemented")
    }
}