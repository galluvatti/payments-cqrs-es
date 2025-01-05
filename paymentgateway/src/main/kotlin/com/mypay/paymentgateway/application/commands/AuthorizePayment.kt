package com.mypay.paymentgateway.application.commands

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.commands.Command
import com.mypay.paymentgateway.domain.payment.merchant.Merchant
import com.mypay.paymentgateway.domain.payment.Money
import com.mypay.paymentgateway.domain.payment.merchant.Order
import com.mypay.paymentgateway.domain.payment.creditcard.CardHolder
import com.mypay.paymentgateway.domain.payment.creditcard.CreditCard

data class AuthorizePayment(
    val merchant: Merchant,
    val aggregateID: AggregateID,
    val authorizationAmount: Money,
    val cardHolder: CardHolder,
    val creditCard: CreditCard,
    val order: Order
) : Command(aggregateID)
