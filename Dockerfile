# ===== Build stage =====
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn test
RUN mvn clean package -DskipTests

# ===== Package stage =====
# each from starts a completely new image and when building is done
# the above images (from earlier in the stage are discarded
FROM openjdk:17-jdk-alpine
WORKDIR /app
# Copy --from=build means copying just the jar maid in the build stage to this stage
COPY --from=build /app/target/scholarlymail-0.0.1-SNAPSHOT.jar app.jar
# defines a fixed command that always runs
# JSON command array syntax - spaces aren't used as separators, just these commas
ENTRYPOINT ["java", "-jar", "app.jar"]

