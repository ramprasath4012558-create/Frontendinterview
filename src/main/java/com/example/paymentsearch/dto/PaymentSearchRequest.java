package com.example.paymentsearch.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class PaymentSearchRequest {

    @NotNull(message = "customerId is required")
    private Long customerId;

    @NotBlank(message = "accountingMonth is required")
    private String accountingMonth;

    private String search;
    private BigDecimal amount;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate depositDate;

    private String paymentType;
    private Long bankAccountName;

    @NotNull(message = "pageNo is required")
    @Min(value = 0, message = "pageNo must be zero or greater")
    private Integer pageNo;

    @NotNull(message = "pageSize is required")
    @Min(value = 1, message = "pageSize must be at least 1")
    @Max(value = 200, message = "pageSize must not exceed 200")
    private Integer pageSize;

    private String sortBy;
    private String sortDir;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getAccountingMonth() {
        return accountingMonth;
    }

    public void setAccountingMonth(String accountingMonth) {
        this.accountingMonth = accountingMonth;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDepositDate() {
        return depositDate;
    }

    public void setDepositDate(LocalDate depositDate) {
        this.depositDate = depositDate;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public Long getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(Long bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
}
