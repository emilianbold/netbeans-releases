#! /bin/bash -x

#
# Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
#

base="<change me>"
module=${base}/completion
unit=${module}/test/unit

golden_dir="${unit}/data/goldenfiles/org/netbeans/modules/cnd/completion/impl/xref/ReferencesTestCase/"


log=/tmp/log

copied_golden_files=`grep "Files differ" ${log} | grep "AssertionFailedError" | grep "completion" | awk '{print $6}' `

for F in `echo $copied_golden_files`; do D=`dirname $F`; BASE=`basename $F`; REF=`ls $D/*.ref`; GOLD=`ls $D/*.golden`; echo "==================== ${BASE} ===================="; diff ${REF} ${GOLD}; done


# moving ALL golden files from the work directory to the reference directory
for F in `echo $copied_golden_files`; do D=`dirname $F`; BASE=`basename $F`; REF=`ls $D/*.ref`; GOLD=`ls $D/*.golden`; cp $REF $golden_dir ; done          
