FROM maven:3.8.1-openjdk-17-slim AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package

RUN ls -la /app/target

FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/TelegramFormBot-1.0-SNAPSHOT.jar ./app.jar

CMD ["java", "-jar", "app.jar"]