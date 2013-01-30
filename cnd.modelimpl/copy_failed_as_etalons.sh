#!/bin/sh -x

#
# Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
#

#
# For all tests that failed,
# copy goldens and data into build/test/unit/work/tmp2
#

cur=`pwd`
modelimpl="${MODELIMPL-${cur}}"
modelimpl_golden="${modelimpl}/test/unit/data/goldenfiles"

failed_dirs=`find ${modelimpl}/build/test/unit/work -name "*.golden" -exec dirname {} \; | sort -u`
failed_files=`find ${modelimpl}/build/test/unit/work -name "*.golden" | sort -u`

if [ -z "${failed_files}" ]; then
    echo "No failed tests found"
else
    dst="${modelimpl}/build/test/unit/etalons"
    rm -r ${dst}/* > /dev/null
    mkdir -p ${dst}
    cp -r ${failed_dirs} ${dst}

    # sed s/\.golden//g
    for d in `ls ${dst}`; do
	cd ${dst}/$d
	for gold in `ls *.golden`; do
	    orig=`echo ${gold} | sed s/\.golden//g`
	    #ls -l ${gold}
	    #ls -l ${orig}
	    echo "==================== ${gold} vs ${orig} ===================="
	    diff ${gold} ${orig}
	done
	cd - > /dev/null
    done

    cnt=`ls ${dst} | wc -l`
    echo ${cnt} directories are copied to ${dst}

    for d in `ls ${dst}`; do
	cd ${dst}/$d
	for gold in `ls *.golden`; do
            failed_name=`echo ${gold} | sed s/\.golden//g`
            orig=`find ${modelimpl_golden} -name ${failed_name}`
            if [ -z "${orig}" ]; then
                echo "ERROR: pair for ${gold} is not found"
            else 
                echo copy ${failed_name} ${orig}
                cp ${failed_name} ${orig}
            fi
	done
	cd - > /dev/null
    done
fi
