package com.mypay.paymentqueries.domain

import jakarta.persistence.*
import java.util.*

@Entity
class Payment(
    id: String,
    @Embedded
    val creditCard: CreditCard,
    val merchantId: String,
    val orderId: String,
    val orderDescription: String,
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    private val transactions: MutableList<Transaction> = mutableListOf()
) : com.mypay.cqrs.core.entities.Entity(id) {
    fun addTransaction(transaction: Transaction) {
        transactions.add(transaction)
    }

    fun getTransactions(): List<Transaction> = Collections.unmodifiableList(transactions)
}