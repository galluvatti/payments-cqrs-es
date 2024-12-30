package com.mypay.paymentgateway.domain.ports.driver

import com.mypay.cqrs.core.commands.CommandHandler
import org.springframework.stereotype.Service

@Service
class CaptureCommandHandler(
) : CommandHandler<CaptureCommand> {
    override fun handle(command: CaptureCommand) {
        TODO("Not yet implemented")
    }
}
