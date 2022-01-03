#!/bin/bash
sed 's/#max_wal_senders = 10/max_wal_senders = 1/g' /var/lib/postgresql/data/postgresql.conf > /var/lib/postgresql/data/postgresql.conf2
mv /var/lib/postgresql/data/postgresql.conf2 /var/lib/postgresql/data/postgresql.conf

sed 's/#wal_level = replica/wal_level = logical/g' /var/lib/postgresql/data/postgresql.conf > /var/lib/postgresql/data/postgresql.conf2
mv /var/lib/postgresql/data/postgresql.conf2 /var/lib/postgresql/data/postgresql.conf

