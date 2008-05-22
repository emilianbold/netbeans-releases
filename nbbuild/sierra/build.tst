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
<project name="sierra-test" default="test" basedir=".">
    <import file="build.pro"/>
    
    <!-- test -->
    <target name="unit-test" depends="test"/>
    <target name="proj-test" depends="test-0,test-1,test-2,test-3,test-4,test-5,test-6,test-7,test-8,test-9,test-a,test-b,test-c"/>
    
    <target name="test">
        <!-- print -->
        <ant dir="${home}/print" target="test"/>
    </target>

    <target name="do-test-jbi">
        <ant antfile="${jbi}/${dir}/build.xml" target="clean" inheritAll="false">
            <property name="caps.netbeans.home" location="${home}/nbbuild/netbeans"/>
        </ant>
        <ant antfile="${jbi}/${dir}/build.xml" inheritAll="false">
            <property name="caps.netbeans.home" location="${home}/nbbuild/netbeans"/>
        </ant>
    </target>

    <target name="test-0">
        <!-- samples -->
        <subant target="default" inheritAll="false">
            <property name="caps.netbeans.home" location="${home}/nbbuild/netbeans"/>
            <buildpath location="${samples}/AsynchronousSample/AsynchronousSample/build.xml"/>
            <buildpath location="${samples}/AsynchronousSample/AsynchronousSampleApplication/build.xml"/>
            <buildpath location="${samples}/SynchronousSample/SynchronousSample/build.xml"/>
            <buildpath location="${samples}/SynchronousSample/SynchronousSampleApplication/build.xml"/>
            <buildpath location="${samples}/TravelReservationService/TravelReservationService/build.xml"/>
            <buildpath location="${samples}/TravelReservationService/TravelReservationServiceApplication/build.xml"/>
            <buildpath location="${samples}/BluePrint1/BluePrint1/build.xml"/>
            <buildpath location="${samples}/BluePrint1/BluePrint1Application/build.xml"/>
            <buildpath location="${samples}/BluePrint2/BluePrint2/build.xml"/>
            <buildpath location="${samples}/BluePrint2/BluePrint2Application/build.xml"/>
            <buildpath location="${samples}/BluePrint3/BluePrint3/build.xml"/>
            <buildpath location="${samples}/BluePrint3/BluePrint3Application/build.xml"/>
            <buildpath location="${samples}/BluePrint4/BluePrint4/build.xml"/>
            <buildpath location="${samples}/BluePrint4/BluePrint4Application/build.xml"/>
            <buildpath location="${samples}/BluePrint5/BluePrint5/build.xml"/>
            <buildpath location="${samples}/BluePrint5/BluePrint5Application/build.xml"/>
        </subant>
    </target>

    <target name="test-1">
        <!-- assign -->
        <!-- antcall target="do-test-jbi"><param name="dir" value="assign/ActivateBilling_Simple"/></antcall -->
        <antcall target="do-test-jbi"><param name="dir" value="assign/AssignBpel"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="assign/AssignNamespaces"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="assign/AtomicAssign"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="assign/CopyByValue/CopyByValueBpel"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="assign/DataTypes"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="assign/MessageWithNoParts"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="assign/predicates"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="assign/VariableAssignments"/></antcall>
        <!-- antcall target="do-test-jbi"><param name="dir" value="assign/virtualassignBpel"/></antcall -->
    </target>
        
    <target name="test-2">
        <!-- benchmark -->
        <antcall target="do-test-jbi"><param name="dir" value="benchmark/BenchmarkBpel"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="benchmark/Ericsson/AlarmIRPBpel2"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="benchmark/purchaseOrderCoordinator"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="benchmark/PurchaseOrderService"/></antcall>
    </target>
        
    <target name="test-3">
        <!-- benchmark -->
        <antcall target="do-test-jbi"><param name="dir" value="benchmark/TravelReservationService/TravelReservationService"/></antcall>

        <!-- blueprints -->
        <antcall target="do-test-jbi"><param name="dir" value="blueprints/bp1/BluePrint1"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="blueprints/bp2/BluePrint2"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="blueprints/bp3/BluePrint3"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="blueprints/bp4/BluePrint4"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="blueprints/bp5/BluePrint5"/></antcall>
    </target>

    <target name="test-4">
        <!-- BpelToBpel -->
        <antcall target="do-test-jbi"><param name="dir" value="BpelToBpel/ClientEmployeeInfo"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="BpelToBpel/EmployeeInfo"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="BpelToBpel/InAServiceUnit"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="BpelToBpel/RemoteClientEmployeeInfo"/></antcall>

        <!-- clusterSupport -->
        <antcall target="do-test-jbi"><param name="dir" value="clusterSupport/ClusterBPEL"/></antcall>

        <!-- compensation -->
        <antcall target="do-test-jbi"><param name="dir" value="compensation/IterativeScopes"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="compensation/NestedScope"/></antcall>
    </target>
        
    <target name="test-5">
        <!-- correlation -->
        <antcall target="do-test-jbi"><param name="dir" value="correlation/correlationBPEL"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="correlation/correlationBPEL2"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="correlation/outoforder"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="correlation/CorrelationOnePropBPEL"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="correlation/CorrelationTwoPropBPEL"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="correlation/FlowAsStartActivity"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="correlation/MatchingInstanceTwoCorrBPEL"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="correlation/ThreeCorrSetsBpel"/></antcall>
    </target>

    <target name="test-6">
        <!-- dynamicpartnerlink -->
        <antcall target="do-test-jbi"><param name="dir" value="dynamicpartnerlink/dynamicPartnerLinkBPEL"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="dynamicpartnerlink/justhttpAndBPEL"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="dynamicpartnerlink/JustHTTPandBPEL2"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="dynamicpartnerlink/sendCallBackInfo"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="dynamicpartnerlink/SyncSampleEPRAssign"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="dynamicpartnerlink/SyncSampleEPRLiteral"/></antcall>

        <!-- empty -->
        <antcall target="do-test-jbi"><param name="dir" value="empty/EmptyBpel"/></antcall>

        <!-- eventHandlers -->
        <antcall target="do-test-jbi"><param name="dir" value="eventHandlers/onAlarm"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="eventHandlers/onEvent"/></antcall>

        <!-- exit -->
        <antcall target="do-test-jbi"><param name="dir" value="exit/ExitBpel"/></antcall>
    </target>

    <target name="test-7">
        <!-- faulthandling -->
        <antcall target="do-test-jbi"><param name="dir" value="faulthandling/FaultHandlingBpel"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="faulthandling/StandardFaultsBpel"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="faulthandling/SystemFaultsBpel"/></antcall>
        
        <!-- flow -->
        <antcall target="do-test-jbi"><param name="dir" value="flow/FlowBpel"/></antcall>

        <!-- foreach-bpel20 -->
        <antcall target="do-test-jbi"><param name="dir" value="foreach-bpel20/ForEachBpel"/></antcall>

        <!-- i18n -->
        <!-- antcall target="do-test-jbi"><param name="dir" value="i18n/assign/AssignBpel"/></antcall -->

        <!-- if -->
        <antcall target="do-test-jbi"><param name="dir" value="if/ifBpel"/></antcall>

        <!-- implementMultiOperations -->
        <antcall target="do-test-jbi"><param name="dir" value="implementMultiOperations/MultipleBPELs"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="implementMultiOperations/OneBPEL"/></antcall>
    </target>

    <target name="test-8">
        <!-- JavaEEIntegration -->
        <antcall target="do-test-jbi"><param name="dir" value="JavaEEIntegration/BPToJavaEE/JavaEEFault/FaultBP"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="JavaEEIntegration/DirectElem/MsgDirectElem"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="JavaEEIntegration/InOnlyMultipleBPJava/InOnlyMultiBP"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="JavaEEIntegration/JavaEEToBP/FaultTest/bplGreetService"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="JavaEEIntegration/JavaEEToBP/JavaEEToBPProj"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="JavaEEIntegration/JavaEEToBP/ProdMngmntSamples/CreditApplicationProcessorBusinessProcess"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="JavaEEIntegration/TwoWayEjbToBP/TwoWay"/></antcall>
        
        <!-- MessageExchange -->
        <antcall target="do-test-jbi"><param name="dir" value="MessageExchange/MessageExchangeBpel"/></antcall>
    </target>

    <target name="test-9">
        <!-- misc -->
        <antcall target="do-test-jbi"><param name="dir" value="misc/MiscBpel"/></antcall>

        <!-- PartnerLinks -->
        <antcall target="do-test-jbi"><param name="dir" value="PartnerLinks/DuplicateProject1"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="PartnerLinks/DuplicateProject2"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="PartnerLinks/OneBP"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="PartnerLinks/PLTLocalNamespace"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="PartnerLinks/TwoBPDuplicateImpl"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="PartnerLinks/TwoBPs"/></antcall>

        <!-- pick -->
        <antcall target="do-test-jbi"><param name="dir" value="pick/PickBpel"/></antcall>

        <!-- repeatuntil -->
        <!-- antcall target="do-test-jbi"><param name="dir" value="repeatuntil/RepeatUntilBpel"/></antcall -->

        <!-- rethrow -->
        <antcall target="do-test-jbi"><param name="dir" value="rethrow/Rethrow"/></antcall>
    </target>
        
    <target name="test-a">
        <!-- samples -->
        <antcall target="do-test-jbi"><param name="dir" value="samples/AsynchronousSample"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="samples/EndToEndScenario"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="samples/samplesBPEL"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="samples/SynchronousSample"/></antcall>

        <!-- scalability -->
        <!-- antcall target="do-test-jbi"><param name="dir" value="scalability/ScalabilityBpel"/></antcall -->
        <antcall target="do-test-jbi"><param name="dir" value="scalability/Test2/ScalabilityTest2BP"/></antcall>

        <!-- scenarios -->
        <!-- antcall target="do-test-jbi"><param name="dir" value="scenarios/CandidateSelection/SelectionProcess"/></antcall -->

        <!-- SchemaElemDecl -->
        <antcall target="do-test-jbi"><param name="dir" value="SchemaElemDecl/BPwithElemRef"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="SchemaElemDecl/BPwithMsgElement"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="SchemaElemDecl/BPwithMsgElemQualified"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="SchemaElemDecl/BPwithNestedElem"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="SchemaElemDecl/BPwithNestedElemQualified"/></antcall>
    </target>

    <target name="test-b">
        <!-- ScopeTermination -->
        <antcall target="do-test-jbi"><param name="dir" value="ScopeTermination/ScopeTerminationBpel"/></antcall>

        <!-- systemicqualities -->
        <antcall target="do-test-jbi"><param name="dir" value="systemicqualities/Redelivery/RedeliverWithinRetries"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="systemicqualities/Redelivery/RedeliverWithSuspend"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="systemicqualities/Redelivery/RedirectOnFailure"/></antcall>
        
        <!-- TerminationHandler -->
        <antcall target="do-test-jbi"><param name="dir" value="TerminationHandler/TerminationHandlerBpel"/></antcall>

        <!-- TestsForBugs -->
        <antcall target="do-test-jbi"><param name="dir" value="TestsForBugs/Bug6431708/testBPEL"/></antcall>

        <!-- wait -->
        <antcall target="do-test-jbi"><param name="dir" value="wait/WaitBpel"/></antcall>

        <!-- while -->
        <antcall target="do-test-jbi"><param name="dir" value="while/WhileBpel"/></antcall>
    </target>

    <target name="test-c">
        <!-- xpathfunctions -->
        <antcall target="do-test-jbi"><param name="dir" value="xpathfunctions/XPathFunctionsBpel"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="xpathfunctions/XSDFunctions"/></antcall>

        <!-- xsddatatypes -->
        <antcall target="do-test-jbi"><param name="dir" value="xsddatatypes/XSDDataTypesBpel"/></antcall>

        <!-- xslt -->
        <antcall target="do-test-jbi"><param name="dir" value="xslt/DoXslTransformBPEL"/></antcall>
        <antcall target="do-test-jbi"><param name="dir" value="xslt/InOutBPXSLT/InOutBP"/></antcall>
    </target>
</project>
