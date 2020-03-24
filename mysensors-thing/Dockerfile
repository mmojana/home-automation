FROM maven:3.6-jdk-8-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -e -B dependency:resolve
COPY src ./src
RUN mvn -e -B -DfinalName=app package

FROM openjdk:8-jre-alpine
COPY --from=builder /app/target/app.jar /

COPY entrypoint.sh /tmp
RUN chmod u+x /tmp/entrypoint.sh

ENTRYPOINT ["/tmp/entrypoint.sh"]
