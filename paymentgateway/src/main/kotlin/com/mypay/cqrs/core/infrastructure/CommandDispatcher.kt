package com.mypay.cqrs.core.infrastructure

import com.github.michaelbull.result.Result
import com.mypay.cqrs.core.commands.Command
import com.mypay.cqrs.core.commands.CommandHandler
import com.mypay.paymentgateway.domain.errors.DomainError

interface CommandDispatcher {
    fun <T> registerHandler(type: Class<T>, handler: CommandHandler<T>) where T : Command
    fun send(command: Command): Result<Unit, DomainError>
}