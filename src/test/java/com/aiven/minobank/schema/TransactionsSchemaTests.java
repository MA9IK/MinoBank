package com.aiven.minobank.schema;

import com.aiven.minobank.BaseIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class TransactionsSchemaTests extends BaseIntegrationTests {

    private UUID seedBasicTransaction() {
        UUID user = seedBasicUser("dev@gmail.com");
        return jdbcTemplate.queryForObject(
                "INSERT INTO transactions (initiated_by, idempotency_key, request_hash, type, reference) VALUES (?, 123, 'asd', 'DEPOSIT', 'sdf') RETURNING id",
                UUID.class,
                user);
    }
    private UUID seedBasicTransaction(UUID userId, String idempotencyKey) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO transactions (initiated_by, idempotency_key, request_hash, type) VALUES (?, ?, 'asdasd', 'DEPOSIT') RETURNING id",
                UUID.class, userId, idempotencyKey);
    }
    private UUID seedBasicTransaction(UUID userId, String idempotencyKey, String type, UUID reverses_id) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO transactions (initiated_by, idempotency_key, request_hash, type, reverses_transaction_id) VALUES (?, ?, 'asdasd', ?, ?) RETURNING id",
                UUID.class, userId, idempotencyKey, type, reverses_id);
    }

    @Test
    public void acceptingDefaultInsertWithRightDefault() {
        UUID trans = seedBasicTransaction();

        assertThat(
                jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM transactions WHERE id = ?",
                        Integer.class,
                        trans)
        )
                .isEqualTo(1);
        assertThat(
                jdbcTemplate.queryForObject(
                        "SELECT status FROM transactions WHERE id = ?",
                        String.class,
                        trans)
        )
                .isEqualTo("PENDING");
    }

    @Test
    public void rejectsSameUserWithSameKey() {
        UUID user = seedBasicUser("dev@gmail.com");
        seedBasicTransaction(user, "key-1");
        assertThatThrownBy(() -> seedBasicTransaction(user, "key-1"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_transactions_user_idempotency_key");
    }

    @Test
    public void acceptsSameIdempotencyKeyForDifferentUsers() {
        UUID user1 = seedBasicUser("dev@gmail.com");
        UUID user2 = seedBasicUser("dev2@gmail.com");

        seedBasicTransaction(user1, "key-1");
        assertThatCode(() -> seedBasicTransaction(user2, "key-1"))
                .doesNotThrowAnyException();
    }


    @Test
    public void rejectsTransactionDoubleReverse() {
        UUID user = seedBasicUser("dev@gmail.com");
        UUID transactionToReverse = seedBasicTransaction(user, "key-2");
        seedBasicTransaction(user, "key-1", "REVERSAL", transactionToReverse);
        assertThatThrownBy(() -> seedBasicTransaction(user, "key-3", "REVERSAL", transactionToReverse))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_transactions_reverses_transaction_id");
    }

    @Test
    public void rejectsNonReversalTransactionWithReverseId() {
        UUID user = seedBasicUser("dev@gmail.com");
        UUID transaction = seedBasicTransaction(user, "key-1");
        assertThatThrownBy(() -> seedBasicTransaction(user, "key-2", "DEPOSIT", transaction))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_transactions_reverses_transaction_id");
    }

    @Test
    public void rejectsRemovingUserWithTransactions() {
        UUID user = seedBasicUser("dev@gmail.com");
        seedBasicTransaction(user, "key-1");
        assertThatThrownBy(() -> jdbcTemplate.update("DELETE FROM users WHERE id=?", user))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_transactions_initiated_by");
    }

}
