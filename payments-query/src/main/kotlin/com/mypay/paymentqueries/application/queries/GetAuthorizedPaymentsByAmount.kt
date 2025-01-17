package com.mypay.paymentqueries.application.queries

data class GetAuthorizedPaymentsByAmount(val equalityType: EqualityType, val amount: Double)