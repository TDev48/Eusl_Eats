# ---------- 1) build the application ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /build

COPY .mvn .mvn
COPY mvnw pom.xml ./
# Windows line endings would break the wrapper script on Linux
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -q -B dependency:go-offline

COPY src src
RUN ./mvnw -q -B -DskipTests package

# ---------- 2) small image that only runs it ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

# uploaded food photos are stored here (a Docker volume keeps them safe)
RUN mkdir -p /app/uploads
ENV UPLOAD_DIR=/app/uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
