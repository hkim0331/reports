#!/bin/sh
SERV=tiger.melt
DUMP=reports/db-dumps
TODAY=`date +%F`

ssh ${SERV} "cd ${DUMP} && ./dump.sh"
scp ${SERV}:${DUMP}/reports-${TODAY}.sql .

