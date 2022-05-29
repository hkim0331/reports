#!/bin/sh
SERV=l.melt
lein uberjar
scp target/uberjar/reports.jar ${SERV}:reports/
ssh ${SERV} 'sudo systemctl restart reports'
ssh ${SERV} 'systemctl status reports'
#echo deployed to ${SERV}
