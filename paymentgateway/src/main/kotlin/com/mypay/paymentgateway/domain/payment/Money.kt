package com.mypay.paymentgateway.domain.payment

import java.util.Currency

data class Money(val currency: Currency, val amount: Double)
