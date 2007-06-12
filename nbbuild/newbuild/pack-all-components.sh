set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

pack_component() 
{
    dist=$1
    base_name=$2
    component=$3
    filter=$4
    zip -q -r $dist/zip/$base_name-$component.zip $filter
#    gtar cvzf $dist/targz/$base_name-$component.tar.gz $filter
#    gtar cvjf $dist/tarbz2/$base_name-$component.tar.bz2 $filter
}

###################################################################
#
# Pack all the components
#
###################################################################

cd $NB_ALL/nbbuild

#Pack all the NetBeans
pack_component $DIST $BASENAME all-in-one netbeans

cd $NB_ALL/nbbuild/netbeans

#Continue with individual component
pack_component $DIST $BASENAME uml "uml*"
rm -rf uml*

pack_component $DIST $BASENAME visualweb "visualweb*"
rm -rf visualweb*

pack_component $DIST $BASENAME ruby "ruby*"
rm -rf ruby*

pack_component $DIST $BASENAME profiler "profiler*"
rm -rf profiler*

pack_component $DIST $BASENAME platform "platform*"
rm -rf platform*

pack_component $DIST $BASENAME mobility "mobility*"
rm -rf mobility*

pack_component $DIST $BASENAME ide "ide*"
rm -rf ide*

pack_component $DIST $BASENAME xml "xml*"
rm -rf xml*

pack_component $DIST $BASENAME harness "harness*"
rm -rf harness*

pack_component $DIST $BASENAME enterprise "enterprise*"
rm -rf enterprise*

pack_component $DIST $BASENAME soa "soa*"
rm -rf soa*

pack_component $DIST $BASENAME apisupport "apisupport*"
rm -rf apisupport*

pack_component $DIST $BASENAME java "java*"
rm -rf java*

pack_component $DIST $BASENAME cnd "cnd*"
rm -rf cnd*

pack_component $DIST $BASENAME nb6.0-etc "*"
