# Putting EUSL Eats online (one server, free)

Everything runs on ONE small Linux server with Docker:
website + back end + database + free HTTPS. Students and stores just open one web address.

```
Browser  ->  Caddy (web, HTTPS)  ->  /api, /uploads -> Spring Boot  ->  database
                                  ->  everything else = your HTML/CSS/JS files
```

## Step 0 - Choose where the server lives (all free)

| Option | Free? | Good | Watch out |
|---|---|---|---|
| **University server** - ask the ICT centre for a Linux VM or a sub-domain | Free | Most stable, trusted address, no card | Needs their permission |
| **Azure for Students** - $100 credit, no credit card, student email | Free for 12 months | No card; easy portal | Credit runs out; pick a size with 2 GB+ RAM |
| **Oracle Cloud "Always Free"** (Arm VM) | Free, no end date | Free forever | Needs a bank card for checking; "out of capacity" errors are common; free size was cut to 2 CPU / 12 GB in 2026; unused servers can be reclaimed |

Any of them works with the same files. You need: **Ubuntu 22.04 or 24.04, at least 2 GB RAM** (1 GB + swap works for a small start), and ports **80 and 443** open to the internet.

## Step 1 - Database (nothing to do)

The database tables are created automatically the first time the back end starts
(`DDL_AUTO=update` is already set in `docker-compose.yml`), and the admin account is created too.
The live site starts clean - your test data is NOT copied.

Only if you want to move your existing data: export it from phpMyAdmin and place the `.sql` file in
`deploy/db-init/` before the first start. If phpMyAdmin says "MySQL" (not MariaDB), set `DB_IMAGE=mysql:8.4` in `.env`.

## Step 2 - Create the server and open ports

Create an Ubuntu server with your chosen provider and note its **public IP address**.
Allow inbound **TCP 80 and 443** (Azure: "Networking -> Add inbound port rule"; Oracle: VCN security list).
On Oracle Ubuntu images also run:
```
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save
```

## Step 3 - Install and start (on the server)

From your PC, send the project (replace the user and IP):
```
scp EUSL_Eats_Deploy.zip ubuntu@SERVER_IP:~
ssh ubuntu@SERVER_IP
```
On the server:
```
sudo apt update && sudo apt install -y unzip
unzip EUSL_Eats_Deploy.zip
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER      # then log out and back in
cd EUSL_Eats_Final/deploy
cp .env.example .env
nano .env                          # fill in the passwords (openssl rand -hex 24 makes good ones)
docker compose up -d --build       # first time takes several minutes
docker compose ps                  # all three should say "running"
```
Only 1 GB RAM? Add swap first: `sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile`

Open `http://SERVER_IP` in your browser. The website should appear.
If it does not: `docker compose logs backend` shows the reason.

## Step 4 - Free name + HTTPS

1. Go to https://www.duckdns.org, log in, create a name (e.g. `eusleats`), and set its IP to your server's IP.
2. In `deploy/.env` set `DOMAIN=eusleats.duckdns.org`.
3. Run `docker compose up -d`. Caddy gets a free certificate by itself (ports 80/443 must be open).

Your site is now `https://eusleats.duckdns.org`.

## Step 5 - Test before telling students

1. Log in at `/admin-login.html` with the ADMIN_EMAIL / ADMIN_PASSWORD from `.env`.
2. Register a store, approve it as admin, add a food item (with a photo).
3. Register a student, place an order, cancel one, check notifications.
4. Change a status as the store; the student sees it.

## Looking after it

- **Update the site** after you change files: upload the new files, then `cd deploy && docker compose up -d --build`.
- **Backups** (do this!): `sh backup.sh` saves the database and uploaded photos in `deploy/backups`.
  Run it every night: `crontab -e` and add `0 2 * * * cd /home/ubuntu/EUSL_Eats_Final/deploy && sh backup.sh`.
  Copy the backups off the server sometimes (WinSCP / `scp`).
- **Logs**: `docker compose logs -f backend`.
- **Restart**: `docker compose restart`.
- **Stop**: `docker compose down` (data stays; `down -v` would delete it - never use `-v`).

## Good to know

- Text messages to stores are only written to the log until you set `SMS_PROVIDER=notifylk` and add the Notify.lk keys in `.env`.
- Uploaded food photos live in a Docker volume, so they survive restarts and updates (but not a deleted server - keep backups).
- Keep `.env` private. Never share it or upload it to GitHub.
- A free single server has no spare: if it goes down, the site is down. Backups let you rebuild it in under an hour.
