#!bash -x
echo PATH=$PATH
export PATH="/bin:"$PATH
echo PATH=$PATH

#if test ! -e /space/hudsonserver/master 
case $OSTYPE in
msdos)
ps -efW| egrep -i "junit|netbeans|test|anagram|ant|jusched"|egrep -vi system32|awk '{print $2}'| xargs /bin/kill -f;
sleep 5;
;;
windows)
ps -efW| egrep -i "junit|netbeans|test|anagram|ant|jusched"|egrep -vi system32|awk '{print $2}'| xargs /bin/kill -f;
sleep 5;
;;
cygwin)
ps -efW| egrep -i "junit|netbeans|test|anagram|ant|jusched"|egrep -vi system32|awk '{print $2}'| xargs /bin/kill -f;
sleep 5;
;;
linux-gnu)
ps -e| egrep -i "junit|netbeans|test|anagram|ant|jusched"|egrep -vi system32|awk '{print $2}'| xargs /bin/kill -9;
sleep 5;
export nb_perf_alt_path="/space/slowfs/ubuntu";
;;
linux)
ps -e| egrep -i "junit|netbeans|test|anagram|ant|jusched"|egrep -vi system32|awk '{print $2}'| xargs /bin/kill -9;
sleep 5;
export nb_perf_alt_path="/space/slowfs/ubuntu";
;;
*)
jps| egrep -i "junit|netbeans|test|anagram|ant|jusched"|egrep -vi system32| xargs kill;
;;
esac
#fi
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

project_root=$reposdir/../ergonomics
export project_root=`cygpath -m $project_root`

netbeans_dest=$reposdir/netbeans
export netbeans_dest=`cygpath -m $netbeans_dest`

platdefharness=$netbeans_dest/harness
export platdefharness=`cygpath -m $platdefharness`
export nbplatform.default.harness.dir=$platdefharness

performance=$project_root/performance
export performance=`cygpath -m $performance`

perfjar=$netbeans_dest/extra/modules/org-netbeans-modules-performance.jar
export perfjar=`cygpath -m $perfjar`

execdir=$netbeans_dest/bin/
export execdir=`cygpath -m $execdir`

cp $performance/hudson/netbeans.conf $netbeans_dest/etc/

# fix the permissions; they get reset after each hg pull ...
chmod a+x $performance/hudson/*.sh
chmod a+x $netbeans_dest/bin/*

cd $project_root
rm -rf nbbuild/nbproject/private
rm -rf performance/build
rm -rf performance/*/build

pwd

$performance/hudson/setupenv.sh
$performance/hudson/j2se.sh
$performance/hudson/j2ee.sh
$performance/hudson/web.sh
$performance/hudson/languages.sh
