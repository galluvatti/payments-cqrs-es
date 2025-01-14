package com.mypay.paymentqueries.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Enumerated(EnumType.STRING)
    val type: TransactionType,
    val amount: Double,
    val currency: String,
    @Enumerated(EnumType.STRING)
    val result: TransactionResult,
    val fees: Double = 0.0,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class TransactionType {
    AUTHORIZE,
    CAPTURE,
    REFUND
}

enum class TransactionResult {
    OK,
    KO,
    FRAUD
}