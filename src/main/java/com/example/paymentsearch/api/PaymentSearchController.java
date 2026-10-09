package com.example.paymentsearch.api;

import com.example.paymentsearch.dto.ApiResponse;
import com.example.paymentsearch.dto.PaymentSearchRequest;
import com.example.paymentsearch.dto.PagedResponse;
import com.example.paymentsearch.dto.PaymentSearchResponse;
import com.example.paymentsearch.service.PaymentSearchService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentSearchController {

    private final PaymentSearchService paymentSearchService;

    public PaymentSearchController(PaymentSearchService paymentSearchService) {
        this.paymentSearchService = paymentSearchService;
    }

    @GetMapping(value = "/find", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PagedResponse<PaymentSearchResponse>>> findPaymentRecords(
            @Valid @ModelAttribute PaymentSearchRequest request) {
        PagedResponse<PaymentSearchResponse> records =
                paymentSearchService.findPaymentRecords(request, 1L);
        return ResponseEntity.ok(new ApiResponse<>(
                200, "Payment records fetched successfully", records));
    }
}
