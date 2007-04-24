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
cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -D "$CVS_STAMP" nbbuild > $CVS_CHECKOUT_LOG 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Checkout of nbbuild module failed"
    exit $ERROR_CODE;
fi

#Get the list of modules for the update/checkout
CVS_MODULES=`ant -f nbbuild/build.xml print-cvs-modules | grep "cvsmodules=" | cut -f 2 -d "=" | tr "[" " " | tr "]" " " | tr "," " "` 
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Error getting the list of modules"
    exit $ERROR_CODE;
fi

CVS_MODULES=`echo ${CVS_MODULES} mobility uml visualweb scripting enterprise print identity`

for module in ${CVS_MODULES}; do
    if [ -d $module ]; then
	#Module already checked out - updating
	cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs update -dPA -D "$CVS_STAMP" $module >> $CVS_CHECKOUT_LOG 2>&1
	if [ $ERROR_CODE != 0 ]; then
	    echo "ERROR: $ERROR_CODE - Update of ${module} module failed"
	    exit $ERROR_CODE;
	fi
    else
	#Need to checkout
	cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -PA -D "$CVS_STAMP" $module >> $CVS_CHECKOUT_LOG 2>&1
	if [ $ERROR_CODE != 0 ]; then
	    echo "ERROR: $ERROR_CODE - Checkout of ${module} module failed"
	    exit $ERROR_CODE;
	fi
    fi
done
