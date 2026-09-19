-- ==============================================================================
-- V3__export_jobs.sql
-- Enterprise Employee & Asset Management System (RBAC) - Export Jobs Schema
-- Asynchronous bulk export tracking & file download references
-- ==============================================================================

CREATE TABLE export_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_uuid VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    export_type VARCHAR(30) NOT NULL, -- EMPLOYEES, DEPARTMENTS, ASSETS, AUDIT_LOGS, ASSIGNMENT_HISTORY
    format VARCHAR(10) NOT NULL, -- CSV, EXCEL
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    progress_percentage INTEGER NOT NULL DEFAULT 0,
    total_records INTEGER DEFAULT 0,
    processed_records INTEGER DEFAULT 0,
    file_path VARCHAR(255),
    file_name VARCHAR(255),
    file_size_bytes BIGINT,
    content_type VARCHAR(100),
    error_message TEXT,
    filter_params JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    expires_at TIMESTAMP
);

CREATE INDEX idx_export_jobs_user ON export_jobs(user_id);
CREATE INDEX idx_export_jobs_status ON export_jobs(status);
CREATE INDEX idx_export_jobs_uuid ON export_jobs(job_uuid);
CREATE INDEX idx_export_jobs_created ON export_jobs(created_at DESC);
