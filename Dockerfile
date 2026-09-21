FROM eclipse-temurin:21-jdk-ubi9-minimal AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN microdnf install -y findutils && \
    ./mvnw -q package -DskipTests 2>/dev/null || \
    (curl -sL https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz | tar -xz -C /opt && \
     ln -s /opt/apache-maven-3.9.6/bin/mvn /usr/local/bin/mvn && \
     mvn -q package -DskipTests)

FROM eclipse-temurin:21-jre-ubi9-minimal
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
