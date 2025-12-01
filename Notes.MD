# Introduce Jenkins CI Pipeline
## Run Jenkins container
docker run -it -p 8080:8080 jenkins/jenkins:lts

## Artifactory Maven Build Example
https://github.com/jenkinsci/pipeline-examples/blob/master/pipeline-examples/artifactory-maven-build/artifactoryMavenBuild.groovy