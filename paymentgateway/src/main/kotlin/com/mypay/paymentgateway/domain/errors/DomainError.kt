package com.mypay.paymentgateway.domain.errors

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class DomainError(val httpStatusCode: HttpStatusCode, val description: String)

data object InsufficientFunds: DomainError(HttpStatus.BAD_REQUEST, "Insufficient funds on card")
data object PaymentAlreadyAuthorized: DomainError(HttpStatus.FORBIDDEN, "Payment can't be authorized twice")
data object OptimisticConcurrencyException: DomainError(HttpStatus.CONFLICT, "Optimistic concurrency violation. Trying to update a older version of the aggregate.")