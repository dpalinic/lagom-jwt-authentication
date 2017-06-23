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
    user_id  uuid,
    created_on timestamp, PRIMARY KEY (username)
);

CREATE TABLE IF NOT EXISTS reserved_emails(
    email    varchar,
    user_id  uuid,
    created_on timestamp, PRIMARY KEY (email)
);