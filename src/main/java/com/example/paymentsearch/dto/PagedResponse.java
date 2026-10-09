package com.example.paymentsearch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PagedResponse<T>(
        @JsonProperty("reports") List<T> paymentEntries,
        int pageNo,
        int pageSize,
        long totalElements,
        int totalPages,
        Object pageable) {

    public PagedResponse(List<T> paymentEntries, int pageNo, int pageSize, long totalElements) {
        this(paymentEntries, pageNo, pageSize, totalElements,
                pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0, null);
    }
}
