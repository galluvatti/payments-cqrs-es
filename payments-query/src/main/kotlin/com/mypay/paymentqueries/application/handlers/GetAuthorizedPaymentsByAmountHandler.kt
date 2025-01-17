package com.mypay.paymentqueries.application.handlers

import com.mypay.paymentqueries.application.queries.EqualityType
import com.mypay.paymentqueries.application.queries.GetAuthorizedPaymentsByAmount
import com.mypay.paymentqueries.domain.Payment
import com.mypay.paymentqueries.domain.PaymentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GetAuthorizedPaymentsByAmountHandler(
    @Autowired val paymentRepository: PaymentRepository
) {
    fun handle(query: GetAuthorizedPaymentsByAmount): List<Payment> {
        return if (query.equalityType == EqualityType.LESS_THAN)
            paymentRepository.findAuthorizedPaymentsWithAmountLessThan(query.amount)
        else paymentRepository.findAuthorizedPaymentsWithAmountGreaterThan(query.amount)
    }
}