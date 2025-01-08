package com.mypay.paymentgateway.adapters.persistence

import com.github.michaelbull.result.*
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.cqrs.core.infrastructure.EventStore
import com.mypay.paymentgateway.domain.payment.Payment
import com.mypay.paymentgateway.domain.errors.DomainError
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PaymentRepository(
    @Autowired private val eventStore: EventStore
) : EventSourcingHandler<Payment> {
    private val logger = LoggerFactory.getLogger(PaymentRepository::class.java)

    override fun save(aggregate: Payment): Result<Unit, DomainError> {
        logger.info("Saving aggregate with id ${aggregate.id}")
        val result = eventStore.saveEvents(aggregate.id, aggregate.getUncommitedChanges(), aggregate.version)
            .onSuccess {
                aggregate.markChangesAsCommitted()
            }
        return result.mapBoth(
            { Ok(Unit) },
            { Err(it) }
        )

    }

    override fun getById(aggregateID: AggregateID): Payment {
        logger.info("Retrieving aggregate with id $aggregateID")
        val eventsStream = eventStore.getEvents(aggregateID)
        val aggregate = Payment(aggregateID)
        aggregate.replayEvents(eventsStream)
        return aggregate
    }
}