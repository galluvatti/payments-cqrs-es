package com.mypay.paymentqueries.domain

import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded

@Embeddable
data class CreditCard(
    val pan: String,
    val cvv: String,
    val expirationMonth: Int,
    val expirationYear: Int,
    val type: Brand,
    val holderFullName: String,
    val holderEmail: String,
    @Embedded
    val holderAddress: Address
)

enum class Brand {
    VISA,
    MASTERCARD,
    AMEX,
    DISCOVERY
}