FROM mcr.microsoft.com/devcontainers/base:ubuntu-22.04

# Cài đặt các công cụ cơ bản
RUN apt-get update && apt-get install -y curl git gnupg2 jq sudo zsh && apt-get clean

# Cài đặt Python
RUN apt-get install -y python3 python3-pip

# Cài đặt Node.js 20
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

# Cài đặt Java 17 (OpenJDK) và Maven
RUN apt-get install -y openjdk-17-jdk maven