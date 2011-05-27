#!bash -x
env
#sanitize any orphaned JUnitTestRunners
jps | grep JUnitTestRunner | cut -d' ' -f1 | xargs kill -9

export ANT_OPTS=-Xmx1024m
export j2se_enabled=1
export j2ee_enabled=1
export languages_enabled=1
export web_enabled=1

case $OSTYPE in
    msdos*)
#  linux*|Linux*|cygwin*)
    export mobility_enabled=1
esac

cd "$WORKSPACE"/../../../../../
reposdir=`pwd`
export reposdir=`cygpath -m $reposdir`

project_root=$reposdir/ergonomics
export project_root=`cygpath -m $project_root`

netbeans_dest=$reposdir/netbeans
export netbeans_dest=`cygpath -m $netbeans_dest`

platdefharness=$netbeans_dest/harness
export platdefharness=`cygpath -m $platdefharness`

performance=$project_root/performance
export performance=`cygpath -m $performance`

cp -f $performance/hudson/netbeans.conf $netbeans_dest/etc/

# fix the permissions; they get reset after each hg pull ...
chmod a+x $performance/hudson/*.sh

$performance/hudson/setupenv.sh
$performance/hudson/j2se.sh
$performance/hudson/j2ee.sh
$performance/hudson/web.sh
$performance/hudson/languages.sh
$performance/hudson/mobility.sh