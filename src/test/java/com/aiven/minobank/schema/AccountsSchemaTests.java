package com.aiven.minobank.schema;

import com.aiven.minobank.BaseIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class AccountsSchemaTests extends BaseIntegrationTests {
    private UUID seedAccount(String iban) {
        return jdbcTemplate.queryForObject("INSERT INTO accounts (iban, currency, type) VALUES (?, 'USD', 'CUSTOMER') RETURNING id", UUID.class, iban);
    }

    private UUID seedAccount(String iban, String type) {
        return jdbcTemplate.queryForObject("INSERT INTO accounts (iban, currency, type) VALUES (?, 'USD', ?) RETURNING id", UUID.class, iban, type);
    }

    @Test
    public void rejectsDuplicateIBAN() {
        seedAccount("iban-1");
        assertThatThrownBy(() -> seedAccount("iban-1"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_accounts_iban");
    }

    @Test
    public void allowsNegativeBalanceInternal() {
        UUID account = seedAccount("iban-1", "INTERNAL");
        assertThatCode(() -> jdbcTemplate.update("UPDATE accounts SET balance_minor = -1 WHERE id=?", account))
                .doesNotThrowAnyException();
    }

    @Test
    public void rejectsNegativeBalanceCustomer() {
        UUID account = seedAccount("iban-1", "CUSTOMER");
        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE accounts SET balance_minor = -1 WHERE id=?", account))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_accounts_balance_minor");
    }

    @Test
    public void checksDefaultAccountValues() {
        UUID account = seedAccount("iban-1");
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM accounts WHERE id=?", account);

        assertThat(row.get("balance_minor")).isEqualTo(0L);
        assertThat(row.get("version")).isEqualTo(0L);
        assertThat(row.get("status")).isEqualTo("ACTIVE");
    }
}
