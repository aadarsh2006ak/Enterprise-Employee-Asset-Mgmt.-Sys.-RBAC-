-- ==============================================================================
-- V2__seed_initial_data.sql
-- Enterprise Employee & Asset Management System (RBAC) - Seed Initial Data
-- Roles, Granular Permissions, Role-Permission Mappings, Categories & Admin User
-- ==============================================================================

-- 1. Insert Core Roles
INSERT INTO roles (name, description) VALUES
    ('ADMIN', 'System owner / HR-IT Head with full system access'),
    ('MANAGER', 'Team lead / Department Head managing department assets & employees'),
    ('EMPLOYEE', 'Individual contributor with self-service asset access')
ON CONFLICT (name) DO NOTHING;

-- 2. Insert Permissions
INSERT INTO permissions (code, description) VALUES
    ('USER_CREATE', 'Create new system users'),
    ('USER_READ', 'View system user accounts'),
    ('USER_UPDATE', 'Update user roles and credentials'),
    ('USER_DELETE', 'Deactivate or delete system users'),
    ('EMPLOYEE_CREATE', 'Create employee profiles'),
    ('EMPLOYEE_READ', 'View employee profiles and details'),
    ('EMPLOYEE_UPDATE', 'Update employee profiles'),
    ('EMPLOYEE_STATUS_CHANGE', 'Change employee employment status'),
    ('ASSET_CREATE', 'Register new assets in inventory'),
    ('ASSET_READ', 'View asset inventory and details'),
    ('ASSET_UPDATE', 'Update asset information and maintenance status'),
    ('ASSET_DELETE', 'Retire or remove assets'),
    ('ASSET_ASSIGN', 'Assign assets to employees'),
    ('ASSET_RETURN', 'Accept returned assets from employees'),
    ('ASSET_REQUEST_CREATE', 'Raise a new asset request'),
    ('ASSET_REQUEST_REVIEW', 'Approve or reject asset requests'),
    ('DEPARTMENT_MANAGE', 'Create and modify departments'),
    ('AUDIT_READ', 'View system audit logs'),
    ('EXPORT_DATA', 'Export CSV/Excel reports')
ON CONFLICT (code) DO NOTHING;

-- 3. Map Permissions to ADMIN Role (All Permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- 4. Map Permissions to MANAGER Role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MANAGER'
  AND p.code IN (
      'EMPLOYEE_READ', 'EMPLOYEE_UPDATE',
      'ASSET_READ', 'ASSET_ASSIGN', 'ASSET_RETURN',
      'ASSET_REQUEST_CREATE', 'ASSET_REQUEST_REVIEW',
      'AUDIT_READ', 'EXPORT_DATA'
  )
ON CONFLICT DO NOTHING;

-- 5. Map Permissions to EMPLOYEE Role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'EMPLOYEE'
  AND p.code IN (
      'ASSET_READ',
      'ASSET_REQUEST_CREATE'
  )
ON CONFLICT DO NOTHING;

-- 6. Insert Asset Categories
INSERT INTO asset_categories (name) VALUES
    ('Laptop'),
    ('Monitor'),
    ('Mobile Device'),
    ('ID Card / Access Badge'),
    ('Headset / Audio'),
    ('Tablet'),
    ('Networking Equipment')
ON CONFLICT (name) DO NOTHING;

-- 7. Insert Default IT Department
INSERT INTO departments (name) VALUES
    ('Information Technology'),
    ('Human Resources'),
    ('Engineering'),
    ('Finance & Operations')
ON CONFLICT (name) DO NOTHING;

-- 8. Insert Default Users (Admin, Manager, Employee)
-- Passwords:
-- Admin:    Admin@123    ($2a$12$5sErpWpQdzRDfDFIWsvXZOzPNI8c23PFxAPwSvyEuouhdykkjAN3i)
-- Manager:  Manager@123  ($2a$12$z.EO2v8wkmW.rRaZ8WwBgOQ/EwM9uQoQL2yHDbi1QvUtf6u4iDfx2)
-- Employee: Employee@123 ($2a$12$maP/.LP2PfTC.ukYFe5douQCYXZXNuUBPTgmgIxe552U4RfvkY7O2)

-- Super Admin User
INSERT INTO users (username, email, password_hash, role_id, is_active, version)
SELECT 
    'admin', 
    'admin@company.com', 
    '$2a$12$5sErpWpQdzRDfDFIWsvXZOzPNI8c23PFxAPwSvyEuouhdykkjAN3i', 
    r.id, 
    TRUE, 
    0
FROM roles r
WHERE r.name = 'ADMIN'
ON CONFLICT (username) DO NOTHING;

-- Manager User
INSERT INTO users (username, email, password_hash, role_id, is_active, version)
SELECT 
    'manager', 
    'manager@company.com', 
    '$2a$12$z.EO2v8wkmW.rRaZ8WwBgOQ/EwM9uQoQL2yHDbi1QvUtf6u4iDfx2', 
    r.id, 
    TRUE, 
    0
FROM roles r
WHERE r.name = 'MANAGER'
ON CONFLICT (username) DO NOTHING;

-- Employee User
INSERT INTO users (username, email, password_hash, role_id, is_active, version)
SELECT 
    'employee', 
    'employee@company.com', 
    '$2a$12$maP/.LP2PfTC.ukYFe5douQCYXZXNuUBPTgmgIxe552U4RfvkY7O2', 
    r.id, 
    TRUE, 
    0
FROM roles r
WHERE r.name = 'EMPLOYEE'
ON CONFLICT (username) DO NOTHING;

-- 9. Insert Employee Profiles
INSERT INTO employees (user_id, employee_code, full_name, department_id, designation, date_of_joining, status, version)
SELECT 
    u.id, 
    'EMP-00001', 
    'System Administrator', 
    d.id, 
    'IT Head & System Administrator', 
    CURRENT_DATE, 
    'ACTIVE', 
    0
FROM users u
JOIN departments d ON d.name = 'Information Technology'
WHERE u.username = 'admin'
ON CONFLICT (employee_code) DO NOTHING;

INSERT INTO employees (user_id, employee_code, full_name, department_id, designation, date_of_joining, status, version)
SELECT 
    u.id, 
    'EMP-00002', 
    'Engineering Manager', 
    d.id, 
    'Engineering Team Lead', 
    CURRENT_DATE, 
    'ACTIVE', 
    0
FROM users u
JOIN departments d ON d.name = 'Engineering'
WHERE u.username = 'manager'
ON CONFLICT (employee_code) DO NOTHING;

INSERT INTO employees (user_id, employee_code, full_name, department_id, designation, date_of_joining, status, version)
SELECT 
    u.id, 
    'EMP-00003', 
    'John Doe', 
    d.id, 
    'Software Engineer', 
    CURRENT_DATE, 
    'ACTIVE', 
    0
FROM users u
JOIN departments d ON d.name = 'Engineering'
WHERE u.username = 'employee'
ON CONFLICT (employee_code) DO NOTHING;

-- Set manager of IT department to Admin employee
UPDATE departments
SET manager_id = (SELECT id FROM employees WHERE employee_code = 'EMP-00001')
WHERE name = 'Information Technology';

-- Set manager of Engineering department to Manager employee
UPDATE departments
SET manager_id = (SELECT id FROM employees WHERE employee_code = 'EMP-00002')
WHERE name = 'Engineering';
