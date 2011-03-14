#!/bin/bash

if test ! -e /space/hudsonserver/master 
then
if [ -n $mobility_enabled ]
then

rm -rf $WORKSPACE/mobility
#ugly workaround
rm -f $netbeans_dest/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml

    
    ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilitySetupTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
rm -f $netbeans_dest/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml
    ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilityDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
rm -f $netbeans_dest/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml
    ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilityDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
rm -f $netbeans_dest/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml
    ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilityDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7
rm -f $netbeans_dest/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml
    ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilityActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
rm -f $netbeans_dest/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml
    ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilityActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
rm -f $netbeans_dest/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml
    ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilityActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7
rm -f $netbeans_dest/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml
cp -R build/test/qa-functional/work/ $WORKSPACE/mobility
cp -R build/test/qa-functional/results/ $WORKSPACE/mobility
rm -rf $WORKSPACE/mobility/userdir0
rm -rf $WORKSPACE/mobility/tmpdir
fi
fi
