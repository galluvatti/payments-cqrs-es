package com.mypay.cqrs.core.handlers

import com.github.michaelbull.result.Result
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.paymentgateway.domain.errors.DomainError

interface EventSourcingHandler<T> {
    fun save(aggregate: T): Result<Unit, DomainError>
    fun getById(aggregateID: AggregateID): T
}