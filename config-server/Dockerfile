FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app

COPY --from=build /app/target/*.jar config-server.jar
COPY --from=build /app/src/main/resources/config/ /app/config/
COPY --from=build /app/src/main/resources/application.yml /app/application.yml

EXPOSE 8888
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=native -jar /app/config-server.jar"]