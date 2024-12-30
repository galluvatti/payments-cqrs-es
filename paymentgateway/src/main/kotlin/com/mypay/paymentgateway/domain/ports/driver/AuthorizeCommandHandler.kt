package com.mypay.paymentgateway.domain.ports.driver

import com.mypay.cqrs.core.commands.CommandHandler
import org.springframework.stereotype.Service

@Service
class AuthorizeCommandHandler(
) : CommandHandler<AuthorizeCommand> {
    override fun handle(command: AuthorizeCommand) {
        TODO("Not yet implemented")
    }
}
