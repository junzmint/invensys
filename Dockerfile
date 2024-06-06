FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update && \
    apt-get install -y maven=3.6.3*

COPY pom.xml /app

RUN mvn clean install -DskipTests

COPY config /app/config
COPY src /app/src

RUN mvn clean package

