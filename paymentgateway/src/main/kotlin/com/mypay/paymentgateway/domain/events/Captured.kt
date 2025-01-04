package com.mypay.paymentgateway.domain.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.valueobjects.Money

class Captured(
    aggregateID: AggregateID,
    version: Int,
    val captureAmount: Money
) :
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "Captured(captureAmount=$captureAmount) ${super.toString()}"
    }
}
