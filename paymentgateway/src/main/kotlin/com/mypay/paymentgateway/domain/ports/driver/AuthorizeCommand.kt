package com.mypay.paymentgateway.domain.ports.driver

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.commands.Command
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.Order
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard

data class AuthorizeCommand(
    val aggregateID: AggregateID,
    val authorizationAmount: Money,
    val cardHolder: CardHolder,
    val creditCard: CreditCard,
    val order: Order
) : Command(aggregateID)
