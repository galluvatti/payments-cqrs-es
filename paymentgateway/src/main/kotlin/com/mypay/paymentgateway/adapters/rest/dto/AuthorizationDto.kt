package com.mypay.paymentgateway.adapters.rest.dto

data class AuthorizationDto(
    val amount: Double,
    val currency: String,
    val cardHolderName: String,
    val cardHolderSurname: String,
    val cardHolderEmail: String,
    val cardHolderCountry: String,
    val cardHolderCity: String,
    val cardHolderAddress: String,
    val cardNumber: String,
    val cardCvv: String,
    val cardExpiryMonth: Int,
    val cardExpiryYear: Int,
    val cardType: String,
    val orderId: String,
    val orderDescription: String
)