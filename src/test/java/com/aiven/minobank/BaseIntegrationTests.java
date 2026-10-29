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
        jdbcTemplate.execute("TRUNCATE TABLE users, user_roles RESTRICT");
        jdbcTemplate.execute("DELETE FROM roles WHERE name LIKE 'test-%'");
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
