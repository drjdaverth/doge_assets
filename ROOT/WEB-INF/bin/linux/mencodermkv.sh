#!/bin/sh
input=$1
output=$2

cd mplayer4win

wine mencoder.exe "$input" -ofps 25 -ovc x264 -x264encopts bitrate=1333:pass=1:threads=2 -oac faac -faacopts mpeg=4:object=2:raw:br=96   -channels 1  -o nul
wine mencoder.exe "$input" -ofps 25 -ovc x264 -x264encopts bitrate=1333:pass=2:threads=2 -oac faac -faacopts mpeg=4:object=2:raw:br=96   -channels 1 -o "$output"

cd ..
