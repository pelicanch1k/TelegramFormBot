CREATE TABLE users (
   id SERIAL PRIMARY KEY,
   user_id BIGINT UNIQUE NOT NULL,
   username VARCHAR(255) NOT NULL,
   first_name VARCHAR(255),
   last_name VARCHAR(255),
   middle_name VARCHAR(255),
   utm VARCHAR(255),
   birthdate DATE,
   gender VARCHAR(10),
   state varchar(15)
   photo BYTEA
);
