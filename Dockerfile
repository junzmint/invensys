# Sử dụng image của JDK 17
FROM openjdk:17-jdk-slim

# Thiết lập thư mục làm việc trong container
WORKDIR /app

# Cài đặt Maven
RUN apt-get update && \
    apt-get install -y maven=3.6.3*

# Sao chép file pom.xml và các file source code vào container
COPY pom.xml /app

# Build ứng dụng
RUN mvn clean install -DskipTests

COPY config /app/config
COPY src /app/src

# Build ứng dụng
RUN mvn clean package

# Chạy ứng dụng
# CMD ["java", "-jar", "target/invensys-1.0-SNAPSHOT.jar"]

# Thay "your-app.jar" bằng tên file jar của bạn
