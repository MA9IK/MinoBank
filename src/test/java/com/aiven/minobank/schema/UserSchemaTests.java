package com.aiven.minobank.schema;

import com.aiven.minobank.BaseIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserSchemaTests extends BaseIntegrationTests {

    @Test
    void rejectsNegativeLoginAttempt() {
        assertThatThrownBy(() -> seedBasicUser("ad@gmail.com", "123", -1))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_users_failed_login_attempt_not_negative");
    }

    @Test
    void rejectsEmailDuplicationIgnoringCase() {
        seedBasicUser("asd@gmail.com");
        assertThatThrownBy(() -> seedBasicUser("ASd@gmail.com"))
                .isInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining("uq_users_email");
    }

    @Test
    void rejectsUnknownStatus() {
        assertThatThrownBy(() -> seedBasicUser("asd@gmail.com", "123", 0, "AAF"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_users_status");
    }

    @Test
    void newUserDefaultsToPendingWithNoFailedAttempts() {
        seedBasicUser("asd@gmail.com");
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM users WHERE email = ?", "asd@gmail.com");

        assertThat(row.get("failed_login_attempts")).isEqualTo(0);
        assertThat(row.get("status")).isEqualTo("PENDING");
    }
}
