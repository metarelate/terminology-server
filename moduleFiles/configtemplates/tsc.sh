#!/bin/bash

exec java -Dwicket.configuration=deployment -jar <<commandFile>>  "$@" 