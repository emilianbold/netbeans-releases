#!/bin/bash

if test ! -e /space/hudsonserver/master 
then

if [ -n $j2ee_enabled ]
then

cd $performance/j2ee
rm -rf $WORKSPACE/j2ee
ant clean -Dnetbeans.dest.dir=$netbeans_dest
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EESetupTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 
cp -R build/test/qa-functional/work/ $WORKSPACE/j2ee
cp -R build/test/qa-functional/results/ $WORKSPACE/j2ee
rm -rf $WORKSPACE/j2ee/userdir0
rm -rf $WORKSPACE/j2ee/tmpdir

fi
fi
