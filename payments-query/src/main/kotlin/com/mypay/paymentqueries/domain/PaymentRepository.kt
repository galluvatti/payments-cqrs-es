package com.mypay.paymentqueries.domain

import org.springframework.data.repository.CrudRepository

interface PaymentRepository: CrudRepository<Payment, String> {
}