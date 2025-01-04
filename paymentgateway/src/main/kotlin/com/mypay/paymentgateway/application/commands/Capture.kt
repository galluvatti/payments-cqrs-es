package com.mypay.paymentgateway.application.commands

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.commands.Command

data class Capture(
    val aggregateID: AggregateID,
    val captureAmount: Double
) : Command(aggregateID)
