set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh


updates1=`cat $CVS_CHECKOUT_LOG | grep "^U "` #New Files
updates2=`cat $CVS_CHECKOUT_LOG | grep "^P "` #Modifications
updates3=`cat $CVS_CHECKOUT_LOG | grep " is no longer in the repository$"` #Removed files

update_count=`echo ${updates1} ${updates2} ${updates3} | wc -l | tr " " "\n" | grep -v '^$'`

if [ ${update_count} -lt 1 ]; then
    #No update
    exit 2; #This means no update! This is not an error
fi
