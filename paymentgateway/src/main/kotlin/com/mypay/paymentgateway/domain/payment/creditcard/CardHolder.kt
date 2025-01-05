package com.mypay.paymentgateway.domain.payment.creditcard

import com.mypay.paymentgateway.domain.payment.billing.FullName
import com.mypay.paymentgateway.domain.payment.billing.Email
import com.mypay.paymentgateway.domain.payment.billing.BillingDetails


data class CardHolder(
    val fullName: FullName,
    val billingDetails: BillingDetails,
    val email: Email
)
