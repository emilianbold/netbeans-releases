#!/bin/bash
if test ! -e /space/hudsonserver/master 
then
if [ -n $web_enabled ]
then

cd $performance/web
rm -rf $WORKSPACE/web
ant clean -Dnetbeans.dest.dir=$netbeans_dest  
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebSetupTest* -Dnetbeans.dest.dir=$netbeans_dest  -Drepeat=1
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Drepeat=1
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Drepeat=7
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Drepeat=7
cp -R build/test/qa-functional/work/ $WORKSPACE/web
cp -R build/test/qa-functional/results/ $WORKSPACE/web
rm -rf $WORKSPACE/web/userdir0
rm -rf $WORKSPACE/web/tmpdir

fi
fi
