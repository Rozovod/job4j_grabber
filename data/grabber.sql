create table post(
    id serial primary key,
    name text,
    link text unique,
    text text,
    created timestamp
);