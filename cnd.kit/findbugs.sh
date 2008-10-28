#!/bin/sh -x

fb_home="$1"
workspace="$2"
out="$3"

#don't want to clean everything - for example, jars are needed...
rm ${workspace}/cnd.apt/src/org/netbeans/modules/cnd/apt/impl/support/generated/*
rm ${workspace}/cnd.apt/build/classes/org/netbeans/modules/cnd/apt/impl/support/generated/*
rm ${workspace}/cnd.modelimpl/src/org/netbeans/modules/cnd/modelimpl/parser/generated/*
rm ${workspace}/cnd.modelimpl/build/classes/org/netbeans/modules/cnd/modelimpl/parser/generated/*

prj="/tmp/cnd.fbp"
echo "<Project filename=\"CND\" projectName=\"CND\">" > ${prj}
for D in `ls -d ${workspace}/cnd* | grep -v cnd.antlr`; do 
	echo "    <Jar>$D/build/classes</Jar>" >> ${prj}
	echo "    <AuxClasspathEntry>$D/src</AuxClasspathEntry>" >> ${prj}
done
echo "<SuppressionFilter>"  >> ${prj}
echo "    <LastVersion value=\"-1\" relOp=\"NEQ\"/>"  >> ${prj}
echo "</SuppressionFilter>"  >> ${prj}
echo "</Project>" >> ${prj}

${fb_home}/bin/findbugs -maxHeap 1536 -textui -project ${prj} -xml -output ${out}
