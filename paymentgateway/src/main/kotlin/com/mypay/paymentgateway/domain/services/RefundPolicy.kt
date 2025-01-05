package com.mypay.paymentgateway.domain.services

import com.mypay.paymentgateway.domain.payment.Payment

interface RefundPolicy {
    fun isRefundable(payment: Payment): Boolean
}