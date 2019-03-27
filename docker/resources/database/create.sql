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
