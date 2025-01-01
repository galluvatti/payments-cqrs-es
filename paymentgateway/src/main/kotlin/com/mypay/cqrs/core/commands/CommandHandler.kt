package com.mypay.cqrs.core.commands

import com.github.michaelbull.result.Result
import com.mypay.paymentgateway.domain.errors.PaymentGatewayError

interface CommandHandler<T> where T : Command {
    fun handle(command: T):Result<Unit, PaymentGatewayError>
}