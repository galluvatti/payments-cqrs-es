package com.mypay.paymentgateway.domain.valueobjects.billing

import com.mypay.paymentgateway.domain.valueobjects.address.Address
import com.mypay.paymentgateway.domain.valueobjects.address.City
import com.mypay.paymentgateway.domain.valueobjects.address.Country

data class BillingDetails(
    val country: Country,
    val city: City,
    val address: Address,
)
