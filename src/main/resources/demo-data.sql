INSERT INTO mtl_employer (employer_id, employer_name, employer_code)
VALUES
    (990000001, 'Acme Manufacturing Demo', 'ACME-DEMO'),
    (990000002, 'Northwind Demo Group', 'NORTHWIND-DEMO')
ON CONFLICT (employer_id) DO NOTHING;

INSERT INTO mtl_member (member_id, first_name, middle_name, last_name, ssn)
VALUES
    (990000001, 'Jordan', NULL, 'Example', 'DEMO-SSN-001'),
    (990000002, 'Taylor', 'R', 'Sample', 'DEMO-SSN-002')
ON CONFLICT (member_id) DO NOTHING;

INSERT INTO mtl_payment_type (payment_type_id, payment_type_code, source)
VALUES
    (990000001, 'CHECK', 'DEMO'),
    (990000002, 'ACH', 'DEMO')
ON CONFLICT (payment_type_id) DO NOTHING;

INSERT INTO mtl_ar_payment_status (payment_status_id, payment_status_code)
VALUES
    (990000001, 'FINALIZED'),
    (990000002, 'REVERSED')
ON CONFLICT (payment_status_id) DO NOTHING;

INSERT INTO mtl_ar_payment_sub_status (payment_sub_status_id, payment_sub_status_code)
VALUES
    (990000001, 'APPROVED'),
    (990000002, 'L2APPROVED'),
    (990000003, 'REVERSED')
ON CONFLICT (payment_sub_status_id) DO NOTHING;

INSERT INTO mtl_bank_account_info (bank_account_id, bank_account_name)
VALUES
    (990000001, 'Demo Operating Account'),
    (990000002, 'Demo Reserve Account')
ON CONFLICT (bank_account_id) DO NOTHING;

INSERT INTO mtl_ar_deposit_bundle (deposit_bundle_id, deposit_bundle_number)
VALUES
    (990000001, 'DEMO-BATCH-2026-1001'),
    (990000002, 'DEMO-BATCH-2026-1002')
ON CONFLICT (deposit_bundle_id) DO NOTHING;

INSERT INTO mtl_ar_payment_entry (
    payment_entry_id, tenant_id, deposit_bundle_id, customer_id, employer_id, member_id,
    bank_account_id, payment_type_id, payment_reference_no, amount_received,
    received_date, postmark_date, deposited_date, payment_status_id,
    payment_sub_status_id, accounting_month, is_active, balance_amount
)
VALUES
    (990000001, 1, 990000001, 990001, 990000001, 990000001,
     990000001, 990000001, 'DEMO-PAY-2026-1001', 1250.50,
     DATE '2026-10-01', DATE '2026-10-02', DATE '2026-10-03',
     990000001, 990000001, '2026-10', TRUE, 0),
    (990000002, 1, 990000001, 990001, 990000002, 990000002,
     990000002, 990000002, 'DEMO-PAY-2026-1002', 875.00,
     DATE '2026-10-02', DATE '2026-10-03', DATE '2026-10-04',
     990000001, 990000002, '2026-10', TRUE, 0),
    (990000003, 1, 990000002, 990001, 990000001, 990000002,
     990000001, 990000001, 'DEMO-PAY-2026-1003', 300.00,
     DATE '2026-10-03', DATE '2026-10-04', DATE '2026-10-05',
     990000002, 990000003, '2026-10', TRUE, 0)
ON CONFLICT (payment_entry_id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('mtl_employer', 'employer_id'),
              GREATEST((SELECT max(employer_id) FROM mtl_employer), 1));
SELECT setval(pg_get_serial_sequence('mtl_member', 'member_id'),
              GREATEST((SELECT max(member_id) FROM mtl_member), 1));
SELECT setval(pg_get_serial_sequence('mtl_payment_type', 'payment_type_id'),
              GREATEST((SELECT max(payment_type_id) FROM mtl_payment_type), 1));
SELECT setval(pg_get_serial_sequence('mtl_ar_payment_status', 'payment_status_id'),
              GREATEST((SELECT max(payment_status_id) FROM mtl_ar_payment_status), 1));
SELECT setval(pg_get_serial_sequence('mtl_ar_payment_sub_status', 'payment_sub_status_id'),
              GREATEST((SELECT max(payment_sub_status_id) FROM mtl_ar_payment_sub_status), 1));
SELECT setval(pg_get_serial_sequence('mtl_bank_account_info', 'bank_account_id'),
              GREATEST((SELECT max(bank_account_id) FROM mtl_bank_account_info), 1));
SELECT setval(pg_get_serial_sequence('mtl_ar_deposit_bundle', 'deposit_bundle_id'),
              GREATEST((SELECT max(deposit_bundle_id) FROM mtl_ar_deposit_bundle), 1));
SELECT setval(pg_get_serial_sequence('mtl_ar_payment_entry', 'payment_entry_id'),
              GREATEST((SELECT max(payment_entry_id) FROM mtl_ar_payment_entry), 1));
