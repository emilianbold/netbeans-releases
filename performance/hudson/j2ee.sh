#!/bin/bash -x

if test ! -e /space/hudsonserver/master 
then

if [ -n "$j2ee_enabled" ]
then

cd $performance/j2ee
rm -rf "$WORKSPACE"/j2ee
ant clean -Dnetbeans.dest.dir=$netbeans_dest
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureJ2EEActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true

touch "$performance"/j2ee/build/test/qa-functional/work/userdir0
touch "$performance"/j2ee/build/test/qa-functional/work/tmpdir
rm -rf "$performance"/j2ee/build/test/qa-functional/work/userdir0
rm -rf "$performance"/j2ee/build/test/qa-functional/work/tmpdir
cp -R build/test/qa-functional/work/ "$WORKSPACE"/j2ee
cp -R build/test/qa-functional/results/ "$WORKSPACE"/j2ee

fi
fi
