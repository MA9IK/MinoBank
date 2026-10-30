CREATE TABLE transactions (
    id UUID DEFAULT gen_random_uuid(),
    initiated_by UUID NOT NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    reference VARCHAR(140),
    reverses_transaction_id UUID,
    failure_reason VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    booked_at TIMESTAMPTZ,

    CONSTRAINT ck_transactions_reverses_transaction_id CHECK (
        (type = 'REVERSAL' AND reverses_transaction_id IS NOT NULL)
        OR (type <> 'REVERSAL' AND reverses_transaction_id IS NULL)
    ),
    CONSTRAINT ck_transactions_booked_at_matches_status CHECK (
        (status = 'PENDING' AND booked_at IS NULL)
            OR (status = 'BOOKED' AND booked_at IS NOT NULL)
            OR (status = 'FAILED' AND booked_at IS NULL)
        ),
    CONSTRAINT ck_transactions_failure_reason_matches_status CHECK (
        (status = 'PENDING' AND failure_reason IS NULL)
            OR (status = 'BOOKED' AND failure_reason IS NULL)
            OR (status = 'FAILED' AND failure_reason IS NOT NULL)
        ),
    CONSTRAINT ck_transactions_no_self_reversal CHECK ( reverses_transaction_id <> id ),
    CONSTRAINT uq_transactions_user_idempotency_key UNIQUE (initiated_by, idempotency_key),
    CONSTRAINT uq_transactions_reverses_transaction_id UNIQUE (reverses_transaction_id),
    CONSTRAINT fk_transactions_reverses_transaction_id FOREIGN KEY (reverses_transaction_id)
        REFERENCES transactions (id) ON DELETE RESTRICT,
    CONSTRAINT ck_transactions_status CHECK ( status IN ('PENDING', 'BOOKED', 'FAILED') ),
    CONSTRAINT ck_transactions_type CHECK ( type IN ('WITHDRAWAL', 'DEPOSIT', 'REVERSAL', 'TRANSFER') ),
    CONSTRAINT fk_transactions_initiated_by FOREIGN KEY (initiated_by)
        REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT pk_transactions PRIMARY KEY (id)
);
CREATE INDEX ix_transactions_created_at ON transactions (created_at);
CREATE INDEX ix_transactions_status ON transactions (status);
