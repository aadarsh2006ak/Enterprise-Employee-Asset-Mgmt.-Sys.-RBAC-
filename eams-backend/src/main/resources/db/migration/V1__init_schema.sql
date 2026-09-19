-- ==============================================================================
-- V1__init_schema.sql
-- Enterprise Employee & Asset Management System (RBAC) - Core Database Schema
-- 3NF Compliant Schema with Concurrency Control & Unique Partial Indexing
-- ==============================================================================

-- 1. Roles & Permissions (RBAC Core)
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE, -- ADMIN, MANAGER, EMPLOYEE
    description VARCHAR(255)
);

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(60) NOT NULL UNIQUE, -- e.g. ASSET_CREATE, EMPLOYEE_DELETE
    description VARCHAR(255)
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 2. Users & Refresh Tokens (Auth Core)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_user_id ON refresh_tokens(user_id);

-- 3. Departments & Employees
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    manager_id BIGINT -- foreign key added after employees table
);

CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    employee_code VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    department_id BIGINT REFERENCES departments(id),
    designation VARCHAR(80),
    date_of_joining DATE,
    reporting_to BIGINT REFERENCES employees(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

-- Foreign key linking department to manager (Employee)
ALTER TABLE departments
    ADD CONSTRAINT fk_departments_manager
    FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE SET NULL;

CREATE INDEX idx_employees_department ON employees(department_id);
CREATE INDEX idx_employees_reporting ON employees(reporting_to);
CREATE INDEX idx_employees_status ON employees(status);

-- 4. Asset Categories, Assets, Assignments & Requests
CREATE TABLE asset_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    asset_tag VARCHAR(30) NOT NULL UNIQUE,
    category_id BIGINT NOT NULL REFERENCES asset_categories(id),
    model_name VARCHAR(120),
    serial_number VARCHAR(120) UNIQUE,
    purchase_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_assets_status ON assets(status);
CREATE INDEX idx_assets_category ON assets(category_id);

CREATE TABLE asset_assignments (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES assets(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    assigned_by BIGINT NOT NULL REFERENCES users(id),
    assigned_at TIMESTAMP NOT NULL DEFAULT now(),
    returned_at TIMESTAMP,
    condition_notes TEXT,
    CONSTRAINT chk_return_after_assign CHECK (returned_at IS NULL OR returned_at >= assigned_at)
);

-- Concurrency Safety Net: Only ONE active assignment per asset at any time
CREATE UNIQUE INDEX uq_active_assignment_per_asset
    ON asset_assignments(asset_id) WHERE returned_at IS NULL;

CREATE INDEX idx_assignments_employee ON asset_assignments(employee_id);

CREATE TABLE asset_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    category_id BIGINT NOT NULL REFERENCES asset_categories(id),
    reason VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT REFERENCES users(id),
    requested_at TIMESTAMP NOT NULL DEFAULT now(),
    reviewed_at TIMESTAMP
);

CREATE INDEX idx_requests_status ON asset_requests(status);

-- 5. Audit Logs (Immutable, JSONB Change Snapshots)
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    username_snapshot VARCHAR(60),
    entity_name VARCHAR(60) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(20) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);
