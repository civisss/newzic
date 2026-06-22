# ── Local Dockerfile (runtime-only) ──
# JAR is pre-built by start.sh → ./gradlew bootJar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
