FROM maven:3.8.1-jdk-11

COPY pom.xml .
COPY src src

RUN mvn install

VOLUME /tmp

EXPOSE 8080

CMD ["java", "-jar", "/target/rewards-0.0.1-SNAPSHOT.jar"]
