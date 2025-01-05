package com.mypay.paymentgateway.domain.services

import org.springframework.stereotype.Service

private const val FEE_PERCENTAGE = 5

@Service
class FixedPercentageMerchantFees : MerchantFees {
    override fun calculate(transactionAmount: Double): Double {
        return transactionAmount / 100 * FEE_PERCENTAGE
    }
}