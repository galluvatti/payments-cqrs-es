package com.mypay.paymentqueries.application.handlers

import com.mypay.paymentqueries.application.queries.EqualityType
import com.mypay.paymentqueries.application.queries.GetAuthorizedPaymentsByAmount
import com.mypay.paymentqueries.domain.Payment
import com.mypay.paymentqueries.domain.PaymentRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GetAuthorizedPaymentsByAmountHandlerTest {

    @Test
    fun `should fetch from repository authorized payment with amount greater than`() {
        val paymentRepository = mockk<PaymentRepository>()
        val amount = 100.0
        every { paymentRepository.findAuthorizedPaymentsWithAmountGreaterThan(amount) } returns
                listOf(mockk<Payment>(), mockk<Payment>())

        val result = GetAuthorizedPaymentsByAmountHandler(paymentRepository).handle(
            GetAuthorizedPaymentsByAmount(EqualityType.GREATER_THAN, amount)
        )

        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun `should fetch from repository authorized payment with amount less than`() {
        val paymentRepository = mockk<PaymentRepository>()
        val amount = 100.0
        every { paymentRepository.findAuthorizedPaymentsWithAmountLessThan(amount) } returns
                listOf(mockk<Payment>(), mockk<Payment>())

        val result = GetAuthorizedPaymentsByAmountHandler(paymentRepository).handle(
            GetAuthorizedPaymentsByAmount(EqualityType.LESS_THAN, amount)
        )

        assertThat(result.size).isEqualTo(2)
    }
}