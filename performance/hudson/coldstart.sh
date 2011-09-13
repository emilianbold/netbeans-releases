#!bash -x
env
#sanitize any orphaned JUnitTestRunners
jps | grep JUnitTestRunner | cut -d' ' -f1 | xargs kill -9

#environment setup
cd "$WORKSPACE"/../../../../../
reposdir=`pwd`
export reposdir=`cygpath -m $reposdir`
project_root=$reposdir/ergonomics
export project_root=`cygpath -m $project_root`
netbeans_dest=$reposdir/netbeans
export netbeans_dest=`cygpath -m $netbeans_dest`
# copy netbeans.conf to netbeans dir
cp -f $performance/hudson/netbeans.conf $netbeans_dest/etc/
# delete all netbeans userdirs
rm -rf $HOME/.netbeans
# start netbeans first time
/home/hudson/scripts/nb_start.bat
# start nb second time + open project
/home/hudson/scripts/nb_open_pr.bat
# start netbeans third time
/home/hudson/scripts/nb_start.bat
#start script dealing with post-startup run
/home/hudson/scripts/preparecold.bat
# restart 
/home/hudson/scripts/restart.bat