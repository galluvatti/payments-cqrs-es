package com.mypay.paymentgateway.adapters.rest

import com.github.michaelbull.result.mapBoth
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.infrastructure.CommandDispatcher
import com.mypay.paymentgateway.adapters.rest.dto.AuthorizationDto
import com.mypay.paymentgateway.domain.ports.driven.PaymentProcessor
import com.mypay.paymentgateway.domain.ports.driver.AuthorizeCommand
import com.mypay.paymentgateway.domain.ports.driver.CaptureCommand
import com.mypay.paymentgateway.domain.ports.driver.RefundCommand
import com.mypay.paymentgateway.domain.valueobjects.AnagraphicDetails
import com.mypay.paymentgateway.domain.valueobjects.Email
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.Order
import com.mypay.paymentgateway.domain.valueobjects.address.Address
import com.mypay.paymentgateway.domain.valueobjects.address.City
import com.mypay.paymentgateway.domain.valueobjects.address.Country
import com.mypay.paymentgateway.domain.valueobjects.billing.BillingDetails
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(path = ["/payments"])
class PaymentsController(
    @Autowired private val commandDispatcher: CommandDispatcher,
    @Autowired private val paymentProcessor: PaymentProcessor
) {
    private val logger = LoggerFactory.getLogger(PaymentsController::class.java)

    @PostMapping
    fun authorize(@RequestBody request: AuthorizationDto): ResponseEntity<Map<String, String>> {
        logger.info("New authorization request: $request")
        val paymentID = UUID.randomUUID()
        val result = commandDispatcher.send(
            AuthorizeCommand(
                AggregateID(paymentID),
                Money(Currency.getInstance(request.currency), request.amount),
                CardHolder(
                    AnagraphicDetails(request.cardHolderName, request.cardHolderSurname),
                    BillingDetails(
                        Country(request.cardHolderCountry),
                        City(request.cardHolderCity),
                        Address(request.cardHolderAddress)
                    ),
                    Email(request.cardHolderEmail)
                ),
                CreditCard(
                    request.cardNumber,
                    request.cardCvv,
                    CreditCard.CardExpiration(request.cardExpiryMonth, request.cardExpiryYear),
                    CreditCard.CardType.valueOf(request.cardType)
                ),
                Order(request.orderId, request.orderDescription),
                paymentProcessor
            )
        )
        return result.mapBoth(
            { _ -> ResponseEntity.status(HttpStatus.CREATED).body(mapOf("paymentID" to paymentID.toString())) },
            { ResponseEntity.status(it.httpStatusCode).body(mapOf("reason" to it.description)) }
        )
    }

    @PutMapping(path = ["/{id}"])
    fun capture(
        @PathVariable(value = "id") paymentID: String,
        @RequestParam(value = "amount") amount: Double
    ): ResponseEntity<Any> {
        logger.info("New capture request, id : $paymentID amount: $amount")
        val result = commandDispatcher.send(
            CaptureCommand(
                AggregateID(UUID.fromString(paymentID)),
                amount,
                paymentProcessor
            )
        )
        return result.mapBoth(
            { _ -> ResponseEntity.status(HttpStatus.OK).build() },
            { ResponseEntity.status(it.httpStatusCode).body(mapOf("reason" to it.description)) }
        )
    }

    @DeleteMapping(path = ["/{id}"])
    fun refund(
        @PathVariable(value = "id") paymentID: String,
        @RequestParam(value = "amount") amount: Double
    ): ResponseEntity<Any> {
        logger.info("New refund request, id : $paymentID amount: $amount")
        val result = commandDispatcher.send(
            RefundCommand(
                AggregateID(UUID.fromString(paymentID)),
                amount,
                paymentProcessor
            )
        )
        return result.mapBoth(
            { _ -> ResponseEntity.status(HttpStatus.OK).build() },
            { ResponseEntity.status(it.httpStatusCode).body(mapOf("reason" to it.description)) }
        )
    }
}