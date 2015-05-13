#!/bin/sh -x

#
# Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
#

fb_home="$1"
workspace="$2"
out="$3"

#don't want to clean everything - for example, jars are needed...
rm ${workspace}/cnd.apt/src/org/netbeans/modules/cnd/apt/impl/support/generated/* 2>/dev/null
rm ${workspace}/cnd.apt/build/classes/org/netbeans/modules/cnd/apt/impl/support/generated/* 2>/dev/null
rm ${workspace}/cnd.modelimpl/src/org/netbeans/modules/cnd/modelimpl/parser/generated/* 2>/dev/null
rm ${workspace}/cnd.modelimpl/build/classes/org/netbeans/modules/cnd/modelimpl/parser/generated/* 2>/dev/null
rm ${workspace}/cnd.modelimpl/build/classes/org/netbeans/modules/cnd/modelimpl/parser/FortranLexicalPrepass* 2>/dev/null

PLIST=`ls -d ${workspace}/cnd*/build/classes ${workspace}/remotefs*/build/classes ${workspace}/dlight*/build/classes ${workspace}/git.remote*/build/classes ${workspace}/subversion.remote/build/classes ${workspace}/mercurial.remote/build/classes ${workspace}/lib.terminalemulator/build/classes ${workspace}/terminal/build/classes | egrep -v "/cnd.antlr/|/cnd.debugger.common/|/cnd.debugger.gdb/|/cnd.debugger.dbx/"`
PR=""
for d in ${PLIST}; do
   s=`echo $d | sed 's/build\/classes/src/'`
   PR="${PR}
      <Jar>$d</Jar>
      <AuxClasspathEntry>$s</AuxClasspathEntry>"
done

prj="/tmp/cnd.fbp"
cat << EOF > ${prj}
<Project filename="CND" projectName="CND">
${PR}
  <SuppressionFilter>
    <LastVersion value="-1" relOp="NEQ"/>
  </SuppressionFilter>
</Project>
EOF

${fb_home}/bin/findbugs -maxHeap 1536 -textui -project ${prj} -xml -effort:max -low -output ${out}
