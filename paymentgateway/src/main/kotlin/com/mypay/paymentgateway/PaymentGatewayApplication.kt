package com.mypay.paymentgateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaymentGatewayApplication

fun main(args: Array<String>) {
	runApplication<PaymentGatewayApplication>(*args)
}
