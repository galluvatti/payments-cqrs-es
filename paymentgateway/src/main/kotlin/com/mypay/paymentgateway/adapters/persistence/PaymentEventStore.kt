package com.mypay.paymentgateway.adapters.persistence

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.cqrs.core.events.EventModel
import com.mypay.cqrs.core.infrastructure.EventProducer
import com.mypay.cqrs.core.infrastructure.EventStore
import com.mypay.paymentgateway.domain.aggregates.payment.Payment
import com.mypay.paymentgateway.domain.errors.DomainError
import com.mypay.paymentgateway.domain.errors.OptimisticConcurrencyException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PaymentEventStore(
    @Autowired private val repository: EventStoreRepository,
    @Autowired private val eventProducer: EventProducer
) : EventStore {
    private val logger = LoggerFactory.getLogger(PaymentEventStore::class.java)

    override fun saveEvents(
        aggregateID: AggregateID,
        events: Iterable<DomainEvent>,
        expectedVersion: Int
    ): Result<Unit, DomainError> {
        val eventStream = repository.findByAggregateId(aggregateID.value.toString())
        if (expectedVersion != -1 && eventStream[eventStream.size - 1].version != expectedVersion) {
            return Err(OptimisticConcurrencyException)
        }
        var version = expectedVersion
        events.forEach {
            version++
            it.version = version
            repository.save(
                EventModel(
                    timestamp = Date(),
                    aggregateId = aggregateID.value.toString(),
                    aggregateType = Payment::javaClass.javaClass.simpleName,
                    version = it.version,
                    eventType = it.getType(),
                    eventData = it

                )
            )
            eventProducer.produce(it)
        }
        return Ok(Unit)
    }

    override fun getEvents(aggregateID: AggregateID): List<DomainEvent> {
        return repository.findByAggregateId(aggregateID.value.toString())
            .map { event -> event.eventData }
    }
}