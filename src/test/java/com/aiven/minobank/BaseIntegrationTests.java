package com.aiven.minobank;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

@SpringBootTest
@Import(TestContainersConfiguration.class)
public abstract class BaseIntegrationTests {
    @Autowired
    public JdbcTemplate jdbcTemplate;

    @BeforeEach()
    void clearDb() {
        jdbcTemplate.execute("TRUNCATE TABLE users, user_roles, transactions, accounts, ledger_entries RESTRICT");
        jdbcTemplate.execute("DELETE FROM roles WHERE name LIKE 'test-%'");
    }

    protected UUID seedBasicTransaction() {
        UUID user = seedBasicUser("dev@gmail.com");
        return jdbcTemplate.queryForObject(
                "INSERT INTO transactions (initiated_by, idempotency_key, request_hash, type, reference) VALUES (?, 123, 'asd', 'DEPOSIT', 'sdf') RETURNING id",
                UUID.class,
                user);
    }
    protected UUID seedBasicTransaction(UUID userId, String idempotencyKey) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO transactions (initiated_by, idempotency_key, request_hash, type) VALUES (?, ?, 'asdasd', 'DEPOSIT') RETURNING id",
                UUID.class, userId, idempotencyKey);
    }
    protected UUID seedBasicTransaction(UUID userId, String idempotencyKey, String type, UUID reverses_id) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO transactions (initiated_by, idempotency_key, request_hash, type, reverses_transaction_id) VALUES (?, ?, 'asdasd', ?, ?) RETURNING id",
                UUID.class, userId, idempotencyKey, type, reverses_id);
    }

    protected UUID seedAccount(String iban) {
        return jdbcTemplate.queryForObject("INSERT INTO accounts (iban, currency, type) VALUES (?, 'USD', 'CUSTOMER') RETURNING id", UUID.class, iban);
    }

    protected UUID seedAccount(String iban, String currency, String type) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO accounts (iban, currency, type) VALUES (?, ?, ?) RETURNING id",
                UUID.class, iban, currency, type);
    }

    protected UUID seedBasicTransaction(UUID userId) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO transactions (initiated_by, idempotency_key, request_hash, type) "
                        + "VALUES (?, ?, ?, 'DEPOSIT') RETURNING id",
                UUID.class, userId, UUID.randomUUID().toString(), "test-hash");
    }

    protected Long seedEntry(UUID transactionId, UUID accountId, String currency, long amountMinor) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO ledger_entries (transaction_id, account_id, amount_minor, currency, balance_after_minor) "
                        + "VALUES (?, ?, ?, ?, ?) RETURNING id",
                Long.class, transactionId, accountId, amountMinor, currency, amountMinor);
    }

    protected UUID seedBasicUser(String email, String pass, int loginAttempts) {
        return jdbcTemplate.queryForObject("INSERT INTO users (email, password_hash, failed_login_attempts) VALUES (?, ?, ?) RETURNING id",
                UUID.class, email, pass, loginAttempts);
    }

    protected UUID seedBasicUser(String email) {
        return jdbcTemplate.queryForObject("INSERT INTO users (email, password_hash) VALUES (?, ?) RETURNING id",
                UUID.class, email, "pass");
    }

    protected UUID seedBasicUser(String email, String pass, int loginAttempts, String status) {
        return jdbcTemplate.queryForObject("INSERT INTO users (email, password_hash, failed_login_attempts, status) VALUES (?, ?, ?, ?) RETURNING id",
               UUID.class, email, pass, loginAttempts, status);
    }
}
