package com.mypay.paymentgateway.infrastructure

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.cqrs.core.infrastructure.EventStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PaymentEventSourcingHandler<T>(
    @Autowired private val eventStore: EventStore
) : EventSourcingHandler<T> {
    override fun save(aggregate: T) {
        TODO("Not yet implemented")
    }

    override fun getById(aggregateID: AggregateID): T {
        TODO("Not yet implemented")
    }
}