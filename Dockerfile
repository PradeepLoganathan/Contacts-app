# ------------------------
# Insecure base image usage: using unpinned “latest” tag
# ------------------------
FROM openjdk:21-jdk-slim
# ------------------------
# Hard-coded secrets in ENV
# ------------------------
ENV DB_HOST=localhost
ENV DB_PORT=5432
ENV DB_USER=admin
ENV DB_PASSWORD=Admin123
#test connection
# ------------------------
# Unsafe package install: no pinning, no GPG verification
# ------------------------
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# ------------------------
# Copy application JAR
# ------------------------
COPY target/contacts-0.0.1-SNAPSHOT.jar /app/app.jar

# ------------------------
# Running as root (default) — should use a non-root USER
# ------------------------
USER root

# ------------------------
# Expose a port without using HEALTHCHECK
# ------------------------
EXPOSE 9090

# ------------------------
# Disabling SSL verification in a profile script
# ------------------------
RUN echo "alias curl='curl -k'" >> /etc/profile.d/insecure-curl.sh

# ------------------------
# No HEALTHCHECK defined
# ------------------------
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
