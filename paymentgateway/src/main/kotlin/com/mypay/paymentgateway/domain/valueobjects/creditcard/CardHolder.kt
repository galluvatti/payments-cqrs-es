package com.mypay.paymentgateway.domain.valueobjects.creditcard

import com.mypay.paymentgateway.domain.valueobjects.AnagraphicDetails
import com.mypay.paymentgateway.domain.valueobjects.Email
import com.mypay.paymentgateway.domain.valueobjects.billing.BillingDetails


data class CardHolder(
    val anagraphicDetails: AnagraphicDetails,
    val billingDetails: BillingDetails,
    val email: Email
)
