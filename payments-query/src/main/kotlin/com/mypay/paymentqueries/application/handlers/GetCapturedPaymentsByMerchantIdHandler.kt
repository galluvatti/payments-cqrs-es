package com.mypay.paymentqueries.application.handlers

import com.mypay.cqrs.core.queries.QueryHandler
import com.mypay.paymentqueries.application.queries.GetCapturedPaymentsByMerchantId
import com.mypay.paymentqueries.domain.Payment
import com.mypay.paymentqueries.domain.PaymentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GetCapturedPaymentsByMerchantIdHandler(
    @Autowired val paymentRepository: PaymentRepository
) : QueryHandler<GetCapturedPaymentsByMerchantId, Payment> {

    override fun handle(query: GetCapturedPaymentsByMerchantId): List<Payment> {
        return paymentRepository.findPaymentsByMerchantIdWithCaptureResultOk(query.merchantId)
    }

}