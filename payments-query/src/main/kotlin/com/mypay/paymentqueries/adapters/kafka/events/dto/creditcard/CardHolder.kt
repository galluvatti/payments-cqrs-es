package com.mypay.paymentqueries.adapters.kafka.events.dto.creditcard

import com.mypay.paymentqueries.adapters.kafka.events.dto.billing.BillingDetails
import com.mypay.paymentqueries.adapters.kafka.events.dto.billing.Email
import com.mypay.paymentqueries.adapters.kafka.events.dto.billing.FullName


data class CardHolder(
    val fullName: FullName,
    val billingDetails: BillingDetails,
    val email: Email
)
