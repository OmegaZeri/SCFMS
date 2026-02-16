import json
import os
import random
import re
from faker import Faker
import mysql.connector



#CONFIG
NUM_RECORDS = 3000  # change this to generate more rows/Generate larger data set for better testing of performance and indexing
fake = Faker()
fake.seed_instance(1529344966071113536)  # Hidden Key

CONFIG_PATH = os.path.join(os.path.dirname(__file__), "db_config.json")
#config can be replicated from example file, but must be created by user to run this script.

if not os.path.exists(CONFIG_PATH):
    raise FileNotFoundError(
        "Missing db_config.json. Create it from db_config.example.json."
    )

with open(CONFIG_PATH, "r", encoding="utf-8") as f:
    DB_CONFIG = json.load(f) #Loads JSON config file for connection stuff.

DB_NAME = DB_CONFIG.get("database")
if not DB_NAME:
    raise ValueError("db_config.json must include a non-empty 'database' field.")

# Validate DB name to prevent odd characters / SQL injection via config
if not re.fullmatch(r"[A-Za-z0-9_]+", DB_NAME):
    raise ValueError("Invalid database name. Use only letters, numbers, and underscores.")


def gen_unique_email(first: str, last: str, used_emails: set[str]) -> str: #Generate email and ensure uniqueness
    #Build email: first initial + last name @ example.edu
    base = f"{first[0].lower()}{last.lower()}"
    email = f"{base}@example.edu"

    #Collision handling: if email already used, append random number until unique
    while email in used_emails:
        email = f"{base}{random.randint(100000, 199999)}@example.edu"

    used_emails.add(email)
    return email

#Generate unique student ID
def gen_unique_student_id(used_ids: set[int]) -> int:
    sid = random.randint(100000, 999999)
    while sid in used_ids: #Collision handling: if ID already used, generate a new one until unique
        sid = random.randint(100000, 999999)
    used_ids.add(sid)
    return sid

#JSON Config file read, applied here for connection
conn = mysql.connector.connect(
    host=DB_CONFIG.get("host", "localhost"),
    user=DB_CONFIG.get("user"),
    password=DB_CONFIG.get("password"),
    port=DB_CONFIG.get("port", 3306),
)
cursor = conn.cursor()

# Ensure database exists, then select it.
cursor.execute(f"CREATE DATABASE IF NOT EXISTS `{DB_NAME}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")  #If it doesn't exist, create it. If it does, do nothing.
cursor.execute(f"USE `{DB_NAME}`")

# Create table
cursor.execute("""
CREATE TABLE IF NOT EXISTS users (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Email VARCHAR(150) NOT NULL UNIQUE,
    Age INT,
    StudentID INT NOT NULL UNIQUE,
    Created DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
""")

# Generate data
data = []
used_emails = set()
used_student_ids = set()

for _ in range(NUM_RECORDS):
    first = fake.first_name()
    last = fake.last_name()

    email = gen_unique_email(first, last, used_emails)
    student_id = gen_unique_student_id(used_student_ids)

    data.append((
        f"{first} {last}",
        email,
        random.randint(18, 22),
        student_id,
        fake.date_time_this_decade(),  # datetime object; mysql connector handles it
    ))

# Insert in batch
cursor.executemany("""
INSERT INTO users (Name, Email, Age, StudentID, Created)
VALUES (%s, %s, %s, %s, %s)
""", data)

conn.commit()
cursor.close()
conn.close()

print(f"Inserted {NUM_RECORDS} fake users into MariaDB/MySQL database '{DB_CONFIG['database']}'")
