#!/bin/bash

jars=`ls *.jar | sort`

echo ""; echo ""; echo ""; echo ""; 
echo "#IMPORTANT: we got messages per *.jar like:"
echo "#WARNING [org.netbeans.core.startup.InstalledFileLocatorImpl]: module org.netbeans.libs.clank in /opt/netbeans/cnd does not own modules/ext/org-clang-lex.jar at org.netbeans.LocaleVariants.findLogicalPath(LocaleVariants.java:271)"

echo "#release.external/*.jar used for code assistance"
for j in $jars; do 
    echo "release.external/$j=modules/ext/$j"
done

echo ""
echo "#properties below are used to provide code assistance for clank built from sputnik"
for j in $jars; do 
    echo "file.reference.$j=external/$j"
done

echo ""
echo "#properties below are used for javadoc"
for j in $jars; do 
    just_name=`echo $j |  cut -d'.' -f1`
    with_dots="${just_name//-/.}"
    echo "javadoc.reference.$j=\${sputnik}/modules/${with_dots}/src"
done

echo ""
echo "#properties below are used to go into clank sources"
for j in $jars; do 
    just_name=`echo $j |  cut -d'.' -f1`
    with_dots="${just_name//-/.}"
    echo "source.reference.$j=\${sputnik}/modules/${with_dots}/src"
done

    
