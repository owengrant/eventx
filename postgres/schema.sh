#!/bin/bash
set -e

# Create user and database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
CREATE USER eventx WITH ENCRYPTED PASSWORD 'sf*&sdnfd()sdf';
CREATE DATABASE eventx;
GRANT ALL PRIVILEGES ON DATABASE eventx TO eventx;
EOSQL

# Create extensions
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "eventx" <<-EOSQL
-- Add postgis extension
CREATE EXTENSION postgis;

-- ADD uuid extension
CREATE EXTENSION  pgcrypto;
EOSQL

psql -v ON_ERROR_STOP=1 --username "eventx" --dbname "eventx" <<-EOSQL
-- Create Event table
CREATE TABLE EVENT(
    "eventId" serial not null check("eventId" > -1),
    context varchar not null check(length(context) > 0),
    event varchar not null check(length(event) > 0),
    "eventType" varchar not null check(length("eventType") > 0),
    revision int not null check(revision > -1),
    entity varchar not null check(length(entity) > 0),
    "entityId" varchar not null check(length("entityId") > 31),
    hash varchar not null unique check(length(hash) > 31),
    version integer not null default 1 check(version > -1),
    data json not null,
    sent boolean not null default false,
    received timestamp not null default now(),
    CONSTRAINT primary_key PRIMARY KEY(context,entity,"entityId",revision)
);

EOSQL