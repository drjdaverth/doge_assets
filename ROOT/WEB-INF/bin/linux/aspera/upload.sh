#!/bin/sh

source=$1
dest=$2
user=$3
password=$4
host=$5

export ASPERA_SCP_PASS=$password

thisdir=`dirname $0`
#PATH=$thisdir:$PATH
#-T to disable encryption
# use ./ascp -A to see where it looks for license
#can put license file in /usr/local/share/etc/aspera-license
#ascp must be in the path since Aspera does this: Could not chdir to home directory /home/aspera_vdms: No such file or directory

ascpcommand="$thisdir/ascp"
$ascpcommand -T "$source" "$user@$host:$dest"
