set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

mkdir -p $NB_ALL
cd  $NB_ALL

Clean the leftovers from the last build.
For more info about "cvspurge" take a look 
at http://www.red-bean.com/cvsutils/
for i in `ls | grep -v "CVS"`; do
    cvspurge $i;
    ERROR_CODE=$?

    if [ $ERROR_CODE != 0 ]; then
	echo "ERROR: Purge of $module failed - removing it"
	rm -rf $module;
    fi
done
