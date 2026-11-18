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


CREATE TABLE accounts (
    id UUID DEFAULT gen_random_uuid(),
    iban VARCHAR(34) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    type VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL DEFAULT 'ACTIVE',
    balance_minor BIGINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    status_changed_by UUID,
    status_reason VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT ck_accounts_type check ( type IN ('CUSTOMER', 'INTERNAL') ),
    CONSTRAINT ck_accounts_balance_minor CHECK ( (type = 'CUSTOMER' AND balance_minor >= 0) OR (type = 'INTERNAL') ),
    CONSTRAINT ck_accounts_status check ( status IN ('FROZEN', 'CLOSED', 'ACTIVE') ),
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT fk_accounts_status_changed_by FOREIGN KEY (status_changed_by)
                      REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT uq_accounts_iban UNIQUE (iban),
    CONSTRAINT uq_accounts_id_currency UNIQUE (id, currency),
    CONSTRAINT ck_accounts_currency CHECK (currency ~ '^[A-Z]{3}$')

);

CREATE TABLE categories (
                            id int GENERATED ALWAYS AS IDENTITY,
                            name VARCHAR(255) NOT NULL,
                            kind VARCHAR(120) NOT NULL,

                            CONSTRAINT pk_categories PRIMARY KEY (id),
                            CONSTRAINT ck_categories_kind CHECK ( kind IN ('INCOME', 'EXPENSE', 'TRANSFER') ),
                            CONSTRAINT uq_categories_name UNIQUE (name)
);

CREATE TABLE ledger_entries (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    transaction_id UUID NOT NULL,
    account_id UUID NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance_after_minor BIGINT NOT NULL,
    category_id int,
    note VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ledger_entries PRIMARY KEY (id),
    CONSTRAINT fk_ledger_entries_category_id FOREIGN KEY (category_id)
        REFERENCES categories (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ledger_entries_account_currency FOREIGN KEY (account_id, currency)
        REFERENCES accounts (id, currency) ON DELETE RESTRICT,
    CONSTRAINT fk_ledger_entries_transaction_id FOREIGN KEY (transaction_id)
        REFERENCES transactions (id) ON DELETE RESTRICT,
    CONSTRAINT ck_ledger_entries_amount_minor CHECK ( amount_minor <> 0 ),
    CONSTRAINT ck_ledger_entries_currency CHECK (currency ~ '^[A-Z]{3}$')
);

CREATE INDEX ix_ledger_entries_transaction_id
    ON ledger_entries (transaction_id);

CREATE INDEX ix_ledger_entries_account_created_at
    ON ledger_entries (account_id, created_at DESC);