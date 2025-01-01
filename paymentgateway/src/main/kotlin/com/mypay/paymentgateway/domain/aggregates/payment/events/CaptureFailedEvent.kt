package com.mypay.paymentgateway.domain.aggregates.payment.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.valueobjects.Money

data class CaptureFailedEvent(
    val aggregateID: AggregateID,
    val version: Int,
    val captureAmount: Money,
    val failureReason: String
) :
    DomainEvent(aggregateID, version)
