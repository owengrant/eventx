CREATE TABLE EVENT(
    eventId serial not null check(eventId > -1),
    event varchar(20) not null check(length(event) > 0),
    eventType varchar(10) not null check(length(eventType) > 0),
    revision serial unique not null check(revision > -1),
    entity varchar(20) not null check(length(entity) > 0),
    entityId int not null check(entityId > -1),
    hash varchar(32) not null unique check(length(hash) > 31),
    version smallint not null default 1 check(version > -1),
    data json not null,
    sent boolean not null default false,
    received timestamp not null default now(),
    PRIMARY KEY(revision,entityId,entity)
);
