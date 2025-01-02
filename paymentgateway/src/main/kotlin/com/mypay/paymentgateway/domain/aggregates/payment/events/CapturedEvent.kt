package com.mypay.paymentgateway.domain.aggregates.payment.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.psp.CaptureID

class CapturedEvent(
    aggregateID: AggregateID,
    version: Int,
    val captureAmount: Money,
    val captureID: CaptureID
) :
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "CapturedEvent(captureAmount=$captureAmount, captureID=$captureID)"
    }
}
