FROM postgres:13-bullseye
RUN apt-get update
RUN apt-get -y install postgresql-13-wal2json
COPY editconf.sh /docker-entrypoint-initdb.d/