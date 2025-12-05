FROM jenkins/jenkins:2.528.2-jdk21
USER root

# Install prerequisites
RUN apt-get update && apt-get install -y lsb-release curl gnupg

# Add Docker repo GPG key
RUN curl -fsSLo /usr/share/keyrings/docker-archive-keyring.asc \
    https://download.docker.com/linux/debian/gpg

# Add Docker repository
RUN echo "deb [arch=$(dpkg --print-architecture) \
    signed-by=/usr/share/keyrings/docker-archive-keyring.asc] \
    https://download.docker.com/linux/debian \
    $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list

# Install Docker CLI
RUN apt-get update && apt-get install -y docker-ce-cli

# Allow matching host docker group GID so the Jenkins user can access
# a bind-mounted /var/run/docker.sock. Pass the host GID at build time
# as `--build-arg DOCKER_GID=<gid>`. Default is 999.
ARG DOCKER_GID=999
# Create docker group with the given GID (ignore errors) and add jenkins
# user to that group so it can access the socket at runtime.
RUN groupadd -g ${DOCKER_GID} docker || true \
    && usermod -aG docker jenkins || true

# Switch back to Jenkins user
USER jenkins

# Install plugins
RUN jenkins-plugin-cli --plugins "blueocean docker-workflow json-path-api"
