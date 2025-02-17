# TelegramFormBot

TelegramFormBot is a Java-based bot designed to handle form submissions via Telegram.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Bot](#running-the-bot)
- [Docker](#docker)
- [License](#license)

## Prerequisites

- Java Development Kit (JDK) 11 or higher
- Apache Maven
- Docker (optional, for running with Docker)

## Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/pelicanch1k/TelegramFormBot.git
    cd TelegramFormBot
    ```

2. Build the project using Maven:
    ```sh
    mvn clean install
    ```

## Configuration

1. Copy the example configuration file and modify it with your own settings:
    ```sh
    cp src/main/resources/application.example.properties src/main/resources/application.properties
    ```

2. Open the `.env` file and update the following fields with your Telegram bot token and other necessary configurations:
    ```env
    BOT_TOKEN=YOUR_TELEGRAM_BOT_TOKEN
    # Add other configuration settings here
    ```

## Running the Bot

1. After building the project and configuring the properties, you can run the bot using the following command:
    ```sh
    java -cp . Main.java
    ```

## Run the code in src/main/resources/schema.sql

1. Run this code:
   ```sql
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
   ```

## Docker

You can also run the bot using Docker-сompose.
```sh
docker-compose up --build
```


## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
