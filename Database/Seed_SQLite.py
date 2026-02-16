import sqlite3
from faker import Faker
import random


DB_NAME = "test.db"
NUM_RECORDS = 1000  # change this to generate more rows
fake = Faker()
fake.seed_instance(1529344966071113536) #Hidden Key
# Connect to SQLite (creates file if it doesn't exist)
conn = sqlite3.connect(DB_NAME)
cursor = conn.cursor()

# Create table
cursor.execute("""
CREATE TABLE IF NOT EXISTS users (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    Name TEXT NOT NULL,
    Email TEXT UNIQUE NOT NULL,
    Age INTEGER,
    StudentID INTEGER UNIQUE NOT NULL,
    Created_at TEXT
)
""")

# Generate data
data = []
used_emails = set()

for _ in range(NUM_RECORDS):
    first = fake.first_name()
    last = fake.last_name()

    # Build email: first initial + last name
    email = f"{first[0].lower()}{last.lower()}@example.edu"

    # Ensure uniqueness in case of collision

    base_email = email
    while email in used_emails:
        email = f"{first[0].lower()}{last.lower()}{random.randint(100000, 199999)}@example.edu"

    used_emails.add(email)

    data.append((
        f"{first} {last}",
        email,
        random.randint(18, 22),
        fake.unique.random_int(100000, 999999),
        fake.date_time_this_decade().isoformat()
    ))

# Insert in batch (faster than one-by-one)
cursor.executemany("""
INSERT INTO users (name, email, age, studentid, created_at)
VALUES (?, ?, ?, ?, ?)
""", data)

conn.commit()
conn.close()

print(f"Inserted {NUM_RECORDS} fake users into {DB_NAME}")
