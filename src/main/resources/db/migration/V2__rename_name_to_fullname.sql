-- Add new full_name column alongside existing name column
ALTER TABLE address_entry ADD COLUMN full_name VARCHAR(255);
-- Copy existing data
UPDATE address_entry SET full_name = name WHERE full_name IS NULL;