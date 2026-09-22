# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY config ./config
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

RUN addgroup --system spring && adduser --system -G spring spring

WORKDIR /app

COPY --from=builder --chown=spring:spring /workspace/target/faq-service-0.0.1-SNAPSHOT.jar app.jar

USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=10 \
    CMD wget -qO- localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]