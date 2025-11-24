-- Remove old name column after all instances use fullName
ALTER TABLE address_entry DROP COLUMN name;