package com.mypay.paymentqueries.domain

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface PaymentRepository : CrudRepository<Payment, String> {

    @Query("""
        SELECT DISTINCT p 
        FROM Payment p 
        JOIN p.transactions t 
        WHERE p.merchantId = :merchantId AND t.type = 'CAPTURE' AND t.result = 'OK'
    """)
    fun findPaymentsByMerchantIdWithCaptureResultOk(@Param("merchantId") merchantId: String): List<Payment>

    @Query("""
        SELECT DISTINCT p
        FROM Payment p
        JOIN p.transactions t
        WHERE t.type = 'AUTHORIZE' AND t.amount > :amount AND t.result = 'OK'
    """)
    fun findPaymentsWithSuccessfulAuthorizationAmountGreaterThan(@Param("amount") amount: Double): List<Payment>

    @Query("SELECT SUM(t.fees) FROM Payment p JOIN p.transactions t WHERE p.merchantId = :merchantId")
    fun findTotalFeesByMerchantId(@Param("merchantId") merchantId: String): Double?
}