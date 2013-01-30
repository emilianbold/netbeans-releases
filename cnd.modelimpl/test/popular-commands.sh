#! /bin/bash -x

#
# Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
#

base="<change-me>"
module=${base}/modelimpl
unit=${module}/test/unit

golden_dir="${unit}/data/goldenfiles/org/netbeans/modules/cnd/modelimpl/trace/FileModelTest"


log=/tmp/log
new_golden_files=`grep "OUTPUT Difference" ${log} | grep "AssertionFailedError" |  awk '{print $8}' `

# total diff
for F in ${new_golden_files}; do BASE=`basename $F`; GOLD=${golden_dir}/${BASE}; echo "==================== ${BASE} ===================="; diff $F ${GOLD}; done 

echo ${golden_dir}
# moving ALL golden files from the work directory to the reference directory
cp -f ${new_golden_files} ${golden_dir}
