#!/usr/bin/env sh

find ~/src/prhyme/dark-corpus -type f -print0 | \
    xargs -0 wc -l | \
    grep -v '[0-9]\{4,\}.*total$' | \
    sort -n | \
    awk -f five-number-summary.awk
