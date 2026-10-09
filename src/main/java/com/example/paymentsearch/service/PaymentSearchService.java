package com.example.paymentsearch.service;

import com.example.paymentsearch.dto.PagedResponse;
import com.example.paymentsearch.dto.PaymentSearchRequest;
import com.example.paymentsearch.dto.PaymentSearchResponse;
import com.example.paymentsearch.repository.PaymentSearchRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PaymentSearchService {

    private static final DateTimeFormatter STORAGE_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    private final PaymentSearchRepository paymentSearchRepository;

    public PaymentSearchService(PaymentSearchRepository paymentSearchRepository) {
        this.paymentSearchRepository = paymentSearchRepository;
    }

    public PagedResponse<PaymentSearchResponse> findPaymentRecords(
            PaymentSearchRequest request, Long tenantId) {
        if (request == null) {
            return new PagedResponse<>(List.of(), 0, 0, 0L);
        }
        String accountingMonth = normalizeAccountingMonth(request.getAccountingMonth());
        BigDecimal searchAmount = parseSearchAmount(request.getSearch());
        List<PaymentSearchResponse> records = paymentSearchRepository.find(
                request, tenantId, accountingMonth, searchAmount);
        long totalElements = paymentSearchRepository.count(
                request, tenantId, accountingMonth, searchAmount);
        return new PagedResponse<>(
                records, request.getPageNo(), request.getPageSize(), totalElements);
    }

    private String normalizeAccountingMonth(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("accountingMonth is required");
        }
        String month = value.trim();
        try {
            return YearMonth.parse(month).format(STORAGE_MONTH);
        } catch (DateTimeParseException ignored) {
            for (DateTimeFormatter formatter : List.of(
                    DateTimeFormatter.ofPattern("MM/yyyy"),
                    DateTimeFormatter.ofPattern("MM-yyyy"))) {
                try {
                    return YearMonth.parse(month, formatter).format(STORAGE_MONTH);
                } catch (DateTimeParseException ignoredFormat) {
                    // Try the next documented month format.
                }
            }
            throw new IllegalArgumentException(
                    "accountingMonth must be yyyy-MM, MM/yyyy, or MM-yyyy");
        }
    }

    private BigDecimal parseSearchAmount(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String numeric = search.trim().replaceAll("[$,]", "");
        if (numeric.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(numeric);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
