CREATE TABLE roles (
    role_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name   VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE departments (
    department_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   VARCHAR(255),
    contact_email VARCHAR(255),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE users (
    user_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name       VARCHAR(80)  NOT NULL,
    last_name        VARCHAR(80)  NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    phone            VARCHAR(20),
    password_hash    VARCHAR(255),
    auth_provider    VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    provider_user_id VARCHAR(100),
    role_id          INT NOT NULL REFERENCES roles (role_id),
    department_id    INT REFERENCES departments (department_id),
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_users_auth_provider CHECK (auth_provider IN ('LOCAL', 'GOOGLE')),
    CONSTRAINT chk_users_email_lower CHECK (email = lower(email)),
    CONSTRAINT chk_users_credentials CHECK (
        (auth_provider = 'LOCAL'  AND password_hash IS NOT NULL) OR
        (auth_provider = 'GOOGLE' AND provider_user_id IS NOT NULL)
    ),
    CONSTRAINT uq_users_provider UNIQUE (auth_provider, provider_user_id)
);

CREATE INDEX idx_users_role       ON users (role_id);
CREATE INDEX idx_users_department ON users (department_id);

INSERT INTO roles (role_name, description) VALUES
    ('STUDENT',           'Reports campus issues and tracks them'),
    ('STAFF',             'General staff who report campus issues'),
    ('MAINTENANCE_STAFF', 'Handles assigned issues'),
    ('ADMIN',             'Manages users, assignments and reference data'),
    ('MANAGEMENT',        'Monitors issues, statistics and reports');

INSERT INTO departments (name, description) VALUES
    ('ICT Department',         'Network, computers and projectors'),
    ('Facilities/Maintenance', 'Buildings, furniture and general repairs'),
    ('Electrical',             'Lighting and power'),
    ('Plumbing',               'Taps, sinks and drainage'),
    ('Administration',         'Campus administration');