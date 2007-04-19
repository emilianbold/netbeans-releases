-- Vehicle Incident Report:  create user and schema
-- CREATE USER vir PASSWORD vir
CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(
    'derby.user.vir', 'vir');
-- new DROP SCHEMA will fail on a new install - That's OK for a new install.
DROP SCHEMA vir RESTRICT;
CREATE SCHEMA vir;

