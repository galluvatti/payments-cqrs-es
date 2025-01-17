package com.mypay.paymentqueries.application.handlers

import com.mypay.paymentqueries.application.queries.GetCapturedPaymentsByMerchantId
import com.mypay.paymentqueries.domain.Payment
import com.mypay.paymentqueries.domain.PaymentRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GetCapturedPaymentsByMerchantIdHandlerTest {

    @Test
    fun `should fetch from repository captured payments by merchant id`() {
        val merchantId = "merchantID"
        val paymentRepository = mockk<PaymentRepository>()
        every { paymentRepository.findPaymentsByMerchantIdWithSuccessfulCapture(merchantId) } returns
                listOf(mockk<Payment>(), mockk<Payment>())

        val result = GetCapturedPaymentsByMerchantIdHandler(paymentRepository).handle(
            GetCapturedPaymentsByMerchantId(merchantId)
        )

        assertThat(result.size).isEqualTo(2)
    }
}