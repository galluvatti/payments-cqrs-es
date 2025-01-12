package com.mypay.paymentqueries.adapters.kafka.events

import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentqueries.adapters.kafka.events.dto.AggregateID
import com.mypay.paymentqueries.adapters.kafka.events.dto.Money

class PaymentCaptureFailed(
    aggregateID: AggregateID,
    version: Int,
    val captureAmount: Money,
    val failureReason: String?= null
) :
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "PaymentCaptureFailed(captureAmount=$captureAmount, failureReason='$failureReason')"
    }
}
