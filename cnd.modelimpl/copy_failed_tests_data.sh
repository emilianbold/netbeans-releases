#!/bin/sh -x
#
# For all tests that failed,
# copy goldens and data into build/test/unit/work/tmp
#
MODELIMPL="${MODELIMPL-.}"
failed_dirs=`find ${MODELIMPL}/build/test/unit/work -name "*.golden" -exec dirname {} \; | sort -u`
if [ -z "${failed_dirs}" ]; then
    echo "No failed tests found"
else
    dst="${MODELIMPL}/build/test/unit/tmp"
    rm -r ${dst}/*
    mkdir -p ${dst}
    cp -r ${failed_dirs} ${dst}
    cnt=`ls ${dst} | wc -l`
    echo ${cnt} directories are copied to ${dst}
fi
