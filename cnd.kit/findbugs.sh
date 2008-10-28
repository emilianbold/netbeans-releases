#!/bin/sh -x

fb_home="$1"
workspace="$2"
out="$3"

#${fb_home}="/Users/vk155633/opensource/4hudson/findbugs-1.3.6-rc2"
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
