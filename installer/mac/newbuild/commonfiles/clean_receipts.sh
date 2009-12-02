#!/bin/sh -x

pkg_name=$1
module_id=$2

script_dir=`dirname "$0"`


if [ -d "/Library/Receipts/$pkg_name.pkg" ] ; then
    rm -rvf "/Library/Receipts/$pkg_name.pkg"
fi

if [ -d "/private/var/db/receipts" ] ; then
    rm -rvf "/private/var/db/receipts/$module_id"*
fi