package com.mypay.paymentgateway.domain.ports.driver

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.commands.Command
import com.mypay.paymentgateway.domain.ports.driven.PaymentProcessor

data class RefundCommand(
    val aggregateID: AggregateID,
    val refundAmount: Double,
    val paymentProcessor: PaymentProcessor
) : Command(aggregateID)
