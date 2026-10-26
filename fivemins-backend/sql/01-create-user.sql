-- À exécuter UNE fois, en administrateur, dans la base pluggable :
--   sqlplus system@//localhost:1521/XEPDB1      (XE 18c/21c)
--   sqlplus system@localhost:1521/XE            (XE 11g)
CREATE USER fivemins IDENTIFIED BY fivemins;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE VIEW TO fivemins;
ALTER USER fivemins QUOTA UNLIMITED ON USERS;
