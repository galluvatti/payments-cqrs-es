package com.mypay.paymentqueries.adapters.kafka.events.dto

import java.util.Currency

data class Money(val currency: Currency, val amount: Double)
