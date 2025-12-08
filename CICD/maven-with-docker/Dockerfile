FROM maven:3.9.8-eclipse-temurin-17

USER root

# Install Docker CLI on Ubuntu
RUN apt-get update && \
    apt-get install -y \
        ca-certificates \
        curl \
        gnupg \
        lsb-release && \
    mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list && \
    apt-get update && \
    apt-get install -y docker-ce-cli

## Allow matching host docker group GID so the container user can access
## a bind-mounted /var/run/docker.sock. Pass host GID with --build-arg
## DOCKER_GID=<gid>. Default is 999.
ARG DOCKER_GID=1001
ARG JENKINS_UID=1002
ARG JENKINS_GID=1002

# Create a `jenkins` user (so the container can run non-root) and ensure
# it is a member of the docker group with the host-supplied GID.
RUN groupadd -g ${JENKINS_GID} jenkins || true \
    && useradd -u ${JENKINS_UID} -g ${JENKINS_GID} -m -s /bin/bash jenkins || true \
    && groupadd -g ${DOCKER_GID} docker || true \
    && usermod -aG docker jenkins || true \
    && mkdir -p /root/.docker \
    && echo '{"tls": false}' > /root/.docker/config.json \
    && touch /root/.docker/ca.pem

USER jenkins
