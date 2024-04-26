#!/bin/sh

# start python http server.
./public/start.sh &

export PORT=3091
export DATABASE_URL='postgresql://localhost/reports?user=postgres&password=password'
export USERS_DB='https://l22.melt.kyutech.ac.jp/api/user/'
export UPLOAD_TO='/home/ubuntu/reports/public'
export HP_URL='https://hp.melt.kyutech.ac.jp/'

java -jar reports.jar

