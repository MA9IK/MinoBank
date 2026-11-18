package com.aiven.minobank.schema;

import com.aiven.minobank.BaseIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LedgerEntriesSchemaTest extends BaseIntegrationTests {
    private Integer countEntriesOfAccount(UUID accountId) {
        return jdbcTemplate.queryForObject(
                "SELECT count(*) FROM ledger_entries WHERE account_id = ?", Integer.class, accountId);
    }

    @Test
    void rejectsZeroAmountEntry() {
        UUID userId = seedBasicUser("ledger-zero@test.com");
        UUID accountId = seedAccount("iban-ledger-1", "USD", "CUSTOMER");
        UUID transactionId = seedBasicTransaction(userId);

        assertThatThrownBy(() -> seedEntry(transactionId, accountId, "USD", 0))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_ledger_entries_amount_minor");
        assertThat(countEntriesOfAccount(accountId)).isEqualTo(0);
    }

    @Test
    void acceptsNonZeroEntry() {
        UUID userId = seedBasicUser("ledger-ok@test.com");
        UUID accountId = seedAccount("iban-ledger-1", "USD", "CUSTOMER");
        UUID transactionId = seedBasicTransaction(userId);

        Long entryId = seedEntry(transactionId, accountId, "USD", 500);

        assertThat(entryId).isNotNull();
        assertThat(countEntriesOfAccount(accountId)).isEqualTo(1);
    }

    @Test
    void rejectsDeletingAccountThatHasEntries() {
        UUID userId = seedBasicUser("ledger-delete@test.com");
        UUID accountId = seedAccount("iban-ledger-1", "USD", "CUSTOMER");
        UUID transactionId = seedBasicTransaction(userId);
        seedEntry(transactionId, accountId, "USD", 500);

        assertThatThrownBy(() -> jdbcTemplate.update("DELETE FROM accounts WHERE id = ?", accountId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_ledger_entries_account_currency");

        assertThat(countEntriesOfAccount(accountId)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM accounts WHERE id = ?", Integer.class, accountId)).isEqualTo(1);
    }

    @Test
    void rejectsEntryWithCurrencyDifferentFromAccount() {
        UUID userId = seedBasicUser("ledger-currency@test.com");
        UUID accountId = seedAccount("iban-ledger-1", "USD", "CUSTOMER");
        UUID transactionId = seedBasicTransaction(userId);

        assertThatThrownBy(() -> seedEntry(transactionId, accountId, "EUR", 500))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_ledger_entries_account_currency");
        assertThat(countEntriesOfAccount(accountId)).isEqualTo(0);
    }
}