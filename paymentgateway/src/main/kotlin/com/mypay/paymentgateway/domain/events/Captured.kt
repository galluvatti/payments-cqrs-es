package com.mypay.paymentgateway.domain.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.payment.Money
import java.time.LocalDateTime

class Captured(
    aggregateID: AggregateID,
    version: Int,
    val captureAmount: Money,
    val captureDate: LocalDateTime
) :
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "Captured(captureAmount=$captureAmount, captureDate=$captureDate) ${super.toString()}"
    }
}
