package com.mypay.paymentgateway.adapters.rest

import com.github.michaelbull.result.mapBoth
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.infrastructure.CommandDispatcher
import com.mypay.paymentgateway.adapters.rest.dto.AuthorizationDto
import com.mypay.paymentgateway.application.commands.Authorize
import com.mypay.paymentgateway.application.commands.Capture
import com.mypay.paymentgateway.application.commands.Refund
import com.mypay.paymentgateway.domain.payment.Money
import com.mypay.paymentgateway.domain.payment.address.Address
import com.mypay.paymentgateway.domain.payment.address.City
import com.mypay.paymentgateway.domain.payment.address.Country
import com.mypay.paymentgateway.domain.payment.billing.BillingDetails
import com.mypay.paymentgateway.domain.payment.billing.Email
import com.mypay.paymentgateway.domain.payment.billing.FullName
import com.mypay.paymentgateway.domain.payment.creditcard.CardHolder
import com.mypay.paymentgateway.domain.payment.creditcard.CreditCard
import com.mypay.paymentgateway.domain.payment.merchant.Merchant
import com.mypay.paymentgateway.domain.payment.merchant.Order
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(path = ["/payments"])
class PaymentsController(
    @Autowired private val commandDispatcher: CommandDispatcher
) {
    private val logger = LoggerFactory.getLogger(PaymentsController::class.java)

    @PostMapping
    fun authorize(@RequestBody request: AuthorizationDto): ResponseEntity<Map<String, String>> {
        logger.info("New authorization request: $request")
        val paymentID = UUID.randomUUID()
        val result = commandDispatcher.send(
            Authorize(
                Merchant(request.merchantID),
                AggregateID(paymentID),
                Money(Currency.getInstance(request.currency), request.amount),
                CardHolder(
                    FullName(request.cardHolderName, request.cardHolderSurname),
                    BillingDetails(
                        Country(request.cardHolderCountry),
                        City(request.cardHolderCity),
                        Address(request.cardHolderAddress)
                    ),
                    Email(request.cardHolderEmail)
                ),
                CreditCard(
                    CreditCard.Pan(request.cardNumber),
                    request.cardCvv,
                    CreditCard.CardExpiration(request.cardExpiryMonth, request.cardExpiryYear),
                    CreditCard.CardBrand.valueOf(request.cardType)
                ),
                Order(request.orderId, request.orderDescription)
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
            Capture(
                AggregateID(UUID.fromString(paymentID)),
                amount
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
            Refund(
                AggregateID(UUID.fromString(paymentID)),
                amount
            )
        )
        return result.mapBoth(
            { _ -> ResponseEntity.status(HttpStatus.OK).build() },
            { ResponseEntity.status(it.httpStatusCode).body(mapOf("reason" to it.description)) }
        )
    }
}