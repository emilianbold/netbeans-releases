<?xml version="1.0" encoding="UTF-8"?>
<!--
  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

  Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.

  The contents of this file are subject to the terms of either the GNU
  General Public License Version 2 only ("GPL") or the Common
  Development and Distribution License("CDDL") (collectively, the
  "License"). You may not use this file except in compliance with the
  License. You can obtain a copy of the License at
  http://www.netbeans.org/cddl-gplv2.html
  or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  specific language governing permissions and limitations under the
  License.  When distributing the software, include this License Header
  Notice in each file and include the License file at
  nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  particular file as subject to the "Classpath" exception as provided
  by Sun in the GPL Version 2 section of the License file that
  accompanied this code. If applicable, add the following below the
  License Header, with the fields enclosed by brackets [] replaced by
  your own identifying information:
  "Portions Copyrighted [year] [name of copyright owner]"

  Contributor(s):

  The Original Software is NetBeans. The Initial Developer of the Original
  Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  Microsystems, Inc. All Rights Reserved.

  If you wish your version of this file to be governed by only the CDDL
  or only the GPL Version 2, indicate your decision by adding
  "[Contributor] elects to include this software in this distribution
  under the [CDDL or GPL Version 2] license." If you do not indicate a
  single choice of license, a recipient has the option to distribute
  your version of this file under either the CDDL, the GPL Version 2 or
  to extend the choice of license to its licensees as provided above.
  However, if you add GPL Version 2 code and therefore, elected the GPL
  Version 2 license, then the option applies only if the new code is
  made subject to such option by the copyright holder.
-->
<project name="properties" default="none" basedir=".">
    <property environment="env"/>

    <!-- dev -->
    <property name="netbeans.host" value="http://deadlock.netbeans.org"/>
    <property name="netbeans.path" value="hudson/job/trunk/lastSuccessfulBuild/artifact/nbbuild/dist/zip"/>
    <property name="netbeans.dir" value=".netbeans/dev"/>

    <property name="env.NB_USER" value="anoncvs"/>
    <property name="sierra.dir"  value=".netbeans/sierra"/>

    <property name="modules.cvs.root" value=":pserver:${env.NB_USER}@cvs.netbeans.org:/cvs"/>
    <property name="modules.cvs.branch" value="-A"/>
    <property name="modules.cvs.modules" value="
      print
      enterprise/bpel
      enterprise/compapp
      enterprise/dataintegrator/eTLEditor
      enterprise/dataintegrator/etlpro
      enterprise/iep
      enterprise/libs
      enterprise/openesbaddons/configextension
      enterprise/openesbaddons/ftp
      enterprise/openesbaddons/snmp
      enterprise/soa
      enterprise/sql
      enterprise/wsdlextensions
      enterprise/xslt
    "/>
    <property name="sources.cvs.root" value=":pserver:${env.NB_USER}@cvs.netbeans.org:/cvs"/>
    <property name="sources.cvs.branch" value="-A"/>
    <property name="sources.cvs.modules" value="
        ant/nbproject
        ant/project/nbproject
        apisupport/harness/external
        classfile/nbproject
        core/bootstrap/nbproject
        core/javahelp/external
        core/javahelp/nbproject
        core/multiview/nbproject
        core/navigator/nbproject
        core/nbproject
        core/options/keymap/nbproject
        core/options/nbproject
        core/palette/nbproject
        core/progress/nbproject
        core/settings/nbproject
        core/startup/nbproject
        core/swing/plaf/nbproject
        core/swing/tabcontrol/nbproject
        core/tasklist/api/nbproject
        db/model/nbproject
        db/nbproject
        debuggercore/api/nbproject
        debuggercore/nbproject
        debuggercore/viewmodel/nbproject
        debuggerjpda/api/nbproject
        diff/nbproject
        editor/codetemplates/nbproject
        editor/completion/nbproject
        editor/errorstripe/api/nbproject
        editor/errorstripe/nbproject
        editor/fold/nbproject
        editor/guards/nbproject
        editor/hints/nbproject
        editor/indent/nbproject
        editor/lib/nbproject
        editor/lib2/nbproject
        editor/mimelookup/nbproject
        editor/nbproject
        editor/options/nbproject
        editor/settings/nbproject
        editor/settings/storage/nbproject
        editor/util/nbproject
        graph/lib/nbproject
        j2ee/core/utilities/nbproject
        j2ee/ddapi/nbproject
        j2ee/ejbapi/nbproject
        j2ee/metadata/nbproject
        j2ee/metadata/support/nbproject
        j2ee/utilities/nbproject
        j2eeserver/j2eeapis/external
        j2eeserver/j2eeapis/nbproject
        j2eeserver/nbproject
        java/api/nbproject
        java/editor/lib/nbproject
        java/lexer/nbproject
        java/platform/nbproject
        java/project/nbproject
        java/source/javacapi/nbproject
        java/source/javacimpl/nbproject
        java/source/nbproject
        java/source/preprocessorbridge/nbproject
        java/sourceui/nbproject
        junit
        lexer/nbproject
        libs/jsr223/nbproject
        libs/lucene/nbproject
        libs/resolver/external
        libs/resolver/nbproject
        libs/swing-layout/external
        libs/swing-layout/nbproject
        libs/xerces/external
        libs/xerces/nbproject
        openide/actions/nbproject
        openide/awt/nbproject
        openide/dialogs/nbproject
        openide/execution/nbproject
        openide/explorer/nbproject
        openide/fs/nbproject
        openide/io/nbproject
        openide/loaders/nbproject
        openide/masterfs/nbproject
        openide/modules/nbproject
        openide/nodes/nbproject
        openide/options/nbproject
        openide/templates/nbproject
        openide/text/nbproject
        openide/util/nbproject
        openide/windows/nbproject
        openidex/nbproject
        performance/insanelibmodule
        projects/libraries/nbproject
        projects/projectapi/nbproject
        projects/projectuiapi/nbproject
        projects/queries/nbproject
        refactoring/api/nbproject
        schema2beans/rt/nbproject
        serverplugins/server/nbproject
        serverplugins/sun/appsrv/nbproject
        serverplugins/sun/sunddapi/nbproject
        utilities/jumpto/nbproject
        web/webapi/nbproject
        websvc/clientapi/nbproject
        websvc/core/nbproject
        websvc/design/nbproject
        websvc/jaxws21/jaxws21api/nbproject
        websvc/jaxws21/nbproject
        websvc/jaxwsapi/nbproject
        websvc/jaxwsmodel/nbproject
        websvc/utilities/nbproject
        websvc/websvcapi/nbproject
        websvc/websvcddapi/nbproject
        websvc/wsitconf/nbproject
        websvc/wsitmodelext/nbproject
        xml/api/nbproject
        xml/axi/nbproject
        xml/catalog/nbproject
        xml/catalogsupport/nbproject
        xml/core/nbproject
        xml/jxpath/nbproject
        xml/lexer/nbproject
        xml/libs/jxpath/nbproject
        xml/multiview/nbproject
        xml/nbprefuse/nbproject
        xml/prefuse/nbproject
        xml/refactoring/nbproject
        xml/retriever/nbproject
        xml/schema/api/nbproject
        xml/schema/completion/nbproject
        xml/schema/schemaui/nbproject
        xml/tageditorsupport/nbproject
        xml/text-edit/nbproject
        xml/validation/nbproject
        xml/wsdl/api/nbproject
        xml/wsdl/refactoring/nbproject
        xml/wsdlbindingsupport/api/nbproject
        xml/wsdlbindingsupport/nbproject
        xml/wsdlext/nbproject
        xml/wsdlui/nbproject
        xml/xam/nbproject
        xml/xamui/nbproject
        xml/xdm/nbproject
        xml/xpath/ext/nbproject
        xml/xpath/nbproject
        xtest
    "/>
    <property name="test.cvs.root" value=":pserver:guest@cvs.dev.java.net:/cvs"/>
    <property name="test.cvs.path" value="open-jbi-components/driver-tests/bpelse"/>
    <property name="test.cvs.branch" value="-A"/>
    <property name="test.cvs.modules" value="
        ${test.cvs.path}/assign
        ${test.cvs.path}/benchmark
        ${test.cvs.path}/blueprints
        ${test.cvs.path}/BpelToBpel
        ${test.cvs.path}/clusterSupport
        ${test.cvs.path}/compensation
        ${test.cvs.path}/correlation
        ${test.cvs.path}/dynamicpartnerlink
        ${test.cvs.path}/empty
        ${test.cvs.path}/eventHandlers
        ${test.cvs.path}/exit
        ${test.cvs.path}/faulthandling
        ${test.cvs.path}/flow
        ${test.cvs.path}/foreach-bpel20
        ${test.cvs.path}/if
        ${test.cvs.path}/implementMultiOperations
        ${test.cvs.path}/JavaEEIntegration
        ${test.cvs.path}/MessageExchange
        ${test.cvs.path}/misc
        ${test.cvs.path}/PartnerLinks
        ${test.cvs.path}/pick
        ${test.cvs.path}/repeatuntil
        ${test.cvs.path}/rethrow
        ${test.cvs.path}/samples
        ${test.cvs.path}/scalability
        ${test.cvs.path}/scenarios
        ${test.cvs.path}/SchemaElemDecl
        ${test.cvs.path}/ScopeTermination
        ${test.cvs.path}/TerminationHandler
        ${test.cvs.path}/wait
        ${test.cvs.path}/while
        ${test.cvs.path}/xpathfunctions
        ${test.cvs.path}/xslt
    "/>
    <property name="home" value="../.."/>
    <property name="cache" value="${home}/.cache"/>
    <property name="dist" value="${cache}/dist"/>
    <property name="lock" value="${cache}/lock"/>
    <property name="test" value="${cache}/test"/>
    <property name="jbi" location="${test}/${test.cvs.path}"/>
    <property name="latest" value="${cache}/latest"/>
    <property name="netbeans" value="${cache}/netbeans"/>
    <property name="enterprise" value="${home}/enterprise"/>
    <property name="soa" value="${home}/enterprise/bpel/samples/resources"/>
    <property name="build.number" value="${home}/nbbuild/netbeans/platform7/build_number"/>
</project>
