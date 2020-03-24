#!/usr/bin/env ash
set -e

java -Dconfig.file="${CONFIG_FILE}" -jar "/app.jar" 
