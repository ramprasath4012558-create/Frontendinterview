package com.example.paymentsearch.repository;

import com.example.paymentsearch.dto.PaymentSearchRequest;
import com.example.paymentsearch.dto.PaymentSearchResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentSearchRepository {

    private static final String FROM_AND_JOINS = """
            FROM mtl_ar_payment_entry pe
            LEFT JOIN mtl_employer e ON e.employer_id = pe.employer_id
            LEFT JOIN mtl_member m ON m.member_id = pe.member_id
            LEFT JOIN mtl_payment_type pt ON pt.payment_type_id = pe.payment_type_id
            LEFT JOIN mtl_ar_payment_status ps ON ps.payment_status_id = pe.payment_status_id
            LEFT JOIN mtl_ar_payment_sub_status pss
                ON pss.payment_sub_status_id = pe.payment_sub_status_id
            LEFT JOIN mtl_bank_account_info ba ON ba.bank_account_id = pe.bank_account_id
            LEFT JOIN mtl_ar_deposit_bundle db ON db.deposit_bundle_id = pe.deposit_bundle_id
            """;

    private static final Map<String, String> SORT_COLUMNS = Map.ofEntries(
            Map.entry("paymentEntryId", "pe.payment_entry_id"),
            Map.entry("depositBatchNumber", "db.deposit_bundle_number"),
            Map.entry("employerName", "e.employer_name"),
            Map.entry("employerCode", "e.employer_code"),
            Map.entry("originalAmount", "pe.amount_received"),
            Map.entry("amount", "pe.amount_received"),
            Map.entry("paymentType", "pt.payment_type_code"),
            Map.entry("paymentReferenceNo", "pe.payment_reference_no"),
            Map.entry("paymentStatus", "ps.payment_status_code"),
            Map.entry("depositedDate", "pe.deposited_date"),
            Map.entry("memberName", "concat_ws(' ', m.first_name, m.middle_name, m.last_name)"));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PaymentSearchRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PaymentSearchResponse> find(PaymentSearchRequest request, long tenantId,
                                             String accountingMonth, BigDecimal searchAmount) {
        QueryParts query = buildWhereClause(request, tenantId, accountingMonth, searchAmount);
        String sortColumn = resolveSortColumn(request.getSortBy());
        String direction = resolveSortDirection(request.getSortDir());
        long offset = (long) request.getPageNo() * request.getPageSize();

        String sql = """
                SELECT pe.payment_entry_id,
                       db.deposit_bundle_number,
                       e.employer_name,
                       e.employer_code,
                       pe.amount_received,
                       pt.payment_type_code,
                       pe.payment_reference_no,
                       ps.payment_status_code,
                       pe.deposited_date,
                       NULLIF(concat_ws(' ', m.first_name, m.middle_name, m.last_name), '') AS member_name,
                       m.ssn,
                       pt.source,
                       ba.bank_account_name
                """ + FROM_AND_JOINS + query.whereClause()
                + " ORDER BY " + sortColumn + " " + direction
                + " NULLS LAST, pe.payment_entry_id DESC LIMIT :limit OFFSET :offset";

        MapSqlParameterSource parameters = query.parameters()
                .addValue("limit", request.getPageSize())
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, parameters, (rs, rowNum) -> new PaymentSearchResponse(
                rs.getLong("payment_entry_id"),
                rs.getString("deposit_bundle_number"),
                rs.getString("employer_name"),
                rs.getString("employer_code"),
                rs.getBigDecimal("amount_received"),
                rs.getString("payment_type_code"),
                rs.getString("payment_reference_no"),
                rs.getString("payment_status_code"),
                rs.getObject("deposited_date", java.time.LocalDate.class),
                rs.getString("member_name"),
                rs.getString("ssn"),
                rs.getString("source"),
                rs.getString("bank_account_name")));
    }

    public long count(PaymentSearchRequest request, long tenantId, String accountingMonth,
                      BigDecimal searchAmount) {
        QueryParts query = buildWhereClause(request, tenantId, accountingMonth, searchAmount);
        Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) " + FROM_AND_JOINS + query.whereClause(),
                query.parameters(), Long.class);
        return count == null ? 0 : count;
    }

    private QueryParts buildWhereClause(PaymentSearchRequest request, long tenantId,
                                        String accountingMonth, BigDecimal searchAmount) {
        List<String> predicates = new ArrayList<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("customerId", request.getCustomerId())
                .addValue("accountingMonth", accountingMonth);

        predicates.add("pe.tenant_id = :tenantId");
        predicates.add("pe.customer_id = :customerId");
        predicates.add("lower(pe.accounting_month) = lower(:accountingMonth)");
        predicates.add("""
                (
                    (ps.payment_status_code IN ('FINALIZED', 'ADJUSTED')
                        AND pss.payment_sub_status_code IN
                            ('APPROVED', 'CORRECTIONAPPROVED', 'L2APPROVED'))
                    OR (ps.payment_status_code = 'REVERSED'
                        AND pss.payment_sub_status_code = 'REVERSED')
                )
                """);

        if (request.getSearch() != null && !request.getSearch().isBlank()) {
            String search = request.getSearch().trim();
            String pattern = "%" + search.toLowerCase() + "%";
            predicates.add("""
                    (
                        lower(e.employer_name) LIKE :search
                        OR lower(e.employer_code) LIKE :search
                        OR cast(e.employer_id AS varchar) LIKE :search
                        OR cast(db.deposit_bundle_number AS varchar) LIKE :search
                        OR lower(concat_ws(' ', m.first_name, m.middle_name, m.last_name)) LIKE :search
                        OR cast(m.ssn AS varchar) LIKE :search
                        OR lower(pe.payment_reference_no) = :exactSearch
                        %s
                    )
                    """.formatted(searchAmount == null
                    ? ""
                    : "OR pe.amount_received = :searchAmount"));
            parameters.addValue("search", pattern)
                    .addValue("exactSearch", search.toLowerCase());
            if (searchAmount != null) {
                parameters.addValue("searchAmount", searchAmount);
            }
        }
        if (request.getAmount() != null) {
            predicates.add("pe.amount_received = :amount");
            parameters.addValue("amount", request.getAmount());
        }
        if (request.getDepositDate() != null) {
            predicates.add("pe.deposited_date = :depositDate");
            parameters.addValue("depositDate", request.getDepositDate());
        }
        if (request.getPaymentType() != null && !request.getPaymentType().isBlank()) {
            predicates.add("pt.payment_type_code = :paymentType");
            parameters.addValue("paymentType", request.getPaymentType());
        }
        if (request.getBankAccountName() != null) {
            predicates.add("ba.bank_account_id = :bankAccountId");
            parameters.addValue("bankAccountId", request.getBankAccountName());
        }

        return new QueryParts(" WHERE " + String.join(" AND ", predicates), parameters);
    }

    private String resolveSortColumn(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "pe.deposited_date";
        }
        String column = SORT_COLUMNS.get(sortBy);
        if (column == null) {
            throw new IllegalArgumentException("Unsupported sortBy value: " + sortBy);
        }
        return column;
    }

    private String resolveSortDirection(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            return "DESC";
        }
        if ("asc".equalsIgnoreCase(sortDir)) {
            return "ASC";
        }
        if ("desc".equalsIgnoreCase(sortDir)) {
            return "DESC";
        }
        throw new IllegalArgumentException("sortDir must be either 'asc' or 'desc'");
    }

    private record QueryParts(String whereClause, MapSqlParameterSource parameters) {
    }
}
