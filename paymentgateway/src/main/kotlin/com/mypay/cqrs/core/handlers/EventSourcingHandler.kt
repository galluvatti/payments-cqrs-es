package com.mypay.cqrs.core.handlers

import com.mypay.cqrs.core.aggregates.AggregateID

interface EventSourcingHandler<T> {
    fun save(aggregate: T)
    fun getById(aggregateID: AggregateID): T
}