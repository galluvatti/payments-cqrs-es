package com.mypay.paymentqueries.adapters.kafka.events.dto.billing

import com.mypay.paymentqueries.adapters.kafka.events.dto.address.Address
import com.mypay.paymentqueries.adapters.kafka.events.dto.address.City
import com.mypay.paymentqueries.adapters.kafka.events.dto.address.Country

data class BillingDetails(
    val country: Country,
    val city: City,
    val address: Address,
)
