SHOW DATABASES;
USE SCFMS;
SHOW TABLES;

SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_guest_users FROM guest_users;
SELECT COUNT(*) AS total_buildings FROM buildings;
SELECT COUNT(*) AS total_rooms FROM rooms;
SELECT COUNT(*) AS total_logs FROM logs;

SELECT * FROM users LIMIT 10;
SELECT * FROM guest_users LIMIT 10;
SELECT Classification, Permissions, COUNT(*) AS total
FROM users
GROUP BY Classification, Permissions
ORDER BY Permissions;
SELECT * FROM buildings;
SELECT * FROM rooms LIMIT 10;
SELECT IsLocked, COUNT(*) AS total
FROM rooms
GROUP BY IsLocked;
SELECT buildingID, PrivilegeRequired, COUNT(*) AS total
FROM rooms
GROUP BY buildingID, PrivilegeRequired
ORDER BY buildingID, PrivilegeRequired;
SELECT * FROM logs;
