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
    find netbeans | egrep -v "netbeans/(extra|testtools|uml|maven)" | zip -q $DIST_DIR/zip/$NAME.zip -@ || exit 1

    #find netbeans | egrep "netbeans/(platform|harness)" | zip -q $DIST_DIR/zip/$NAME-platform.zip -@ || exit 1
    find netbeans | egrep -v "netbeans/(extra|testtools|xml|mobility|enterprise|visualweb|uml|ruby|soa|cnd|identity|php|groovy|webcommon|maven)" | zip -q $DIST_DIR/zip/$NAME-javase.zip -@ || exit 1
    find netbeans | egrep -v "netbeans/(extra|testtools|uml|ruby|soa|cnd|identity|php|maven)" | zip -q $DIST_DIR/zip/$NAME-java.zip -@ || exit 1
    find netbeans | egrep -v "netbeans/(extra|testtools|xml|java|apisupport|harness|profiler|mobility|enterprise|visualweb|uml|soa|identity|cnd|php|groovy|maven)" | zip -q $DIST_DIR/zip/$NAME-ruby.zip -@ || exit 1
    find netbeans | egrep -v "netbeans/(extra|testtools|xml|java|apisupport|harness|profiler|mobility|enterprise|visualweb|uml|soa|identity|cnd|ruby|groovy|maven)" | zip -q $DIST_DIR/zip/$NAME-php.zip -@ || exit 1
    find netbeans | egrep -v "netbeans/(extra|testtools|xml|java|apisupport|harness|profiler|mobility|enterprise|visualweb|uml|ruby|soa|identity|php|groovy|webcommon|maven)" | zip -q $DIST_DIR/zip/$NAME-cpp.zip -@ || exit 1

    mkdir $DIST_DIR/zip/moduleclusters

    rm -rf $NB_ALL/nbbuild/netbeans/extra

    cd $NB_ALL/nbbuild/netbeans
    pack_component $DIST_DIR/zip/moduleclusters $NAME uml "uml*"
    rm -rf uml*

    cd $NB_ALL/nbbuild/netbeans
    pack_component $DIST_DIR/zip/moduleclusters $NAME maven "maven*"
    rm -rf maven*

    cd $NB_ALL/nbbuild

    #Pack all the NetBeans
    pack_component $DIST_DIR/zip/moduleclusters $NAME all-in-one netbeans

    cd $NB_ALL/nbbuild/netbeans

    #Continue with individual component
    pack_component $DIST_DIR/zip/moduleclusters $NAME webcommon "webcommon*"
    rm -rf webcommon*

    pack_component $DIST_DIR/zip/moduleclusters $NAME groovy "groovy*"
    rm -rf groovy*

    pack_component $DIST_DIR/zip/moduleclusters $NAME php "php*"
    rm -rf php*

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
