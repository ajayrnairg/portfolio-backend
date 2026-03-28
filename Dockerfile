# Stage 1: Build
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Grant execute permission and download dependencies
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Copy source and build
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Fine-tune memory for 512MB RAM Free Tier
# Java 25 has great container awareness, but we'll be safe
ENV JAVA_OPTS="-Xmx384m -Xms128m"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]