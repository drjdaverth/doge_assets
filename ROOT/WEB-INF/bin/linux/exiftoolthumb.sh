#!/bin/sh
input=$1
output=$2
type=$3
exiftool -b -$type "$input" > "$output"
