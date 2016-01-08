#!/bin/sh
http \
    GET https://ilmoeuro@api.github.com/repos/otavanopisto/muikku/issues?milestone=17\&state=closed \
    | jq '.[] | "**", (.labels | map(select(.name == "bug" or .name == "enhancement")) | first | .name), "#", .number, "** ", .title, "\n\n"' -j
