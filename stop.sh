#!/bin/sh

# kill public python http server.
kill `lsof -t -i:8080`

# kill reports.jar itself.
kill `lsof -t -i:3091`
