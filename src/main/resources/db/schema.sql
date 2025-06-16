-- Create organizations table
CREATE TABLE IF NOT EXISTS organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    url VARCHAR(255) NOT NULL UNIQUE,
    logo_url VARCHAR(255)
);

-- Create jobs table
CREATE TABLE IF NOT EXISTS jobs (
    id BIGSERIAL PRIMARY KEY,
    position_name VARCHAR(255) NOT NULL,
    job_page_url VARCHAR(255) NOT NULL UNIQUE,
    organization_id BIGINT NOT NULL,
    labor_function VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    posted_date BIGINT NOT NULL,
    description TEXT,
    job_type VARCHAR(50),
    experience_level VARCHAR(50),
    salary VARCHAR(100),
    remote BOOLEAN,
    application_url VARCHAR(255),
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- Create job_tags table
CREATE TABLE IF NOT EXISTS job_tags (
    job_id BIGINT NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (job_id, tag),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_jobs_location ON jobs(location);
CREATE INDEX IF NOT EXISTS idx_jobs_posted_date ON jobs(posted_date);
CREATE INDEX IF NOT EXISTS idx_jobs_labor_function ON jobs(labor_function);
CREATE INDEX IF NOT EXISTS idx_jobs_organization ON jobs(organization_id);
CREATE INDEX IF NOT EXISTS idx_jobs_job_type ON jobs(job_type);
CREATE INDEX IF NOT EXISTS idx_jobs_experience_level ON jobs(experience_level);
CREATE INDEX IF NOT EXISTS idx_jobs_remote ON jobs(remote); 