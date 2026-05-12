# ── Étape 1 : build du jar avec Maven ────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# On copie d'abord le pom.xml seul pour profiter du cache Docker
# (les dépendances ne sont re-téléchargées que si pom.xml change)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Puis on copie le code source et on build
COPY src ./src
RUN mvn package -DskipTests -q

# ── Étape 2 : image finale légère ────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/Loc-mns-Back-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# -Xmx256m limite la RAM utilisée par la JVM
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]
