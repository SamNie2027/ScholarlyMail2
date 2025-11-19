## GET /articles
- Gets all articles. Accepts an empty payload

## GET /articles/:id
- Gets the article specified in the path. Accepts an empty payload

## POST /articles
- Adds the article to the db
- example payload:
- {id: "1", title:"Mavin in 5 minutes", url:"https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html", createdAt="11-03-2025"}

## PATCH /articles/:id
- updates field of the article to a value with the id provided in the path
- example payload:
- {title: "Mavin in 4 minutes"}

## DELETE /articles/:id
- Deletes the article with the specified id
- Calls deleteArticles in the service 

## PATCH /articles/:id/read
- Marks the article with the specified id as read.
- Calls updateArticles in the service with an update for read

### Build/run
first, copy example.env -> .env and add a secure mongodb username and password
```
docker compose up
```
- creates and runs the web and mongodb container
```
docker compose down
```
- destroys both containers but the volume for mongodb stays (and thus the data and mongodb config)
```
docker compose down -v
```
- destroys everything including the volume



### Test
Run tests in the test folder
- Separate tests using mocks for the repository (uses a real lightweight mongodb container), container, and service 
- Integration test (uses a real lightweight mongodb container)
For API tests - import postgres.json into Postgres. Supply the appropriate URL

### What I learned
Using Copilot to create tests
Spring application setup
Containerizing a spring application
Testing a containerized application
Interacting with a containerized database
Creating a compose file
Building containers with authentication
Multi-stage building with docker
Building with maven automatically through docker build
Working with docker volumes
Working with docker networks
API testing for a containerized application
