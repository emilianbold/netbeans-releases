#!/bin/bash

if test ! -e /space/hudsonserver/master 
then

if [ -n "$j2se_enabled" ] 
then

cd $performance/j2se

rm -rf $WORKSPACE/j2se
ant clean -Dnetbeans.dest.dir=$netbeans_dest
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2SESetupTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2SEMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2SEMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2SEMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2SEDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2SEDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2SEDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2SEActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2SEActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2SEActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 
cp -R build/test/qa-functional/work/ $WORKSPACE/j2se
cp -R build/test/qa-functional/results/ $WORKSPACE/j2se
rm -rf $WORKSPACE/j2se/userdir0
rm -rf $WORKSPACE/j2se/tmpdir

fi
fi
