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

    #Pack the distributions
    ant zip-cluster-config -Dcluster.config=full -Dzip.name=$DIST_DIR/zip/$NAME.zip || exit 1

    #ant zip-cluster-config -Dcluster.config=platform -Dzip.name=$DIST_DIR/zip/$NAME-platform.zip || exit 1
    ant zip-cluster-config -Dcluster.config=basic -Dzip.name=$DIST_DIR/zip/$NAME-javase.zip || exit 1
    ant zip-cluster-config -Dcluster.config=standard -Dzip.name=$DIST_DIR/zip/$NAME-java.zip || exit 1
    ant zip-cluster-config -Dcluster.config=ruby -Dzip.name=$DIST_DIR/zip/$NAME-ruby.zip || exit 1
    ant zip-cluster-config -Dcluster.config=php -Dzip.name=$DIST_DIR/zip/$NAME-php.zip || exit 1
    ant zip-cluster-config -Dcluster.config=cnd -Dzip.name=$DIST_DIR/zip/$NAME-cpp.zip || exit 1

    mkdir $DIST_DIR/zip/moduleclusters

    cd $NB_ALL/nbbuild/netbeans

    codename="org-netbeans-core-browser"
    native="extra/modules/lib/native"
    locale="extra/modules/locale"
    config="extra/config/Modules"
    update_tracking="extra/update_tracking"
    modules="extra/modules"

    pack_component $DIST_DIR/zip/moduleclusters $NAME extra-core-browser-linux       "$config/$codename-linux.xml      $update_tracking/$codename-linux.xml   $modules/$codename-linux.jar   $locale/$codename-linux_*.jar   $native/linux/xulrunner"
    pack_component $DIST_DIR/zip/moduleclusters $NAME extra-core-browser-windows     "$config/$codename-win.xml        $update_tracking/$codename-win.xml     $modules/$codename-win.jar     $locale/$codename-win_*.jar     $native/win32/xulrunner"
    pack_component $DIST_DIR/zip/moduleclusters $NAME extra-core-browser-solaris-x86 "$config/$codename-solaris.xml    $update_tracking/$codename-solaris.xml $modules/$codename-solaris.jar $locale/$codename-solaris_*.jar $native/solaris-x86/xulrunner"
    pack_component $DIST_DIR/zip/moduleclusters $NAME extra-core-browser-macosx      "$config/$codename-macosx.xml     $update_tracking/$codename-macosx.xml  $modules/$codename-macosx.jar  $locale/$codename-macosx_*.jar  $native/macosx/xulrunner $native/macosx/libcocoautils.jnilib"

    rm -rf extra

    pack_component $DIST_DIR/zip/moduleclusters $NAME soa "soa*"
    rm -rf soa*

    pack_component $DIST_DIR/zip/moduleclusters $NAME uml "uml*"
    rm -rf uml*

    pack_component $DIST_DIR/zip/moduleclusters $NAME visualweb "visualweb*"
    rm -rf visualweb*

    pack_component $DIST_DIR/zip/moduleclusters $NAME xml "xml*"
    rm -rf xml*

    pack_component $DIST_DIR/zip/moduleclusters $NAME javacard "javacard*"
    rm -rf javacard*

    cd $NB_ALL/nbbuild

    #Pack all the NetBeans with javafx
    pack_component $DIST_DIR/zip/moduleclusters $NAME all-in-one-w-javafx netbeans

    cd $NB_ALL/nbbuild/netbeans
    pack_component $DIST_DIR/zip/moduleclusters $NAME javacard "javafx*"
    rm -rf javafx*

    cd $NB_ALL/nbbuild

    #Pack all the NetBeans
    pack_component $DIST_DIR/zip/moduleclusters $NAME all-in-one netbeans

    cd $NB_ALL/nbbuild/netbeans

    #Continue with individual component
    pack_component $DIST_DIR/zip/moduleclusters $NAME dlight "dlight*"
    rm -rf dlight*

    pack_component $DIST_DIR/zip/moduleclusters $NAME webcommon "webcommon*"
    rm -rf webcommon*

    pack_component $DIST_DIR/zip/moduleclusters $NAME groovy "groovy*"
    rm -rf groovy*

    pack_component $DIST_DIR/zip/moduleclusters $NAME php "php*"
    rm -rf php*

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

    pack_component $DIST_DIR/zip/moduleclusters $NAME harness "harness*"
    rm -rf harness*

    pack_component $DIST_DIR/zip/moduleclusters $NAME enterprise "enterprise*"
    rm -rf enterprise*

    pack_component $DIST_DIR/zip/moduleclusters $NAME ergonomics "ergonomics*"
    rm -rf ergonomics*

    pack_component $DIST_DIR/zip/moduleclusters $NAME soa "soa*"
    rm -rf soa*

    pack_component $DIST_DIR/zip/moduleclusters $NAME apisupport "apisupport*"
    rm -rf apisupport*

    pack_component $DIST_DIR/zip/moduleclusters $NAME java "java*"
    rm -rf java*

    pack_component $DIST_DIR/zip/moduleclusters $NAME cnd "cnd*"
    rm -rf cnd*

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
