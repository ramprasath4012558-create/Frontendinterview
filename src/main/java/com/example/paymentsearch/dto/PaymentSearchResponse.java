package com.example.paymentsearch.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentSearchResponse(
        Long paymentEntryId,
        String depositBatchNumber,
        String employerName,
        String employerCode,
        BigDecimal originalAmount,
        String paymentType,
        String paymentReferenceNo,
        String paymentStatus,
        LocalDate depositedDate,
        String memberName,
        String ssn,
        String paymentTypeSource,
        String bankAccountName) {
}
