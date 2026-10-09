package com.example.paymentsearch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.paymentsearch.dto.PaymentSearchRequest;
import com.example.paymentsearch.dto.PaymentSearchResponse;
import com.example.paymentsearch.repository.PaymentSearchRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentSearchServiceTest {

    @Mock
    private PaymentSearchRepository repository;

    @InjectMocks
    private PaymentSearchService service;

    @Test
    void normalizesMonthAndBuildsPagedResponse() {
        PaymentSearchRequest request = request("09/2026", "$1,234.50");
        List<PaymentSearchResponse> expected = List.of();
        when(repository.find(request, 1L, "2026-09", new BigDecimal("1234.50")))
                .thenReturn(expected);
        when(repository.count(request, 1L, "2026-09", new BigDecimal("1234.50")))
                .thenReturn(21L);

        var result = service.findPaymentRecords(request, 1L);

        assertEquals(expected, result.paymentEntries());
        assertEquals(0, result.pageNo());
        assertEquals(10, result.pageSize());
        assertEquals(21L, result.totalElements());
        assertEquals(3, result.totalPages());
        verify(repository).find(request, 1L, "2026-09", new BigDecimal("1234.50"));
        verify(repository).count(request, 1L, "2026-09", new BigDecimal("1234.50"));
    }

    @Test
    void rejectsUnsupportedMonthBeforeQuerying() {
        PaymentSearchRequest request = request("2026/09", null);

        assertThrows(IllegalArgumentException.class,
                () -> service.findPaymentRecords(request, 1L));
        verifyNoInteractions(repository);
    }

    @Test
    void supportsMonthWithDash() {
        PaymentSearchRequest request = request("09-2026", null);
        when(repository.find(request, 1L, "2026-09", null)).thenReturn(List.of());
        when(repository.count(request, 1L, "2026-09", null)).thenReturn(0L);

        service.findPaymentRecords(request, 1L);

        ArgumentCaptor<String> month = ArgumentCaptor.forClass(String.class);
        verify(repository).find(org.mockito.ArgumentMatchers.eq(request),
                org.mockito.ArgumentMatchers.eq(1L), month.capture(),
                org.mockito.ArgumentMatchers.isNull());
        assertEquals("2026-09", month.getValue());
    }

    private PaymentSearchRequest request(String accountingMonth, String search) {
        PaymentSearchRequest request = new PaymentSearchRequest();
        request.setCustomerId(42L);
        request.setAccountingMonth(accountingMonth);
        request.setSearch(search);
        request.setPageNo(0);
        request.setPageSize(10);
        return request;
    }
}
