USE identity;

CREATE TABLE IF NOT EXISTS users(
  id              uuid,
  client_id       uuid,
  username        varchar,
  email           varchar,
  first_name      varchar,
  last_name       varchar,
  hashed_password varchar, PRIMARY KEY (id)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS users_by_username AS
  SELECT * FROM users
  WHERE username IS NOT NULL
  PRIMARY KEY (username, id);

CREATE TABLE IF NOT EXISTS reserved_usernames(
    username varchar,
    id       uuid, PRIMARY KEY (username)
);

CREATE TABLE IF NOT EXISTS reserved_emails(
    email    varchar,
    id       uuid, PRIMARY KEY (email)
);