package com.aiven.minobank.schema;

import com.aiven.minobank.BaseIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class UserRolesSchemaTests extends BaseIntegrationTests {

    private int seedRole(String name) {
        return jdbcTemplate.queryForObject("INSERT INTO roles (name) VALUES (?) RETURNING id", Integer.class, name);
    }
    private void assignRole(UUID userId, int roleId) {
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", userId, roleId);
    }

    @Test
    void rejectsRoleNameDuplicationIgnoringCase() {
        seedRole("test-admin");
        assertThatThrownBy(() -> seedRole("test-ADMIN"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_roles_name");
    }

    @Test
    void rejectsUserRolePairDuplication() {
        UUID userId = seedBasicUser("dev@gmail.com");
        int roleId = seedRole("test-user");

        assignRole(userId, roleId);

        assertThatThrownBy(() -> assignRole(userId, roleId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("pk_user_roles");
    }

    @Test
    void allowsManyToManyAssignments() {
        UUID userId = seedBasicUser("asd@gmail.com");
        UUID userId2 = seedBasicUser("asda@gmail.com");
        int roleId = seedRole("test-user");
        int roleId2 = seedRole("test-admin");


        assertThatCode(() -> {
            assignRole(userId, roleId);
            assignRole(userId, roleId2);
            assignRole(userId2, roleId);
        }).doesNotThrowAnyException();
        assertThat(
                jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM user_roles WHERE user_id IN (?, ?) AND role_id IN (?, ?)", Integer.class, userId, userId2, roleId, roleId2
                )
        ).isEqualTo(3);
    }

    @Test
    void deletingUserRemovesTheirRoleAssignments() {

        UUID userId = seedBasicUser("das@gmail.com");
        int roleId = seedRole("test-admin");
        assignRole(userId, roleId);

        assertThat(
                jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM user_roles WHERE user_id = ?", Integer.class, userId
                )
        ).isEqualTo(1);

        assertThat(
                jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId)
        ).isEqualTo(1);
        assertThat(
                jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM user_roles WHERE user_id = ?",
                        Integer.class,
                        userId
                )
        ).isEqualTo(0);
    }

    @Test
    void rejectsDeleteRoleThatAssigned() {

        UUID userId = seedBasicUser("asd@gmail.com");
        int roleId = seedRole("test-user");
        assignRole(userId, roleId);

        assertThatThrownBy(() -> jdbcTemplate.update("DELETE FROM roles WHERE id = ?", roleId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_user_roles_role_id");
        assertThat(
                jdbcTemplate.queryForObject("SELECT count(*) FROM user_roles WHERE user_id = ?", Integer.class, userId)
        ).isEqualTo(1);
        assertThat(
                jdbcTemplate.queryForObject("SELECT count(*) FROM roles WHERE id = ?", Integer.class, roleId)
        ).isEqualTo(1);
    }

    @Test
    void allowsDeleteRoleNobodyHolds() {
        int roleId = seedRole("test-user");
        assertThat(jdbcTemplate.update("DELETE FROM roles WHERE id = ?", roleId)).isEqualTo(1);
    }

    @Test
    void rejectsAssigningUndefinedUser() {
        int roleId = seedRole("test-user");
        assertThatThrownBy(() -> assignRole(UUID.randomUUID(), roleId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_user_roles_user_id");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM user_roles", Integer.class
        )).isEqualTo(0);
    }

    @Test
    void rejectsAssigningUndefinedRole() {
        UUID userId = seedBasicUser("email@test.com");
        assertThatThrownBy(() -> assignRole(userId, Integer.MAX_VALUE))
                    .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_user_roles_role_id");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM user_roles", Integer.class
        )).isEqualTo(0);
    }

}
