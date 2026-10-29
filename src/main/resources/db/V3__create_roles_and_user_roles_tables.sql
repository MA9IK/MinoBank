CREATE TABLE roles (
    id int GENERATED ALWAYS AS IDENTITY,
    name CITEXT not null,
    description varchar(255),

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
);

CREATE TABLE user_roles (
    user_id uuid not null,
    role_id int not null,
    assigned_at timestamptz default current_timestamp not null,

    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id)
                        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id)
                        REFERENCES roles (id) ON DELETE RESTRICT,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id)
);

CREATE INDEX ix_user_roles_role_id ON user_roles (role_id);