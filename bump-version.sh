#!/bin/sh
#
# origin: ${utils}/src/bump-version.sh
#
# bump-vesion script for clojure projects.
# confused using macos's /usr/bin/sed. so gsed.
#
# CAUSION
# The POSIX standard regular expressions does not support back-references.
# Back-references are considered as an "extended" faciliy.
# This script, bump-version.sh, uses the extended function.
# So, gnu-sed on macOS.

if [ -z "$1" ]; then
    echo "usage: $0 <version>"
    exit
fi

# use  extended regular expressions in the script
if [ -x "${HOMEBREW_PREFIX}/bin/gsed" ]; then
    SED="${HOMEBREW_PREFIX}/bin/gsed -E"
else
    SED="/usr/bin/sed -E"
fi

# project.clj
${SED} -i "s/(defproject \S+) \S+/\1 \"$1\"/" project.clj

# src/cljs/reports/core.cljs
NOW=`date '+%F %T'`
${SED} -i \
  -e "s/(def \^:private version) .+/\1 \"$1\")/" \
  -e "s/(def \^:private now) .+/\1 \"$NOW\")/" \
    src/cljs/reports/core.cljs

# resources/html/home.html
${SED} -i \
 -e "s/app.js\?version=.*/app.js?version=$1\" %}/" \
    resources/html/home.html

# CHANGELOG.md
VER=$1
TODAY=`date +%F`
${SED} -i -e "/SNAPSHOT/c\
## ${VER} / ${TODAY}" CHANGELOG.md
