-- Update audit_logs table to match entity definitions
-- This migration updates the audit_logs table to use more compatible data types

-- Update the ip_address column to VARCHAR for better compatibility
ALTER TABLE audit_logs ALTER COLUMN ip_address TYPE VARCHAR(45);

-- Update old_values and new_values to TEXT for better compatibility
-- Note: In PostgreSQL, this will still store JSON but as TEXT type
ALTER TABLE audit_logs ALTER COLUMN old_values TYPE TEXT;
ALTER TABLE audit_logs ALTER COLUMN new_values TYPE TEXT;

-- Add comments to clarify the changes
COMMENT ON COLUMN audit_logs.ip_address IS 'IP address as string, supports both IPv4 and IPv6';
COMMENT ON COLUMN audit_logs.old_values IS 'JSON string containing old values';
COMMENT ON COLUMN audit_logs.new_values IS 'JSON string containing new values';
