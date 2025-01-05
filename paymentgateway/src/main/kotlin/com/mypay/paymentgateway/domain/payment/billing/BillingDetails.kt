package com.mypay.paymentgateway.domain.payment.billing

import com.mypay.paymentgateway.domain.payment.address.Address
import com.mypay.paymentgateway.domain.payment.address.City
import com.mypay.paymentgateway.domain.payment.address.Country

data class BillingDetails(
    val country: Country,
    val city: City,
    val address: Address,
)
