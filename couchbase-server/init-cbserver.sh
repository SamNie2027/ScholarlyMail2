#!/bin/bash
# used to start couchbase server - can't get around this as docker compose only allows you to start one command - so we have to start couchbase like the standard couchbase Dockerfile would 
# https://github.com/couchbase/docker/blob/master/enterprise/couchbase-server/7.0.3/Dockerfile#L82

/entrypoint.sh couchbase-server & 

# top-level timer
SCRIPT_START_TS=$(date +%s)
echo "[init-cbserver] script start at $(date -u --date=@$SCRIPT_START_TS 2>/dev/null || date)" >/dev/stderr

log_stage() {
  stage_name="$1"
  stage_start_ts="$2"
  now_ts=$(date +%s)
  stage_elapsed=$((now_ts - stage_start_ts))
  total_elapsed=$((now_ts - SCRIPT_START_TS))
  echo "[init-cbserver] STAGE ${stage_name} finished: ${stage_elapsed}s (total ${total_elapsed}s)" >/dev/stderr
}

# track if setup is complete so we don't try to setup again
FILE=/opt/couchbase/init/setupComplete.txt

if ! [ -f "$FILE" ]; then
  # used to automatically create the cluster based on environment variables
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-cluster-init.html

  echo $COUCHBASE_ADMINISTRATOR_USERNAME ":"  $COUCHBASE_ADMINISTRATOR_PASSWORD  
  # Wait for the Couchbase REST API to be available on 127.0.0.1:8091
  echo "Waiting for Couchbase REST API on 127.0.0.1:8091..."
  stage_start=$(date +%s)
  MAX_WAIT_API=120
  WAITED_API=0
  until /opt/couchbase/bin/curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8091/pools | grep -q "200"; do
    if [ "$WAITED_API" -ge "$MAX_WAIT_API" ]; then
      echo "Timed out waiting for Couchbase API after ${MAX_WAIT_API}s"
      break
    fi
    echo "Couchbase API not ready yet, sleeping 5s... (waited ${WAITED_API}s)"
    sleep 5
    WAITED_API=$((WAITED_API+5))
  done
  log_stage "REST_API_ready" $stage_start

  # Retry cluster-init to handle transient connection/refused errors during startup
  echo "Initializing cluster (with retries)..."
  CLUSTER_INIT_ATTEMPTS=10
  SLEEP_BETWEEN=5
  ci_attempt=1
  stage_start=$(date +%s)
  while [ $ci_attempt -le $CLUSTER_INIT_ATTEMPTS ]; do
    echo "cluster-init attempt $ci_attempt"
    /opt/couchbase/bin/couchbase-cli cluster-init -c 127.0.0.1 \
      --cluster-username $COUCHBASE_ADMINISTRATOR_USERNAME \
      --cluster-password $COUCHBASE_ADMINISTRATOR_PASSWORD \
      --services data,index,query \
      --cluster-ramsize $COUCHBASE_RAM_SIZE \
      --cluster-index-ramsize $COUCHBASE_INDEX_RAM_SIZE \
      --index-storage-setting default && break
    echo "cluster-init failed; sleeping ${SLEEP_BETWEEN}s before retry"
    sleep $SLEEP_BETWEEN
    ci_attempt=$((ci_attempt+1))
  done
  log_stage "cluster_init" $stage_start

  sleep 2s

  # used to auto create the bucket based on environment variables
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-bucket-create.html

  echo "Creating bucket (with retries)..."
  BUCKET_ATTEMPTS=10
  b_attempt=1
  stage_start=$(date +%s)
  while [ $b_attempt -le $BUCKET_ATTEMPTS ]; do
    echo "bucket-create attempt $b_attempt"
    /opt/couchbase/bin/couchbase-cli bucket-create -c localhost:8091 \
      --username $COUCHBASE_ADMINISTRATOR_USERNAME \
      --password $COUCHBASE_ADMINISTRATOR_PASSWORD \
      --bucket $COUCHBASE_BUCKET \
      --bucket-ramsize $COUCHBASE_BUCKET_RAMSIZE \
      --bucket-type couchbase && break
    echo "bucket-create failed; sleeping ${SLEEP_BETWEEN}s before retry"
    sleep $SLEEP_BETWEEN
    b_attempt=$((b_attempt+1))
  done
  log_stage "bucket_create" $stage_start

  sleep 2s

  # used to auto create the sync gateway user based on environment variables  
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-user-manage.html#examples

  echo "Creating RBAC user (with retries)..."
  USER_ATTEMPTS=10
  u_attempt=1
  stage_start=$(date +%s)
  while [ $u_attempt -le $USER_ATTEMPTS ]; do
    echo "user-manage attempt $u_attempt"
    /opt/couchbase/bin/couchbase-cli user-manage \
      --cluster http://127.0.0.1 \
      --username $COUCHBASE_ADMINISTRATOR_USERNAME \
      --password $COUCHBASE_ADMINISTRATOR_PASSWORD \
      --set \
      --rbac-username $COUCHBASE_RBAC_USERNAME \
      --rbac-password $COUCHBASE_RBAC_PASSWORD \
      --roles mobile_sync_gateway[*] \
      --auth-domain local && break
    echo "user-manage failed; sleeping ${SLEEP_BETWEEN}s before retry"
    sleep $SLEEP_BETWEEN
    u_attempt=$((u_attempt+1))
  done
  log_stage "rbac_user_create" $stage_start

  sleep 10s

  # Retry loop for index creation to handle transient states (rebalance, indexing not ready, etc.)
  echo "Creating primary index with retries..."
  CREATE_ATTEMPTS=30
  SLEEP_BETWEEN=5
  attempt=1
  stage_start=$(date +%s)
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
  if [ $attempt -le $CREATE_ATTEMPTS ]; then
    log_stage "index_create_success" $stage_start
  else
    log_stage "index_create_timeout" $stage_start
    echo "Warning: failed to create primary index after ${CREATE_ATTEMPTS} attempts. Continuing startup."
  fi

  # create file so we know that the cluster is setup and don't run the setup again 
  touch $FILE
  log_stage "setup_complete_touch" $SCRIPT_START_TS
fi 
  # docker compose will stop the container from running unless we do this
  # known issue and workaround
  echo "[init-cbserver] entering tail -f /dev/null (container will remain running)" >/dev/stderr
  TOTAL_END_TS=$(date +%s)
  echo "[init-cbserver] total runtime: $((TOTAL_END_TS - SCRIPT_START_TS))s" >/dev/stderr
  tail -f /dev/null

