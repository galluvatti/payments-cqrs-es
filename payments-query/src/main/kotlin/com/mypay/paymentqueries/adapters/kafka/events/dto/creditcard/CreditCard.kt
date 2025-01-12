package com.mypay.paymentqueries.adapters.kafka.events.dto.creditcard

data class CreditCard(
    val pan: Pan,
    val cvv: String,
    val expiration: CardExpiration,
    val type: CardBrand
) {

    data class Pan(val value: String)

    data class CardExpiration(
        val month: Int,
        val year: Int
    )

    enum class CardBrand {
        VISA, MASTERCARD, AMEX, DISCOVERY
    }
}
