package com.mypay.paymentqueries.domain

import jakarta.persistence.*

@Entity
class Payment(
    @Id
    val id: String,
    @Embedded
    val creditCard: CreditCard,
    val merchantId: String,
    val orderId: String,
    val orderDescription: String,
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val transactions: MutableList<Transaction> = mutableListOf()
) {
    fun addTransaction(transaction: Transaction) {
        transactions.add(transaction)
    }
}