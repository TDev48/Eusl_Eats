# Database

- The tables are created **automatically** from the Java classes when the back end starts on an empty
  database (set the environment variable `DDL_AUTO=update`; the Docker setup already does this).
- `notifications.sql` is the same SQL the back end uses for the notifications table (reference only).
- **No data is stored in this repository.** Your own database contains emails, phone numbers and password
  hashes, so it must never be uploaded. Back it up privately (see `deploy/backup.sh`).
