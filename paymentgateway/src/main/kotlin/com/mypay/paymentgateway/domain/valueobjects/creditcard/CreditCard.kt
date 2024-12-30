package com.mypay.paymentgateway.domain.valueobjects.creditcard

data class CreditCard(
    val pan: String,
    val cvv: String,
    val expiration: CardExpiration,
    val type: CardType
) {
    data class CardExpiration(
        val month: Int,
        val year: Int
    )

    enum class CardType {
        VISA, MASTERCARD, AMEX
    }
}
