#!/bin/sh
localpath=$1
checkoutpath=$2

mkdir -p $localpath
cd $localpath
git --bare init
git config core.sharedrepository 1
git config receive.denyNonFastforwards true


mkdir -p $checkoutpath
cd $checkoutpath
git clone $localpath
