package com.mypay.paymentgateway.domain.aggregates.payment.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.psp.CaptureID

data class CapturedPaymentEvent(
    val aggregateID: AggregateID,
    val version: Int,
    val capturedAmount: Money,
    val captureID: CaptureID
) :
    DomainEvent(aggregateID, version)
