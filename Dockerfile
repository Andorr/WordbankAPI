FROM maven:3.6.3-jdk-11-slim AS builder

RUN mkdir app
WORKDIR app

# Build
ADD pom.xml /app
RUN mvn clean package

# Copy project to container
COPY . /app
RUN mvn package

RUN (pwd)
RUN ls
RUN cd ./target && ls


FROM openjdk:11-jdk-slim
WORKDIR /root/

COPY --from=builder /app/target/*-jar-with-dependencies.jar app.jar
EXPOSE ${PORT}

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","./app.jar"]