package com.mypay.paymentgateway.domain.ports.driver

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.commands.Command
import com.mypay.paymentgateway.domain.valueobjects.Money

data class CaptureCommand(
    val aggregateID: AggregateID,
    val captureAmount: Money,
) : Command(aggregateID)
