# ---------- build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# (опционально) сначала скачиваем зависимости ускорит последующие сборки
RUN ./gradlew --no-daemon dependencies || true

COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---------- run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -m appuser
USER appuser
ENV HOME=/home/appuser

COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
