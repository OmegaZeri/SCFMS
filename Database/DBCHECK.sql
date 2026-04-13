SHOW DATABASES;
USE scfms_db;
SHOW TABLES;

SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_buildings FROM buildings;
SELECT COUNT(*) AS total_rooms FROM rooms;
SELECT COUNT(*) AS total_logs FROM logs;

SELECT * FROM users LIMIT 10;
SELECT * FROM buildings;
SELECT * FROM rooms LIMIT 10;
SELECT * FROM logs;
