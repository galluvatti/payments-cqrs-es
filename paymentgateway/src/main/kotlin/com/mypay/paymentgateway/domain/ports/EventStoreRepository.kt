package com.mypay.paymentgateway.domain.ports

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.EventModel
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface EventStoreRepository : MongoRepository<EventModel, AggregateID> {
    fun findByAggregateId(aggregateId: String): List<EventModel>
}