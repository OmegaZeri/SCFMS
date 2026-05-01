import json
import os
import random
import re
import sys
from datetime import date

from faker import Faker
import mysql.connector


#CONFIG
DEFAULT_DATABASE = "SCFMS"
NUM_RECORDS = 3000  # change this to generate more rows/Generate larger data set for better testing of performance and indexing
NUM_GUEST_USERS = 250
NUM_BUILDINGS = 8
MIN_ROOMS_PER_BUILDING = 8
MAX_ROOMS_PER_BUILDING = 16
MAX_LOGS_PER_USER = 6
CLASSIFICATION_PERMISSIONS = {
    "undergrad": 1,
    "grad": 2,
    "faculty": 3,
    "security officer": 4,
}

USERS_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS users (
    userID INT NOT NULL PRIMARY KEY,
    userName VARCHAR(100) NOT NULL,
    Email VARCHAR(150) NOT NULL UNIQUE,
    Password VARCHAR(100) NOT NULL,
    PhoneNumber VARCHAR(25) NOT NULL,
    Classification VARCHAR(50) NOT NULL,
    Permissions INT NOT NULL,
    Age INT,
    Created DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
"""

BUILDINGS_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS buildings (
    buildingID INT NOT NULL PRIMARY KEY,
    buildingName VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
"""

GUEST_USERS_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS guest_users (
    userID INT NOT NULL PRIMARY KEY,
    userName VARCHAR(100) NOT NULL,
    Email VARCHAR(150) NOT NULL UNIQUE,
    Password VARCHAR(100) NOT NULL,
    PhoneNumber VARCHAR(25) NOT NULL,
    Age INT,
    Created DATETIME NOT NULL,
    RevokeAccess DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
"""

ROOMS_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS rooms (
    roomID INT NOT NULL,
    buildingID INT NOT NULL,
    IsLocked BOOLEAN NOT NULL,
    PRIMARY KEY (roomID, buildingID),
    UNIQUE KEY uq_rooms_roomID (roomID),
    CONSTRAINT fk_rooms_building
        FOREIGN KEY (buildingID) REFERENCES buildings(buildingID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
"""

LOGS_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS logs (
    userID INT NOT NULL,
    roomID INT NOT NULL,
    logDate DATE NOT NULL,
    PRIMARY KEY (userID, roomID, logDate),
    CONSTRAINT fk_logs_user
        FOREIGN KEY (userID) REFERENCES users(userID),
    CONSTRAINT fk_logs_room
        FOREIGN KEY (roomID) REFERENCES rooms(roomID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
"""

INSERT_USERS_SQL = """
INSERT INTO users (userID, userName, Email, Password, PhoneNumber, Classification, Permissions, Age, Created)
VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
"""

INSERT_BUILDINGS_SQL = """
INSERT INTO buildings (buildingID, buildingName)
VALUES (%s, %s)
"""

INSERT_GUEST_USERS_SQL = """
INSERT INTO guest_users (userID, userName, Email, Password, PhoneNumber, Age, Created, RevokeAccess)
VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
"""

INSERT_ROOMS_SQL = """
INSERT INTO rooms (roomID, buildingID, IsLocked)
VALUES (%s, %s, %s)
"""

INSERT_LOGS_SQL = """
INSERT INTO logs (userID, roomID, logDate)
VALUES (%s, %s, %s)
"""

fake = Faker()
fake.seed_instance(1529344966071113536)  # Lottery Seed for Data
random.seed(1529344966071113536)


def get_base_dir() -> str:
    # When bundled as an exe, look for db_config.json next to the executable
    if getattr(sys, "frozen", False):
        return os.path.dirname(sys.executable)
    return os.path.dirname(__file__)


def load_db_config() -> dict:
    config_path = os.path.join(get_base_dir(), "db_config.json")
    #config can be replicated from example file, but must be created by user to run this script

    if not os.path.exists(config_path):
        raise FileNotFoundError(
            f"Missing db_config.json at '{config_path}'. Create it from db_config.example.json."
        )

    with open(config_path, "r", encoding="utf-8") as f:
        db_config = json.load(f) #Loads JSON config file for connection stuff

    db_name = db_config.get("database") or DEFAULT_DATABASE
    db_config["database"] = db_name

    # Validate DB name to prevent odd characters / SQL injection via config
    if not re.fullmatch(r"[A-Za-z0-9_]+", db_name):
        raise ValueError("Invalid database name. Use only letters, numbers, and underscores.")

    return db_config


def gen_unique_email(first: str, last: str, used_emails: set[str]) -> str: #Generate email and ensure uniqueness
    #Build email: first initial + last name @ example.edu
    base = f"{first[0].lower()}{last.lower()}"
    email = f"{base}@example.edu"

    #Collision handling: if email already used, append random number until unique
    while email in used_emails:
        email = f"{base}{random.randint(100000, 199999)}@example.edu"

    used_emails.add(email)
    return email


#Generate unique user ID
def gen_unique_user_id(used_ids: set[int]) -> int:
    uid = random.randint(1000000, 1999999)
    while uid in used_ids: #Collision handling: if ID already used, generate a new one until unique
        uid = random.randint(1000000, 1999999)
    used_ids.add(uid)
    return uid


def gen_classification_and_permissions() -> tuple[str, int]:
    classification = random.choices(
        population=list(CLASSIFICATION_PERMISSIONS.keys()),
        weights=[70, 15, 10, 5],
        k=1,
    )[0]
    return classification, CLASSIFICATION_PERMISSIONS[classification]


def gen_password() -> str:
    return fake.password(length=12, special_chars=True, digits=True, upper_case=True, lower_case=True)


def gen_phone_number() -> str:
    return fake.numerify(text="##########")


def build_users() -> list[tuple[int, str, str, str, str, str, int, int, object]]:
    users = []
    used_emails: set[str] = set()
    used_user_ids: set[int] = set()

    #Generate data
    for _ in range(NUM_RECORDS):
        first = fake.first_name()
        last = fake.last_name()

        email = gen_unique_email(first, last, used_emails)
        user_id = gen_unique_user_id(used_user_ids)
        password = gen_password()
        phone_number = gen_phone_number()
        classification, permissions = gen_classification_and_permissions()

        users.append((
            user_id,
            f"{first} {last}",
            email,
            password,
            phone_number,
            classification,
            permissions,
            random.randint(18, 22),
            fake.date_time_this_decade(),  # datetime object; mysql connector handles it
        ))

    return users


def build_guest_users() -> list[tuple[int, str, str, str, str, int, object, object]]:
    guest_users = []
    used_emails: set[str] = set()
    used_user_ids: set[int] = set()

    #Generate data
    for _ in range(NUM_GUEST_USERS):
        first = fake.first_name()
        last = fake.last_name()
        created = fake.date_time_this_year()

        email = gen_unique_email(first, last, used_emails)
        user_id = gen_unique_user_id(used_user_ids)
        password = gen_password()
        phone_number = gen_phone_number()
        revoke_access = fake.date_time_between(start_date=created, end_date="+90d")

        guest_users.append((
            user_id,
            f"{first} {last}",
            email,
            password,
            phone_number,
            random.randint(18, 75),
            created,  # datetime object; mysql connector handles it
            revoke_access,  # datetime object; mysql connector handles it
        ))

    return guest_users


def build_buildings() -> list[tuple[int, str]]:
    building_names = [
        "Admin Hall",
        "Cedar Hall",
        "Library Annex",
        "North Science",
        "South Science",
        "Student Center",
        "East Lecture Hall",
        "West Lecture Hall",
    ]
    return [(index + 1, building_names[index]) for index in range(NUM_BUILDINGS)]


def build_rooms(buildings: list[tuple[int, str]]) -> list[tuple[int, int, bool]]:
    rooms = []

    for building_id, _ in buildings:
        room_count = random.randint(MIN_ROOMS_PER_BUILDING, MAX_ROOMS_PER_BUILDING)
        floor = 1
        room_number = 1

        for _ in range(room_count):
            room_id = (building_id * 1000) + (floor * 100) + room_number
            is_locked = random.choice([True, False])
            rooms.append((room_id, building_id, is_locked))
            room_number += 1

            if room_number > 15:
                floor += 1
                room_number = 1

    return rooms


def build_logs(
    users: list[tuple[int, str, str, str, str, str, int, int, object]],
    rooms: list[tuple[int, int, bool]],
) -> list[tuple[int, int, date]]:
    logs = []
    used_log_keys: set[tuple[int, int, date]] = set()
    room_ids = [room_id for room_id, _, _ in rooms]

    for user_id, *_ in users:
        log_count = random.randint(1, MAX_LOGS_PER_USER)
        for _ in range(log_count):
            log_date = fake.date_between(start_date="-180d", end_date="today")
            room_id = random.choice(room_ids)
            log_key = (user_id, room_id, log_date)

            while log_key in used_log_keys:
                log_date = fake.date_between(start_date="-180d", end_date="today")
                room_id = random.choice(room_ids)
                log_key = (user_id, room_id, log_date)

            used_log_keys.add(log_key)
            logs.append(log_key)

    return logs


def connect_to_mysql(db_config: dict) -> mysql.connector.MySQLConnection:
    #JSON Config file read, applied here for connection
    return mysql.connector.connect(
        host=db_config.get("host", "localhost"),
        user=db_config.get("user"),
        password=db_config.get("password"),
        port=db_config.get("port", 3306),
    )


def prepare_database(cursor, db_name: str) -> None:
    # Ensure database exists, then select it.
    cursor.execute(
        f"CREATE DATABASE IF NOT EXISTS `{db_name}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
    )  #If it doesn't exist, create it. If it does, do nothing
    cursor.execute(f"USE `{db_name}`")


def recreate_tables(cursor) -> None:
    # Drop children first so reseeding stays simple.
    cursor.execute("DROP TABLE IF EXISTS logs")
    cursor.execute("DROP TABLE IF EXISTS rooms")
    cursor.execute("DROP TABLE IF EXISTS guest_users")
    cursor.execute("DROP TABLE IF EXISTS buildings")
    cursor.execute("DROP TABLE IF EXISTS users")

    # Create table
    cursor.execute(USERS_TABLE_SQL)
    cursor.execute(BUILDINGS_TABLE_SQL)
    cursor.execute(GUEST_USERS_TABLE_SQL)
    cursor.execute(ROOMS_TABLE_SQL)
    cursor.execute(LOGS_TABLE_SQL)


def insert_seed_data(cursor, users, guest_users, buildings, rooms, logs) -> None:
    # Insert in batch
    cursor.executemany(INSERT_USERS_SQL, users)
    cursor.executemany(INSERT_BUILDINGS_SQL, buildings)
    cursor.executemany(INSERT_GUEST_USERS_SQL, guest_users)
    cursor.executemany(INSERT_ROOMS_SQL, rooms)
    cursor.executemany(INSERT_LOGS_SQL, logs)


def main() -> None:
    db_config = load_db_config()
    db_name = db_config["database"]

    users = build_users()
    guest_users = build_guest_users()
    buildings = build_buildings()
    rooms = build_rooms(buildings)
    logs = build_logs(users, rooms)

    conn = connect_to_mysql(db_config)
    cursor = conn.cursor()

    try:
        prepare_database(cursor, db_name)
        recreate_tables(cursor)
        insert_seed_data(cursor, users, guest_users, buildings, rooms, logs)
        conn.commit()
    finally:
        cursor.close()
        conn.close()

    print(
        f"Seeded MySQL database '{db_name}' with "
        f"{len(users)} users, {len(guest_users)} guest users, "
        f"{len(buildings)} buildings, {len(rooms)} rooms, and {len(logs)} logs."
    )


if __name__ == "__main__":
    main()
