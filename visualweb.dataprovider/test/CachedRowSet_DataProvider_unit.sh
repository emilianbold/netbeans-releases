#@echo off
#rmdir /S /Q ..\build
#rmdir /S /Q lib
#rmdir /S /Q work
#rmdir /S /Q results
export BASE=g:/home/tr61
echo $BASE

#  ant -Dnetbeans.dest.dir="/sun/NB-6.0" -Dxtest.file_appserver.properties=/sun/as.properties -Dxtest.file_database.properties=/sun/db_derby.properties -Dxtest.testtype="unit" -Dxtest.testbags="CachedRowSetDataProviderTest" -Dxtest.testattribs="stable" -Dnetbeans.javacore.noscan=true

#ant -Dnetbeans.dest.dir="$BASE/nbbuild/netbeans" -Dxtest.file_appserver.properties=$BASE/visualweb/dataprovider/runtime/test/unit/as.properties -Dxtest.file_database.properties=$BASE/visualweb/dataprovider/runtime/test/unit/db.properties -Dxtest.testtype="unit" -Dxtest.testbags="CachedRowSetDataProviderTest" -Dxtest.testattribs="stable" -Dnetbeans.javacore.noscan=true

ant -Dnetbeans.dest.dir="$BASE/nbbuild/netbeans" -Dxtest.file_appserver.properties=$BASE/visualweb/dataprovider/runtime/test/unit/as.properties -Dxtest.file_database.properties=$BASE/visualweb/dataprovider/runtime/test/unit/db_derby.properties -Dxtest.testtype="unit" -Dxtest.testattribs="stable" -Dnetbeans.javacore.noscan=true
