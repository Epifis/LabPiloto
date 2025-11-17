# ===============================
# ETAPA DE CONSTRUCCIÓN
# ===============================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar archivos de Maven
COPY backend/labpilot/pom.xml .
COPY backend/labpilot/src ./src

# Copiar properties de producción
COPY application-docker.properties ./src/main/resources/application.properties

# Construir la aplicación
RUN mvn clean package -DskipTests

# ===============================
# ETAPA DE EJECUCIÓN
# ===============================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el JAR construido
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Comando de ejecución
ENTRYPOINT ["java", "-jar", "app.jar"]