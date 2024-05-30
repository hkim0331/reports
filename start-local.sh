#!/bin/sh
export DATABASE_URL="postgresql://localhost/reports?user=postgres"
export USERS_DB="https://l22.melt.kyutech.ac.jp/api/user/"
export UPLOAD_TO="/Volumes/RAM_DISK"

# trail '/' is necessary.
# export HP_URL="http://localhost:8080/"
export HP_URL="https://hp.melt.kyutech.ac.jp/"

export RP_MODE="student" # normal, exam, student

java -jar target/uberjar/reports.jar
