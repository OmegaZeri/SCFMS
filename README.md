# SCFMS Seed Tool

This repo includes a Python seed script and a packaged Windows executable for creating a MySQL database, creating a `users` table, and inserting fake test data. 

## Running the EXE

Place these two files in the same folder:

- `SeedDB_mariaDB.exe`
- `db_config.json`
To build the db_config.json, please look at the example config file provided.

Then run the exe by double-clicking it or from PowerShell:

```powershell
.\SeedDB_mariaDB.exe
```

## Requirements for the EXE

The packaged exe does not require Python or pip packages on the target machine.

The target machine does need:

- MySQL Server installed and running, or access to a reachable MySQL server
- A valid `db_config.json` next to the exe
- A MySQL user with permission to create a database and tables

## Running the Python Script

From the repo root:

```powershell
py Database\SeedDB_mariaDB.py
```

Install dependencies first if needed:

```powershell
py -m pip install faker mysql-connector-python
```

## Rebuilding the EXE

Install PyInstaller:

```powershell
py -m pip install pyinstaller
```

Build the executable:

```powershell
py -m PyInstaller --onefile --hidden-import=faker Database\SeedDB_mariaDB.py
```

The built exe will be created in `dist\`.
