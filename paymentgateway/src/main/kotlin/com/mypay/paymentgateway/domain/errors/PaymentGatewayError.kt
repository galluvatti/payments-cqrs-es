package com.mypay.paymentgateway.domain.errors

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class PaymentGatewayError(val httpStatusCode: HttpStatusCode, val description: String)

data object InsufficientFunds: PaymentGatewayError(HttpStatus.BAD_REQUEST, "Insufficient funds on card")
data object PaymentAlreadyAuthorized: PaymentGatewayError(HttpStatus.FORBIDDEN, "Payment can't be authorized twice")