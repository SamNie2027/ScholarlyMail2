FROM couchbase/server:latest AS stage_base
# FROM couchbase:latest AS stage_base
RUN mkdir -p /opt/couchbase/init
COPY init-cbserver.sh /opt/couchbase/init/init-cbserver.sh
RUN chmod +x /opt/couchbase/init/init-cbserver.sh