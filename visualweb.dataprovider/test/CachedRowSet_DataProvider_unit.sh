#@echo off
#rmdir /S /Q ..\build
#rmdir /S /Q lib
#rmdir /S /Q work
#rmdir /S /Q results
#BASE is the root of the NetBeans repository
export BASE=../../../../
echo $BASE

# uncomment when running CachedRowSetTest also - after including test in cfg-unit.xml 
ant -Dnetbeans.dest.dir="$BASE/nbbuild/netbeans" -Dxtest.file_database.properties=$BASE/visualweb/test/data/DefaultDatabase.properties -Dxtest.testtype="unit" -Dxtest.testattribs="stable" -Dnetbeans.javacore.noscan=true
#ant check-properties -Dnetbeans.dest.dir="$BASE/nbbuild/netbeans"  -Dxtest.testtype="unit" -Dxtest.testattribs="stable" -Dnetbeans.javacore.noscan=true
#ant  -Dnetbeans.dest.dir="$BASE/nbbuild/netbeans"  -Dxtest.testtype="unit" -Dxtest.testattribs="stable" -Dnetbeans.javacore.noscan=true



