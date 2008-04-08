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
    zip -q -r $dist/$base_name-$component.zip $filter
#    gtar cvzf $dist/targz/$base_name-$component.tar.gz $filter
#    gtar cvjf $dist/tarbz2/$base_name-$component.tar.bz2 $filter
}

###################################################################
#
# Pack all the components
#
###################################################################

pack_all_components()
{
    DIST_DIR=${1}
    NAME=${2}
    cd $NB_ALL/nbbuild

    #Pack the distrubutions
    find netbeans | egrep -v "netbeans/(extra|testtools|php)" | zip -q $DIST_DIR/zip/$NAME.zip -@ || exit 1

    #find netbeans | egrep "netbeans/(platform|harness)" | zip -q $DIST_DIR/zip/$NAME-platform.zip -@ || exit 1
    find netbeans | egrep -v "netbeans/(extra|testtools|xml|mobility|enterprise|visualweb|uml|ruby|soa|cnd|identity|php)" | zip -q $DIST_DIR/zip/$NAME-javase.zip -@ || exit 1
    find netbeans | egrep -v "netbeans/(extra|testtools|xml|enterprise|visualweb|uml|ruby|soa|cnd|identity|php)" | egrep -v "(org-netbeans-modules-mobility-end2end|org-netbeans-modules-mobility-jsr172)" | zip -q $DIST_DIR/zip/$NAME-mobility.zip -@ || exit 1
    find netbeans | egrep -v "netbeans/(extra|testtools|mobility|uml|ruby|soa|cnd|identity|php)" | zip -q $DIST_DIR/zip/$NAME-javaee.zip -@ || exit 1
    find netbeans | egrep -v "netbeans/(extra|testtools|xml|java|apisupport|harness|profiler|mobility|enterprise|visualweb|uml|soa|identity|cnd|php)" | zip -q $DIST_DIR/zip/$NAME-ruby.zip -@ || exit 1
    find netbeans | egrep -v "netbeans/(extra|testtools|xml|java|apisupport|harness|profiler|mobility|enterprise|visualweb|uml|soa|identity|cnd|ruby)" | zip -q $DIST_DIR/zip/$NAME-php.zip -@ || exit 1
    find netbeans | egrep -v "netbeans/(extra|testtools|xml|java|apisupport|harness|profiler|mobility|enterprise|visualweb|uml|ruby|soa|identity|php)" | zip -q $DIST_DIR/zip/$NAME-cpp.zip -@ || exit 1

    mkdir $DIST_DIR/zip/moduleclusters

    rm -rf $NB_ALL/nbbuild/netbeans/extra

    cd $NB_ALL/nbbuild/netbeans

    #Pack PHP first, it can't be in all-in-one zip
    pack_component $DIST_DIR/zip/moduleclusters $NAME php "php*"
    rm -rf php*

    cd $NB_ALL/nbbuild

    #Pack all the NetBeans
    pack_component $DIST_DIR/zip/moduleclusters $NAME all-in-one netbeans

    cd $NB_ALL/nbbuild/netbeans

    #Continue with individual component
    pack_component $DIST_DIR/zip/moduleclusters $NAME uml "uml*"
    rm -rf uml*

    pack_component $DIST_DIR/zip/moduleclusters $NAME visualweb "visualweb*"
    rm -rf visualweb*

    pack_component $DIST_DIR/zip/moduleclusters $NAME ruby "ruby*"
    rm -rf ruby*

    pack_component $DIST_DIR/zip/moduleclusters $NAME profiler "profiler*"
    rm -rf profiler*

    pack_component $DIST_DIR/zip/moduleclusters $NAME platform "platform*"
    rm -rf platform*

    pack_component $DIST_DIR/zip/moduleclusters $NAME mobility "mobility*"
    rm -rf mobility*

    pack_component $DIST_DIR/zip/moduleclusters $NAME identity "identity*"
    rm -rf identity*

    pack_component $DIST_DIR/zip/moduleclusters $NAME ide "ide*"
    rm -rf ide*

    pack_component $DIST_DIR/zip/moduleclusters $NAME xml "xml*"
    rm -rf xml*

    pack_component $DIST_DIR/zip/moduleclusters $NAME harness "harness*"
    rm -rf harness*

    pack_component $DIST_DIR/zip/moduleclusters $NAME enterprise "enterprise*"
    rm -rf enterprise*

    pack_component $DIST_DIR/zip/moduleclusters $NAME soa "soa*"
    rm -rf soa*

    pack_component $DIST_DIR/zip/moduleclusters $NAME apisupport "apisupport*"
    rm -rf apisupport*

    pack_component $DIST_DIR/zip/moduleclusters $NAME java "java*"
    rm -rf java*

    pack_component $DIST_DIR/zip/moduleclusters $NAME cnd "cnd*"
    rm -rf cnd*

    pack_component $DIST_DIR/zip/moduleclusters $NAME gsf "gsf*"
    rm -rf gsf*

    pack_component $DIST_DIR/zip/moduleclusters $NAME nb6.0-etc "*"
}

pack_all_components $DIST $BASENAME

if [ $ML_BUILD == 1 ]; then
    cd $NB_ALL
    rm -rf $NB_ALL/nbbuild/netbeans
    mv $NB_ALL/nbbuild/netbeans-ml $NB_ALL/nbbuild/netbeans

    mkdir -p $DIST/ml/zip
    pack_all_components $DIST/ml $BASENAME-ml
fi
