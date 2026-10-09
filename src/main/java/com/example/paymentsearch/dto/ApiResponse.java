package com.example.paymentsearch.dto;

public record ApiResponse<T>(int status, String message, T data) {
}
