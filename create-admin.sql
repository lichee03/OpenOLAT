-- Create administrator user
-- Username: administrator  
-- Password: openolat

BEGIN;

-- Insert identity (id=1, name='administrator', status=2 means active)
INSERT INTO o_bs_identity (id, version, creationdate, name, status)
VALUES (1, 0, NOW(), 'administrator', 2);

-- Insert user profile
INSERT INTO o_user (user_id, version, creationdate, u_firstname, u_lastname, u_email, informsessiontimeout)
VALUES (1, 0, NOW(), 'Administrator', 'OpenOLAT', 'admin@localhost', false);

-- Insert authentication (password: openolat, SHA-256 hash)
INSERT INTO o_bs_authentication (id, version, creationdate, lastmodified, identity_fk, provider, authusername, credential)
VALUES (1, 0, NOW(), NOW(), 1, 'OLAT', 'administrator', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8');

COMMIT;

SELECT 'Administrator user created successfully!' as status;
