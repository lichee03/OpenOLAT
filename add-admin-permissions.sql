-- Add administrator permissions and groups

BEGIN;

-- Get or create base group
INSERT INTO o_bs_group (id, creationdate) 
VALUES (1, NOW())
ON CONFLICT (id) DO NOTHING;

-- Create system administrator security group membership
INSERT INTO o_bs_group_member (creationdate, g_role, fk_group_id, fk_identity_id)
VALUES (NOW(), 'admin', 1, 1)
ON CONFLICT DO NOTHING;

-- Create policy granting system admin rights
INSERT INTO o_bs_policy (id, version, creationdate, oresource_id, group_id, permission)
VALUES (1, 0, NOW(), NULL, 1, 'olat.admin')
ON CONFLICT (id) DO NOTHING;

COMMIT;

SELECT 'Administrator permissions granted!' as status;
