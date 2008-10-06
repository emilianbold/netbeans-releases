#!/bin/sh -x
#
# For all tests that failed,
# copy goldens and data into build/test/unit/work/tmp
#

modelimpl="${MODELIMPL-.}"

failed_dirs=`find ${modelimpl}/build/test/unit/work -name "*.golden" -exec dirname {} \; | sort -u`

if [ -z "${failed_dirs}" ]; then
    echo "No failed tests found"
else
    dst="${modelimpl}/build/test/unit/tmp"
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
fi
