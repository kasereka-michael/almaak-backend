#!/bin/bash
# Start MySQL service
service mysql start

# Wait for MySQL to be ready
until mysqladmin ping -h localhost -P 3369 -u root -pmichael --silent; do
    echo "Waiting for MySQL to be ready..."
    sleep 2
done

# Start the Spring Boot application
java -jar app.jar