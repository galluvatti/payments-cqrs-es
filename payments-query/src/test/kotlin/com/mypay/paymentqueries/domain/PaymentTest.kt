package com.mypay.paymentqueries.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PaymentTest {
    @Test
    fun `should add a transaction`() {
        val payment = Payment(
            "id",
            CreditCard(
                "4111111111111111",
                "123",
                1,
                2026,
                Brand.VISA,
                "John Doe",
                "john@mail,com",
                Address("IT", "Milan", "Via di casa mia 24")
            ),
            "merchantID",
            "orderID",
            "orderDescription",
            mutableListOf(Transaction(0, TransactionType.AUTHORIZE, 10.0, "EUR", TransactionResult.OK))
        )
        payment.addTransaction(
            Transaction(
                1,
                TransactionType.CAPTURE,
                10.0,
                "EUR",
                TransactionResult.OK
            )
        )

     assertThat(payment.getTransactions().size).isEqualTo(2)
    }
}