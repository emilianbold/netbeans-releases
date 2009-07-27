#!/bin/sh -x

fb_home="$1"
workspace="$2"
out="$3"

prj="/tmp/dlight.fbp"
echo "<Project filename=\"DLight\" projectName=\"DLight\">" > ${prj}
for D in `ls -d ${workspace}/dlight*`; do
	if [ -d $D/build/classes ]; then
		echo "    <Jar>$D/build/classes</Jar>" >> ${prj}
		echo "    <AuxClasspathEntry>$D/src</AuxClasspathEntry>" >> ${prj}
	fi
done
echo "<SuppressionFilter>"  >> ${prj}
echo "    <LastVersion value=\"-1\" relOp=\"NEQ\"/>"  >> ${prj}
echo "</SuppressionFilter>"  >> ${prj}
echo "</Project>" >> ${prj}

${fb_home}/bin/findbugs -maxHeap 1536 -textui -project ${prj} -xml -output ${out}
