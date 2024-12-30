package com.mypay.paymentgateway.domain.valueobjects

import java.util.Currency

data class Money(val currency: Currency, val amount: Double)
