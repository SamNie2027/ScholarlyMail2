# Running with docker compose up
- The spring application is built by maven and exists as a lightweight final package with the java runtime, then a docker image is built.
- This will run the images required: mongodb, the spring application, and the mongo db volume if it doesn't already exist. 
- The database will take on the username and password specified in a .env, follow the format specified in example.env
- The username and password are only set when the volume is created

# Tearing down with docker compose down
- This will tear down the mongodb and spring application
- The mongodb data will still exist in the mongodb volume

# Tearing down with docker compose down -v
- This will tear down everything - including the volume
- Thus your data will be lost

# Endpoints

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

## PATCH /articles/:id/read
- Marks the article with the specified id as read.