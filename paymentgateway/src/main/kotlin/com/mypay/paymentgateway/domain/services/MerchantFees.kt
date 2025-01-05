package com.mypay.paymentgateway.domain.services

interface MerchantFees {
    fun calculate(transactionAmount: Double): Double
}