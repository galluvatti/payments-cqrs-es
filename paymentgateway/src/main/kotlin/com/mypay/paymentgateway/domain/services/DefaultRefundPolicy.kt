package com.mypay.paymentgateway.domain.services

import com.mypay.paymentgateway.domain.payment.Payment
import org.springframework.stereotype.Service
import java.time.LocalDateTime

private const val REFUND_WINDOW_DAYS = 30L

@Service
class DefaultRefundPolicy : RefundPolicy {
    override fun isRefundable(payment: Payment): Boolean {
        return payment.getCaptureDate().plusDays(REFUND_WINDOW_DAYS).isAfter(LocalDateTime.now())
    }
}