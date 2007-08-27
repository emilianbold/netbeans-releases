
base=/net/endif/export/home1/deimos/dev/spb/vk155633/netbeans.src/trunk/cnd/
module=${base}/modelimpl
unit=${module}/test/unit

golden_dir="${unit}/data/goldenfiles/org/netbeans/modules/cnd/modelimpl/trace/FileModelTest"


log=/tmp/log
new_golden_files=`grep "Difference between" ${log} | grep "AssertionFailedError" |  awk '{print $5}' `

# total diff
for F in ${new_golden_files}; do BASE=`basename $F`; GOLD=${golden_dir}/${BASE}; echo "==================== ${BASE} ===================="; diff $F ${GOLD}; done 

# moving ALL golden files from the work directory to the reference directory
mv ${new_golden_files} ${golden_dir}
