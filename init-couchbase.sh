#!/bin/bash
/entrypoint.sh couchbase-server &

# track if setup is complete so we don't try to setup again
FILE=/opt/couchbase/init/setupComplete.txt

if ! [ -f "$FILE" ]; then
  # used to automatically create the cluster based on environment variables
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-cluster-init.html
  /opt/couchbase/bin/couchbase-cli cluster-init \
    --cluster-username $DB_ROOT_USERNAME \
    --cluster-password $DB_ROOT_PASSWORD \
    --cluster-ramsize 300 \
    --cluster-index-ramsize 256 \
    --services data,index,query \
    --index-storage-setting default


  echo ">>> Creating bucket: articles"
  /opt/couchbase/bin/couchbase-cli bucket-create \
    --cluster localhost \
    --username ${DB_ROOT_USERNAME} \
    --password ${DB_ROOT_PASSWORD} \
    --bucket articles \
    --bucket-type couchbase \
    --bucket-ramsize 200 \
    --enable-flush 1

  echo ">>> Couchbase initialization complete!"
