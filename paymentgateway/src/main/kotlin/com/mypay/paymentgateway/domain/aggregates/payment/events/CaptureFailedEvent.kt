package com.mypay.paymentgateway.domain.aggregates.payment.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.valueobjects.Money

class CaptureFailedEvent(
    aggregateID: AggregateID,
    version: Int,
    val captureAmount: Money,
    val failureReason: String
) :
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "CaptureFailedEvent(captureAmount=$captureAmount, failureReason='$failureReason')"
    }
}
