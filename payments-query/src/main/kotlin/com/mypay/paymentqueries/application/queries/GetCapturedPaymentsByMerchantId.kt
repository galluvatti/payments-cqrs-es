package com.mypay.paymentqueries.application.queries

import com.mypay.cqrs.core.queries.Query

data class GetCapturedPaymentsByMerchantId(val merchantId: String): Query()