package com.mypay.paymentqueries.domain

import jakarta.persistence.Embeddable

@Embeddable
data class Address(
    val country: String,
    val city: String,
    val street: String
)