#!/bin/sh
# Daily database backup. Run from the deploy folder:  sh backup.sh
# Keeps the 14 newest backups in ./backups
set -e
cd "$(dirname "$0")"
mkdir -p backups
. ./.env
STAMP=$(date +%Y-%m-%d_%H%M)
docker compose exec -T db sh -c "mysqldump -uroot -p\"$DB_ROOT_PASSWORD\" smart_cafeteria 2>/dev/null || mariadb-dump -uroot -p\"$DB_ROOT_PASSWORD\" smart_cafeteria" | gzip > "backups/db_$STAMP.sql.gz"
# also save the uploaded food photos
docker run --rm -v deploy_uploads:/data -v "$(pwd)/backups":/backup alpine tar czf "/backup/uploads_$STAMP.tar.gz" -C /data . 2>/dev/null || true
ls -1t backups/db_*.sql.gz 2>/dev/null | tail -n +15 | xargs -r rm
ls -1t backups/uploads_*.tar.gz 2>/dev/null | tail -n +15 | xargs -r rm
echo "Backup saved: backups/db_$STAMP.sql.gz"
