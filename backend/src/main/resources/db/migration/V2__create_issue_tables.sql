CREATE TABLE categories (
    category_id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                  VARCHAR(60) NOT NULL UNIQUE,
    description           VARCHAR(255),
    default_department_id INT REFERENCES departments (department_id),
    is_active             BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE locations (
    location_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    building    VARCHAR(80) NOT NULL,
    floor       VARCHAR(10),
    room        VARCHAR(30),
    description VARCHAR(255),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX uq_locations_place
    ON locations (building, COALESCE(floor, ''), COALESCE(room, ''));

CREATE TABLE issues (
    issue_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    issue_code        VARCHAR(20) GENERATED ALWAYS AS ('ISS-' || lpad(issue_id::text, 5, '0')) STORED,
    title             VARCHAR(150) NOT NULL,
    description       TEXT NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    priority          VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    reporter_id       BIGINT NOT NULL REFERENCES users (user_id),
    category_id       INT NOT NULL REFERENCES categories (category_id),
    location_id       INT NOT NULL REFERENCES locations (location_id),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    first_response_at TIMESTAMPTZ,
    resolved_at       TIMESTAMPTZ,
    closed_at         TIMESTAMPTZ,
    CONSTRAINT chk_issues_status CHECK (status IN
        ('SUBMITTED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REOPENED')),
    CONSTRAINT chk_issues_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT chk_issues_resolved_after_created CHECK (resolved_at IS NULL OR resolved_at >= created_at)
);

CREATE INDEX idx_issues_reporter        ON issues (reporter_id, created_at DESC);
CREATE INDEX idx_issues_status          ON issues (status, created_at DESC);
CREATE INDEX idx_issues_category_loc    ON issues (category_id, location_id);
CREATE INDEX idx_issues_created_at      ON issues (created_at);

CREATE TABLE assignments (
    assignment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    issue_id      BIGINT NOT NULL REFERENCES issues (issue_id),
    assigned_to   BIGINT REFERENCES users (user_id),
    department_id INT REFERENCES departments (department_id),
    assigned_by   BIGINT NOT NULL REFERENCES users (user_id),
    assigned_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    unassigned_at TIMESTAMPTZ,
    is_current    BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_assignments_target CHECK (assigned_to IS NOT NULL OR department_id IS NOT NULL),
    CONSTRAINT chk_assignments_current CHECK (is_current = (unassigned_at IS NULL))
);

CREATE UNIQUE INDEX uq_assignments_one_current ON assignments (issue_id) WHERE is_current;
CREATE INDEX idx_assignments_user_current  ON assignments (assigned_to)   WHERE is_current;
CREATE INDEX idx_assignments_dept_current  ON assignments (department_id) WHERE is_current;

CREATE TABLE issue_updates (
    update_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    issue_id   BIGINT NOT NULL REFERENCES issues (issue_id),
    user_id    BIGINT NOT NULL REFERENCES users (user_id),
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    comment    TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_updates_old_status CHECK (old_status IS NULL OR old_status IN
        ('SUBMITTED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REOPENED')),
    CONSTRAINT chk_updates_new_status CHECK (new_status IS NULL OR new_status IN
        ('SUBMITTED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REOPENED')),
    CONSTRAINT chk_updates_not_empty CHECK (new_status IS NOT NULL OR comment IS NOT NULL)
);

CREATE INDEX idx_updates_issue_time ON issue_updates (issue_id, created_at);

CREATE TABLE attachments (
    attachment_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    issue_id        BIGINT NOT NULL REFERENCES issues (issue_id),
    file_name       VARCHAR(255) NOT NULL,
    file_type       VARCHAR(100) NOT NULL,
    file_size_bytes INT NOT NULL CHECK (file_size_bytes > 0),
    file_url        VARCHAR(500) NOT NULL,
    uploaded_by     BIGINT NOT NULL REFERENCES users (user_id),
    uploaded_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_attachments_issue ON attachments (issue_id);

INSERT INTO categories (name, description, default_department_id) VALUES
    ('Electrical',  'Broken lights, power problems',
        (SELECT department_id FROM departments WHERE name = 'Electrical')),
    ('Plumbing',    'Leaking taps, blocked sinks',
        (SELECT department_id FROM departments WHERE name = 'Plumbing')),
    ('ICT/Network', 'Wi-Fi outages, faulty projectors, computer problems',
        (SELECT department_id FROM departments WHERE name = 'ICT Department')),
    ('Furniture',   'Broken chairs and desks',
        (SELECT department_id FROM departments WHERE name = 'Facilities/Maintenance')),
    ('Cleaning',    'Sanitation and cleaning issues',
        (SELECT department_id FROM departments WHERE name = 'Facilities/Maintenance')),
    ('Buildings',   'Doors, windows, walls, ceilings',
        (SELECT department_id FROM departments WHERE name = 'Facilities/Maintenance')),
    ('Other',       'Anything that does not fit another category',
        (SELECT department_id FROM departments WHERE name = 'Administration'));

INSERT INTO locations (building, floor, room, description) VALUES
    ('Library',   '1', NULL,   'Main library, ground floor'),
    ('Block A',   '1', 'A101', 'Classroom'),
    ('Block B',   '2', 'B204', 'Classroom'),
    ('Cafeteria', NULL, NULL,  'Student cafeteria');