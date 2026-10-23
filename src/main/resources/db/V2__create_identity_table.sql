CREATE TABLE users (
    id uuid DEFAULT gen_random_uuid(),
    email CITEXT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    email_verified_at TIMESTAMPTZ,
    failed_login_attempts INT  DEFAULT 0 NOT NULL,
    locked_until TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT ck_users_status CHECK ( status IN ('PENDING', 'ACTIVE', 'LOCKED', 'CLOSED') ),
    CONSTRAINT ck_users_failed_login_attempt_not_negative CHECK ( failed_login_attempts >= 0 ),
    CONSTRAINT uq_users_email UNIQUE (email)
);

