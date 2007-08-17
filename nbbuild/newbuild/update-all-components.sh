set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

mkdir -p $NB_ALL
cd  $NB_ALL

###################################################################
#
# Checkout/Update all the required NB modules
#
###################################################################

#nbbuild module is required for the list of modules
cvs -z6 -q -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -D "$CVS_STAMP" nbbuild > $CVS_CHECKOUT_LOG 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    tail -100 $CVS_CHECKOUT_LOG
    echo "ERROR: $ERROR_CODE - Checkout of nbbuild module failed"
    exit $ERROR_CODE;
fi

#Get the list of modules for the update/checkout
CVS_MODULES=`ant -f nbbuild/build.xml print-cvs-modules -Dcluster.config=stableuc | grep "cvsmodules=" | cut -f 2 -d "=" | tr "[" " " | tr "]" " " | tr "," " "` 
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Error getting the list of modules"
    exit $ERROR_CODE;
fi

CVS_MODULES=`echo ${CVS_MODULES} testtools jemmy jellytools xtest` # | tr " " "\n" | grep -v '^$' | sort | uniq | tr "\n" " "

set +x

for module in ${CVS_MODULES}; do
    #Need to improve the errors checking...
    RUNNING_JOBS_COUNT=`jobs | wc -l | tr " " "\n" | grep -v '^$'`
    #Control the number of running updates
    while [ $RUNNING_JOBS_COUNT -ge 10 ]; do
	#10 or more jobs
	sleep 10
	jobs > /dev/null 
	RUNNING_JOBS_COUNT=`jobs | wc -l | tr " " "\n" | grep -v '^$'`
    done

    if [ -d $module ]; then
	#Module already checked out - updating
	cvs -z6 -q -d :pserver:anoncvs@cvs.netbeans.org:/cvs update -dPA -D "$CVS_STAMP" $module &
    else
	#Need to checkout
	cvs -z6 -q  -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -PA -D "$CVS_STAMP" $module &
    fi
done

RUNNING_JOBS_COUNT=`jobs | wc -l | tr " " "\n" | grep -v '^$'`
#Wait for the end of all cvs processes to end
while [ $RUNNING_JOBS_COUNT -ge 1 ]; do
    #1 or more jobs
    sleep 10
    jobs > /dev/null
    RUNNING_JOBS_COUNT=`jobs | wc -l | tr " " "\n" | grep -v '^$'`
done
set -x
