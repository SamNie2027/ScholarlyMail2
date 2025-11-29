#!/bin/bash
# used to start couchbase server - can't get around this as docker compose only allows you to start one command - so we have to start couchbase like the standard couchbase Dockerfile would 
# https://github.com/couchbase/docker/blob/master/enterprise/couchbase-server/7.0.3/Dockerfile#L82

/entrypoint.sh couchbase-server & 

# track if setup is complete so we don't try to setup again
FILE=/opt/couchbase/init/setupComplete.txt

if ! [ -f "$FILE" ]; then
  # used to automatically create the cluster based on environment variables
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-cluster-init.html

  echo $COUCHBASE_ADMINISTRATOR_USERNAME ":"  $COUCHBASE_ADMINISTRATOR_PASSWORD  

  sleep 10s 
  /opt/couchbase/bin/couchbase-cli cluster-init -c 127.0.0.1 \
  --cluster-username $COUCHBASE_ADMINISTRATOR_USERNAME \
  --cluster-password $COUCHBASE_ADMINISTRATOR_PASSWORD \
  --services data,index,query \
  --cluster-ramsize $COUCHBASE_RAM_SIZE \
  --cluster-index-ramsize $COUCHBASE_INDEX_RAM_SIZE \
  --index-storage-setting default

  sleep 2s 

  # used to auto create the bucket based on environment variables
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-bucket-create.html

  /opt/couchbase/bin/couchbase-cli bucket-create -c localhost:8091 \
  --username $COUCHBASE_ADMINISTRATOR_USERNAME \
  --password $COUCHBASE_ADMINISTRATOR_PASSWORD \
  --bucket $COUCHBASE_BUCKET \
  --bucket-ramsize $COUCHBASE_BUCKET_RAMSIZE \
  --bucket-type couchbase 

  sleep 2s 

  # used to auto create the sync gateway user based on environment variables  
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-user-manage.html#examples

  /opt/couchbase/bin/couchbase-cli user-manage \
  --cluster http://127.0.0.1 \
  --username $COUCHBASE_ADMINISTRATOR_USERNAME \
  --password $COUCHBASE_ADMINISTRATOR_PASSWORD \
  --set \
  --rbac-username $COUCHBASE_RBAC_USERNAME \
  --rbac-password $COUCHBASE_RBAC_PASSWORD \
  --roles mobile_sync_gateway[*] \
  --auth-domain local

  sleep 2s 

  # Wait for cluster tasks (eg. rebalance) to finish before creating indexes.
  # If a rebalance is in progress, index creation will fail with a 500 error.
  echo "Waiting for any rebalance tasks to complete..."
  MAX_WAIT=120
  WAITED=0
  while /opt/couchbase/bin/curl -s -u $COUCHBASE_ADMINISTRATOR_USERNAME:$COUCHBASE_ADMINISTRATOR_PASSWORD http://127.0.0.1:8091/pools/default/tasks | grep -q '"type"\s*:\s*"rebalance"' ; do
    if [ "$WAITED" -ge "$MAX_WAIT" ]; then
      echo "Timed out waiting for rebalance to finish (waited ${MAX_WAIT}s). Proceeding to attempts to create index."
      break
    fi
    echo "Rebalance in progress, sleeping 5s... (waited ${WAITED}s)"
    sleep 5
    WAITED=$((WAITED+5))
  done

  # Retry loop for index creation to handle transient states (rebalance, indexing not ready, etc.)
  echo "Creating primary index with retries..."
  CREATE_ATTEMPTS=30
  SLEEP_BETWEEN=5
  attempt=1
  while [ $attempt -le $CREATE_ATTEMPTS ]; do
    echo "Attempt $attempt: creating primary index"
    # Capture both body and HTTP code
    resp=$(/opt/couchbase/bin/curl -s -w "\n%{http_code}" http://localhost:8093/query/service \
      -u $COUCHBASE_ADMINISTRATOR_USERNAME:$COUCHBASE_ADMINISTRATOR_PASSWORD \
      -d "statement=CREATE PRIMARY INDEX \`idx_primary_articles\` ON \`$COUCHBASE_BUCKET\`")
    http_code=$(echo "$resp" | tail -n1)
    body=$(echo "$resp" | sed '$d')

    if [ "$http_code" = "200" ]; then
      # check for errors in body
      if echo "$body" | /bin/grep -q '"errors"\s*:\s*\['; then
        echo "Index create returned errors: $body"
      else
        echo "Index created successfully."
        break
      fi
    else
      echo "HTTP $http_code from query service. Response: $body"
    fi

    # If response mentions rebalance or indexing not ready, wait and retry
    if echo "$body" | /bin/grep -qi 'rebalance\|index.*cannot start\|CreatePrimaryIndex'; then
      echo "Transient indexing error detected; sleeping ${SLEEP_BETWEEN}s before retry"
      sleep $SLEEP_BETWEEN
      attempt=$((attempt+1))
      continue
    fi

    # For other errors, still retry a few times
    echo "Unknown error during index creation. Sleeping ${SLEEP_BETWEEN}s before retry"
    sleep $SLEEP_BETWEEN
    attempt=$((attempt+1))
  done

  if [ $attempt -gt $CREATE_ATTEMPTS ]; then
    echo "Warning: failed to create primary index after ${CREATE_ATTEMPTS} attempts. Continuing startup."
  fi

  # create file so we know that the cluster is setup and don't run the setup again 
  touch $FILE
fi 
  # docker compose will stop the container from running unless we do this
  # known issue and workaround
  tail -f /dev/null

