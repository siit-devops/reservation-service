FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/reservation-service-0.0.1-SNAPSHOT.jar /app/
EXPOSE 8081
CMD ["java", "-jar", "reservation-service-0.0.1-SNAPSHOT.jar"]