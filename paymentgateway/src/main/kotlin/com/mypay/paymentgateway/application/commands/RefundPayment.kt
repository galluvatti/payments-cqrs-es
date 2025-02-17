package com.mypay.paymentgateway.application.commands

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.commands.Command

data class RefundPayment(
    val aggregateID: AggregateID,
    val refundAmount: Double
) : Command(aggregateID)
