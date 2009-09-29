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
  License. When distributing the software, include this License Header
  Notice in each file and include the License file at
  nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
<project name="test" default="test" basedir=".">
    <import file="build.pro"/>
    
    <!-- test -->
    <target name="test" depends="unit-test,sample-test,project-test"/>
    
    <!-- unit test -->
    <target name="unit-test">
        <!-- ant target="test" dir="${home}/print"/-->
        <ant target="test" dir="${home}/xml.search"/>
        <ant target="test" dir="${home}/bpel.model"/>
    </target>

    <!-- sample test -->
    <target name="sample-test" depends="test-s1,test-s2,test-s3"/>

    <target name="test-s1">
        <subant target="default" inheritAll="false">
            <property name="complete.validation"           value="false"/>
            <property name="esb.netbeans.home"             location="${home}/nbbuild/netbeans"/>
            <property name="ide.module.install.dir"        location="${home}/nbbuild/netbeans/ide10/modules"/>
            <property name="soa.module.install.dir"        location="${home}/nbbuild/netbeans/soa2/modules"/>
            <property name="xml.module.install.dir"        location="${home}/nbbuild/netbeans/xml2/modules"/>
            <property name="java.module.install.dir"       location="${home}/nbbuild/netbeans/java3/modules"/>
            <property name="enterprise.module.install.dir" location="${home}/nbbuild/netbeans/enterprise6/modules"/>

            <!-- BPEL sample 1 -->
            <buildpath location="${bpel.samples}/Asynchronous/Asynchronous/build.xml"/>
            <buildpath location="${bpel.samples}/Asynchronous/AsynchronousApplication/build.xml"/>
            <buildpath location="${bpel.samples}/Synchronous/Synchronous/build.xml"/>
            <buildpath location="${bpel.samples}/Synchronous/SynchronousApplication/build.xml"/>
            <buildpath location="${bpel.samples}/TravelReservationService/TravelReservationService/build.xml"/>
            <buildpath location="${bpel.samples}/TravelReservationService/TravelReservationServiceApplication/build.xml"/>
            <buildpath location="${bpel.samples}/BluePrint1/BluePrint1/build.xml"/>
            <buildpath location="${bpel.samples}/BluePrint1/BluePrint1Application/build.xml"/>
            <buildpath location="${bpel.samples}/BluePrint2/BluePrint2/build.xml"/>
            <buildpath location="${bpel.samples}/BluePrint2/BluePrint2Application/build.xml"/>
        </subant>
    </target>

    <target name="test-s2">
        <subant target="default" inheritAll="false">
            <property name="complete.validation"           value="false"/>
            <property name="esb.netbeans.home"             location="${home}/nbbuild/netbeans"/>
            <property name="ide.module.install.dir"        location="${home}/nbbuild/netbeans/ide10/modules"/>
            <property name="soa.module.install.dir"        location="${home}/nbbuild/netbeans/soa2/modules"/>
            <property name="xml.module.install.dir"        location="${home}/nbbuild/netbeans/xml2/modules"/>
            <property name="java.module.install.dir"       location="${home}/nbbuild/netbeans/java3/modules"/>
            <property name="enterprise.module.install.dir" location="${home}/nbbuild/netbeans/enterprise6/modules"/>

            <!-- BPEL sample 2 -->
            <buildpath location="${bpel.samples}/BluePrint3/BluePrint3/build.xml"/>
            <buildpath location="${bpel.samples}/BluePrint3/BluePrint3Application/build.xml"/>
            <buildpath location="${bpel.samples}/BluePrint4/BluePrint4/build.xml"/>
            <buildpath location="${bpel.samples}/BluePrint4/BluePrint4Application/build.xml"/>
            <buildpath location="${bpel.samples}/BluePrint5/BluePrint5/build.xml"/>
            <buildpath location="${bpel.samples}/BluePrint5/BluePrint5Application/build.xml"/>
        </subant>
    </target>

    <target name="test-s3">
        <subant target="default" inheritAll="false">
            <property name="complete.validation"           value="false"/>
            <property name="esb.netbeans.home"             location="${home}/nbbuild/netbeans"/>
            <property name="ide.module.install.dir"        location="${home}/nbbuild/netbeans/ide10/modules"/>
            <property name="soa.module.install.dir"        location="${home}/nbbuild/netbeans/soa2/modules"/>
            <property name="xml.module.install.dir"        location="${home}/nbbuild/netbeans/xml2/modules"/>
            <property name="java.module.install.dir"       location="${home}/nbbuild/netbeans/java3/modules"/>
            <property name="enterprise.module.install.dir" location="${home}/nbbuild/netbeans/enterprise6/modules"/>

            <!-- XSLT sample -->
            <buildpath location="${xslt.samples}/Welcome/Welcome/build.xml"/>
            <buildpath location="${xslt.samples}/Welcome/WelcomeApplication/build.xml"/>
        </subant>
    </target>

    <!-- project test -->
    <target name="project-test" depends="project-test-00,project-test-10,project-test-20"/>
    <target name="project-test-00" depends="test-01,test-02,test-03,test-04,test-05,test-06,test-07,test-08,test-09,test-10"/>
    <target name="project-test-10" depends="test-11,test-12,test-13,test-14,test-15,test-16,test-17,test-18,test-19,test-20"/>
    <target name="project-test-20" depends="test-21,test-22,test-23,test-24,test-25,test-26,test-27"/>

    <target name="do-jbi-test">
        <ant antfile="${jbi}/${dir}/build.xml" target="clean" inheritAll="false">
            <property name="esb.netbeans.home" location="${home}/nbbuild/netbeans"/>
        </ant>
        <ant antfile="${jbi}/${dir}/build.xml" inheritAll="false">
            <property name="esb.netbeans.home" location="${home}/nbbuild/netbeans"/>
        </ant>
    </target>

    <target name="test-01">
        <!-- assign 1 -->
        <antcall target="do-jbi-test"><param name="dir" value="assign/ActivateBilling_Simple"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/Assign2BPEL"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/AssignBpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/AssignIgnoreMissingFromData"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/AssignLiteralBpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/AssignNamespaces"/></antcall>
    </target>

    <target name="test-02">
        <!-- assign 2 -->
        <antcall target="do-jbi-test"><param name="dir" value="assign/AssignSelectionFailure"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/AtomicAssign"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/AttrQTest"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/AttrTest1"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/CopyByValue/CopyByValueBpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/DataTypes"/></antcall>
    </target>

    <target name="test-03">
        <!-- assign 3 -->
        <antcall target="do-jbi-test"><param name="dir" value="assign/MessageWithNoParts"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/pattern-demo-content-based-router"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/predicates"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/scriptExampleProcessRepeatingNodes"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/testBooleanOpsBPEL"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/TypeHierarchy"/></antcall>
    </target>

    <target name="test-04">
        <!-- assign 4 -->
        <antcall target="do-jbi-test"><param name="dir" value="assign/VariableAssignments"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/VariablePropertyTest/BooleanCopy"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/VariablePropertyTest/conditionProj"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/VariablePropertyTest/FromToPropertySpec"/></antcall>
    </target>
    
    <target name="test-05">
        <!-- assign 5 -->
        <antcall target="do-jbi-test"><param name="dir" value="assign/VariablePropertyTest/QualifiedQuery"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/VariablePropertyTest/VariableProperty"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/VariablePropertyTest/VariableProperty1Bpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="assign/virtualassignBpel"/></antcall>
    </target>

    <target name="test-06">
        <!-- benchmark 1 -->
        <antcall target="do-jbi-test"><param name="dir" value="benchmark/BenchmarkBpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="benchmark/dynamicAddressingDPL"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="benchmark/Ericsson/AlarmIRPBpel2"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="benchmark/purchaseOrderCoordinator"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="benchmark/PurchaseOrderService"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="benchmark/Server/Server"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="benchmark/Transformation"/></antcall>
    </target>
        
    <target name="test-07">
        <!-- benchmark 2 -->
        <antcall target="do-jbi-test"><param name="dir" value="benchmark/TravelReservationService/TravelReservationService"/></antcall>

        <!-- blueprints -->
        <antcall target="do-jbi-test"><param name="dir" value="blueprints/bp1/BluePrint1"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="blueprints/bp2/BluePrint2"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="blueprints/bp3/BluePrint3"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="blueprints/bp4/BluePrint4"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="blueprints/bp5/BluePrint5"/></antcall>
    </target>

    <target name="test-08">
        <!-- BpelToBpel -->
        <antcall target="do-jbi-test"><param name="dir" value="BpelToBpel/ClientEmployeeInfo"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="BpelToBpel/EmployeeInfo"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="BpelToBpel/InAServiceUnit"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="BpelToBpel/RemoteClientEmployeeInfo"/></antcall>

        <!-- CAPSIntegration -->
        <antcall target="do-jbi-test"><param name="dir" value="CAPSIntegration/BPtoJCDEAP/prjPfizerSupplyChainEAP_BPEL_JMS_2"/></antcall>
    </target>

    <target name="test-09">
        <!-- clusterSupport -->
        <antcall target="do-jbi-test"><param name="dir" value="clusterSupport/ClusterBPEL"/></antcall>

        <!-- compensation -->
        <antcall target="do-jbi-test"><param name="dir" value="compensation/IterativeScopes"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="compensation/NestedScope"/></antcall>
    </target>
        
    <target name="test-10">
        <!-- correlation 1 -->
        <antcall target="do-jbi-test"><param name="dir" value="correlation/correlationBPEL"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="correlation/correlationBPEL2"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="correlation/CorrelationOnePropBPEL"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="correlation/CorrelationTwoPropBPEL"/></antcall>
    </target>

    <target name="test-11">
        <!-- correlation 2 -->
        <antcall target="do-jbi-test"><param name="dir" value="correlation/FlowAsStartActivity"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="correlation/MatchingInstanceTwoCorrBPEL"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="correlation/outoforder"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="correlation/ThreeCorrSetsBpel"/></antcall>
    </target>

    <target name="test-12">
        <!-- dynamicpartnerlink 1 -->
        <antcall target="do-jbi-test"><param name="dir" value="dynamicpartnerlink/DPL_OneWayCallbackBpelServiceBpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="dynamicpartnerlink/DPL_SendEPRToPartnerBpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="dynamicpartnerlink/dynamicPartnerLinkBPEL"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="dynamicpartnerlink/justhttpAndBPEL"/></antcall>
    </target>

    <target name="test-13">
        <!-- dynamicpartnerlink 2 -->
        <antcall target="do-jbi-test"><param name="dir" value="dynamicpartnerlink/DPL_OneWayCallbackBpelServiceBpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="dynamicpartnerlink/DPL_SendEPRToPartnerBpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="dynamicpartnerlink/dynamicPartnerLinkBPEL"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="dynamicpartnerlink/justhttpAndBPEL"/></antcall>
    </target>

    <target name="test-14">
        <!-- empty -->
        <antcall target="do-jbi-test"><param name="dir" value="empty/EmptyBpel"/></antcall>

        <!-- eventHandlers -->
        <antcall target="do-jbi-test"><param name="dir" value="eventHandlers/onAlarm"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="eventHandlers/onEvent"/></antcall>

        <!-- exit -->
        <antcall target="do-jbi-test"><param name="dir" value="exit/ExitBpel"/></antcall>
    </target>

    <target name="test-15">
        <!-- faulthandling -->
        <antcall target="do-jbi-test"><param name="dir" value="faulthandling/FaultHandlingBpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="faulthandling/faultMsg/faultMsg"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="faulthandling/StandardFaultsBpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="faulthandling/SystemFaultsBpel"/></antcall>

        <!-- flow -->
        <antcall target="do-jbi-test"><param name="dir" value="flow/FlowBpel"/></antcall>

        <!-- foreach-bpel20 -->
        <antcall target="do-jbi-test"><param name="dir" value="foreach-bpel20/ForEachBpel"/></antcall>
    </target>

    <target name="test-16">
        <!-- i18n -->
        <antcall target="do-jbi-test"><param name="dir" value="i18n/assign/AssignBpel"/></antcall>

        <!-- if -->
        <antcall target="do-jbi-test"><param name="dir" value="if/ifBpel"/></antcall>

        <!-- implementMultiOperations -->
        <antcall target="do-jbi-test"><param name="dir" value="implementMultiOperations/MultipleBPELs"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="implementMultiOperations/OneBPEL"/></antcall>

        <!-- JavaEEIntegration 1 -->
        <antcall target="do-jbi-test"><param name="dir" value="JavaEEIntegration/BPToJavaEE/JavaEEFault/FaultBP"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="JavaEEIntegration/DirectElem/MsgDirectElem"/></antcall>
    </target>

    <target name="test-17">
        <!-- JavaEEIntegration 2 -->
        <antcall target="do-jbi-test"><param name="dir" value="JavaEEIntegration/InOnlyMultipleBPJava/InOnlyMultiBP"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="JavaEEIntegration/JavaEEToBP/FaultTest/bplGreetService"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="JavaEEIntegration/JavaEEToBP/JavaEEToBPProj"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="JavaEEIntegration/JavaEEToBP/ProdMngmntSamples/CreditApplicationProcessorBusinessProcess"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="JavaEEIntegration/TwoWayEjbToBP/TwoWay"/></antcall>
    </target>

    <target name="test-18">
        <!-- MessageExchange -->
        <antcall target="do-jbi-test"><param name="dir" value="MessageExchange/MessageExchangeBpel"/></antcall>

        <!-- misc -->
        <antcall target="do-jbi-test"><param name="dir" value="misc/MiscBpel"/></antcall>

        <!-- nmproperty 1 -->
        <antcall target="do-jbi-test"><param name="dir" value="nmproperty/AccessToSubject/BasicAuthEcho"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="nmproperty/AccessToSubject/SyncSampleWithSAML/SyncSampleWithSAML"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="nmproperty/dynamicaddressingNMproperty"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="nmproperty/FromToPropertySpec"/></antcall>
    </target>

    <target name="test-19">
        <!-- nmproperty 2 -->
        <antcall target="do-jbi-test"><param name="dir" value="nmproperty/HttpOutBoundHeaderTest_bpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="nmproperty/Server/Server"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="nmproperty/SOAPHeader"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="nmproperty/TestBasicAuth/TestBasicAuth"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="nmproperty/TestBasicAuth/TestBasicAuthClient"/></antcall>
    </target>

    <target name="test-20">
        <!-- PartnerLinks -->
        <antcall target="do-jbi-test"><param name="dir" value="PartnerLinks/DuplicateProject1"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="PartnerLinks/DuplicateProject2"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="PartnerLinks/OneBP"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="PartnerLinks/PLTLocalNamespace"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="PartnerLinks/SinglePartnerLink"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="PartnerLinks/TwoBPDuplicateImpl"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="PartnerLinks/TwoBPs"/></antcall>
    </target>
        
    <target name="test-21">
        <!-- pick -->
        <antcall target="do-jbi-test"><param name="dir" value="pick/PickBpel"/></antcall>

        <!-- POJOCalls -->
        <antcall target="do-jbi-test"><param name="dir" value="POJOCalls/JavaCallWithNoParam"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="POJOCalls/memberJavaMethod/memberJavaMethod"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="POJOCalls/testXPath"/></antcall>

        <!-- referenced -->
        <antcall target="do-jbi-test"><param name="dir" value="referenced/Master"/></antcall>

        <!-- repeatuntil -->
        <antcall target="do-jbi-test"><param name="dir" value="repeatuntil/RepeatUntilBpel"/></antcall>
    </target>

    <target name="test-22">
        <!-- rethrow -->
        <antcall target="do-jbi-test"><param name="dir" value="rethrow/Rethrow"/></antcall>

        <!-- samples -->
        <antcall target="do-jbi-test"><param name="dir" value="samples/AsynchronousSample"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="samples/EndToEndScenario"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="samples/samplesBPEL"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="samples/SynchronousSample"/></antcall>
    </target>

    <target name="test-23">
        <!-- scalability -->
        <antcall target="do-jbi-test"><param name="dir" value="scalability/ScalabilityBpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="scalability/Test2/ScalabilityTest2BP"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="scalability/Test3/ScalabilityTest3Bpel"/></antcall>

        <!-- scenarios -->
        <antcall target="do-jbi-test"><param name="dir" value="scenarios/CandidateSelection/SelectionProcess"/></antcall>
    </target>

    <target name="test-24">
        <!-- SchemaElemDecl -->
        <antcall target="do-jbi-test"><param name="dir" value="SchemaElemDecl/BPwithElemRef"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="SchemaElemDecl/BPwithMsgElement"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="SchemaElemDecl/BPwithMsgElemQualified"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="SchemaElemDecl/BPwithNestedElem"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="SchemaElemDecl/BPwithNestedElemQualified"/></antcall>

        <!-- ScopeTermination -->
        <antcall target="do-jbi-test"><param name="dir" value="ScopeTermination/ScopeTerminationBpel"/></antcall>
    </target>

    <target name="test-25">
        <!-- systemicqualities -->
        <antcall target="do-jbi-test"><param name="dir" value="systemicqualities/Redelivery/RedeliverWithinRetries"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="systemicqualities/Redelivery/RedeliverWithSuspend"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="systemicqualities/Redelivery/RedeliveryRedirectToBP-503/DPL_OneWayBpelService1Bpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="systemicqualities/Redelivery/RedeliveryRedirectToBP-503/RedeliveryRedirectToBPFail1Bpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="systemicqualities/Redelivery/RedirectOnFailure"/></antcall>

        <!-- TerminationHandler -->
        <antcall target="do-jbi-test"><param name="dir" value="TerminationHandler/TerminationHandlerBpel"/></antcall>
    </target>

    <target name="test-26">
        <!-- TestsForBugs -->
        <antcall target="do-jbi-test"><param name="dir" value="TestsForBugs/Bug6431708/testBPEL"/></antcall>

        <!-- wait -->
        <antcall target="do-jbi-test"><param name="dir" value="wait/WaitBpel"/></antcall>

        <!-- WaitingRequestLifeSpan -->
        <antcall target="do-jbi-test"><param name="dir" value="WaitingRequestLifeSpan/WaitingRequestLifeSpan"/></antcall>

        <!-- while -->
        <antcall target="do-jbi-test"><param name="dir" value="while/WhileBpel"/></antcall>
    </target>

    <target name="test-27">
        <!-- xpathfunctions -->
        <antcall target="do-jbi-test"><param name="dir" value="xpathfunctions/DateTimeComparison/DateTimeComparisonBPEL"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="xpathfunctions/XPathFunctionsBpel"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="xpathfunctions/XSDFunctions"/></antcall>

        <!-- xsddatatypes -->
        <antcall target="do-jbi-test"><param name="dir" value="xsddatatypes/XSDDataTypesBpel"/></antcall>

        <!-- xslt -->
        <antcall target="do-jbi-test"><param name="dir" value="xslt/DoXslTransformBPEL"/></antcall>
        <antcall target="do-jbi-test"><param name="dir" value="xslt/InOutBPXSLT/InOutBP"/></antcall>
    </target>
</project>
