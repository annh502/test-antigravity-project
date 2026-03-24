FROM mcr.microsoft.com/devcontainers/base:ubuntu-22.04

# Basic tools
RUN apt-get update && apt-get install -y \
    curl git gnupg2 jq sudo zsh \
    python3 python3-pip \
    openjdk-17-jdk maven \
    && apt-get clean

# Node.js 20
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

# Docker CLI + Docker-in-Docker setup
RUN apt-get install -y ca-certificates lsb-release && \
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
      | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] \
      https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
      > /etc/apt/sources.list.d/docker.list && \
    apt-get update && apt-get install -y \
      docker-ce docker-ce-cli containerd.io docker-compose-plugin \
    && apt-get clean

# Allow vscode user to use Docker without sudo
RUN usermod -aG docker vscode

USER vscode