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
<project name="sierra-properties" default="none" basedir=".">
    <property environment="env"/>

    <!-- dev -->
    <property name="netbeans.host" value="http://deadlock.netbeans.org"/>
    <property name="netbeans.path" value="hudson/job/trunk/lastSuccessfulBuild/artifact/nbbuild/dist/zip"/>

    <!-- daily -->
    <property name="netbeans.host" value="http://bits.netbeans.org"/>
    <property name="netbeans.path" value="dev/nightly/latest/zip"/>

    <property name="test.cvs.root" value=":pserver:guest@cvs.dev.java.net:/cvs"/>
    <property name="test.cvs.path" value="open-jbi-components/driver-tests/bpelse"/>
    <property name="test.cvs.branch" value="-A"/>
    <property name="test.cvs.modules" value="
        ${test.cvs.path}/assign/ActivateBilling_Simple
        ${test.cvs.path}/assign/AssignBpel
        ${test.cvs.path}/assign/AssignNamespaces
        ${test.cvs.path}/assign/AtomicAssign
        ${test.cvs.path}/assign/CopyByValue/CopyByValueBpel
        ${test.cvs.path}/assign/DataTypes
        ${test.cvs.path}/assign/MessageWithNoParts
        ${test.cvs.path}/assign/predicates
        ${test.cvs.path}/assign/VariableAssignments
        ${test.cvs.path}/assign/virtualassignBpel
        ${test.cvs.path}/benchmark/BenchmarkBpel
        ${test.cvs.path}/benchmark/Ericsson/AlarmIRPBpel2
        ${test.cvs.path}/benchmark/purchaseOrderCoordinator
        ${test.cvs.path}/benchmark/PurchaseOrderService
        ${test.cvs.path}/benchmark/TravelReservationService/TravelReservationService
        ${test.cvs.path}/blueprints/bp1/BluePrint1
        ${test.cvs.path}/blueprints/bp2/BluePrint2
        ${test.cvs.path}/blueprints/bp3/BluePrint3
        ${test.cvs.path}/blueprints/bp4/BluePrint4
        ${test.cvs.path}/blueprints/bp5/BluePrint5
        ${test.cvs.path}/BpelToBpel/ClientEmployeeInfo
        ${test.cvs.path}/BpelToBpel/EmployeeInfo
        ${test.cvs.path}/BpelToBpel/InAServiceUnit
        ${test.cvs.path}/BpelToBpel/RemoteClientEmployeeInfo
        ${test.cvs.path}/clusterSupport/ClusterBPEL
        ${test.cvs.path}/compensation/IterativeScopes
        ${test.cvs.path}/compensation/NestedScope
        ${test.cvs.path}/correlation/correlationBPEL
        ${test.cvs.path}/correlation/correlationBPEL2
        ${test.cvs.path}/correlation/outoforder
        ${test.cvs.path}/correlation/CorrelationOnePropBPEL
        ${test.cvs.path}/correlation/CorrelationTwoPropBPEL
        ${test.cvs.path}/correlation/FlowAsStartActivity
        ${test.cvs.path}/correlation/MatchingInstanceTwoCorrBPEL
        ${test.cvs.path}/correlation/ThreeCorrSetsBpel
        ${test.cvs.path}/dynamicpartnerlink/dynamicPartnerLinkBPEL
        ${test.cvs.path}/dynamicpartnerlink/justhttpAndBPEL
        ${test.cvs.path}/dynamicpartnerlink/JustHTTPandBPEL2
        ${test.cvs.path}/dynamicpartnerlink/sendCallBackInfo
        ${test.cvs.path}/dynamicpartnerlink/SyncSampleEPRAssign
        ${test.cvs.path}/dynamicpartnerlink/SyncSampleEPRLiteral
        ${test.cvs.path}/empty/EmptyBpel
        ${test.cvs.path}/eventHandlers/onAlarm
        ${test.cvs.path}/eventHandlers/onEvent
        ${test.cvs.path}/exit/ExitBpel
        ${test.cvs.path}/faulthandling/FaultHandlingBpel
        ${test.cvs.path}/faulthandling/StandardFaultsBpel
        ${test.cvs.path}/faulthandling/SystemFaultsBpel
        ${test.cvs.path}/flow/FlowBpel
        ${test.cvs.path}/foreach-bpel20/ForEachBpel
        ${test.cvs.path}/i18n/assign/AssignBpel
        ${test.cvs.path}/if/ifBpel
        ${test.cvs.path}/implementMultiOperations/MultipleBPELs
        ${test.cvs.path}/implementMultiOperations/OneBPEL
        ${test.cvs.path}/JavaEEIntegration/BPToJavaEE/JavaEEFault/FaultBP
        ${test.cvs.path}/JavaEEIntegration/DirectElem/MsgDirectElem
        ${test.cvs.path}/JavaEEIntegration/InOnlyMultipleBPJava/InOnlyMultiBP
        ${test.cvs.path}/JavaEEIntegration/JavaEEToBP/FaultTest/bplGreetService
        ${test.cvs.path}/JavaEEIntegration/JavaEEToBP/JavaEEToBPProj
        ${test.cvs.path}/JavaEEIntegration/JavaEEToBP/ProdMngmntSamples/CreditApplicationProcessorBusinessProcess
        ${test.cvs.path}/JavaEEIntegration/TwoWayEjbToBP/TwoWay
        ${test.cvs.path}/MessageExchange/MessageExchangeBpel
        ${test.cvs.path}/misc/MiscBpel
        ${test.cvs.path}/PartnerLinks/DuplicateProject1
        ${test.cvs.path}/PartnerLinks/DuplicateProject2
        ${test.cvs.path}/PartnerLinks/OneBP
        ${test.cvs.path}/PartnerLinks/PLTLocalNamespace
        ${test.cvs.path}/PartnerLinks/TwoBPDuplicateImpl
        ${test.cvs.path}/PartnerLinks/TwoBPs
        ${test.cvs.path}/pick/PickBpel
        ${test.cvs.path}/repeatuntil/RepeatUntilBpel
        ${test.cvs.path}/rethrow/Rethrow
        ${test.cvs.path}/samples/AsynchronousSample
        ${test.cvs.path}/samples/EndToEndScenario
        ${test.cvs.path}/samples/samplesBPEL
        ${test.cvs.path}/samples/SynchronousSample
        ${test.cvs.path}/scalability/ScalabilityBpel
        ${test.cvs.path}/scalability/Test2/ScalabilityTest2BP
        ${test.cvs.path}/scenarios/CandidateSelection/SelectionProcess
        ${test.cvs.path}/SchemaElemDecl/BPwithElemRef
        ${test.cvs.path}/SchemaElemDecl/BPwithMsgElement
        ${test.cvs.path}/SchemaElemDecl/BPwithMsgElemQualified
        ${test.cvs.path}/SchemaElemDecl/BPwithNestedElem
        ${test.cvs.path}/SchemaElemDecl/BPwithNestedElemQualified
        ${test.cvs.path}/ScopeTermination/ScopeTerminationBpel
        ${test.cvs.path}/systemicqualities/Redelivery/RedeliverWithinRetries
        ${test.cvs.path}/systemicqualities/Redelivery/RedeliverWithSuspend
        ${test.cvs.path}/systemicqualities/Redelivery/RedirectOnFailure
        ${test.cvs.path}/TerminationHandler/TerminationHandlerBpel
        ${test.cvs.path}/TestsForBugs/Bug6431708/testBPEL
        ${test.cvs.path}/wait/WaitBpel
        ${test.cvs.path}/while/WhileBpel
        ${test.cvs.path}/xpathfunctions/XPathFunctionsBpel
        ${test.cvs.path}/xpathfunctions/XSDFunctions
        ${test.cvs.path}/xsddatatypes/XSDDataTypesBpel
        ${test.cvs.path}/xslt/DoXslTransformBPEL
        ${test.cvs.path}/xslt/InOutBPXSLT/InOutBP
    "/>
    <property name="home" value="../.."/>
    <property name="cache" value="${home}/../cache"/>
    <property name="sierra.dir"  value=".netbeans/sierra"/>
    <property name="netbeans.dir" value=".netbeans/dev"/>
    <property name="jbi" location="${cache}/test/${test.cvs.path}"/>
    <property name="samples" value="${home}/bpel.samples/resources"/>
    <property name="build.number" value="${home}/nbbuild/netbeans/platform8/build_number"/>
</project>
