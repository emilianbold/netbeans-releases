<?xml version="1.0" encoding="UTF-8"?>
<!--
  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

  Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.

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
<project name="properties" default="none" basedir=".">
    <property environment="env"/>

    <property name="netbeans.prox" value="false"/>
    <property name="netbeans.type" value="javase"/>

    <!-- NetBeans latest nightly -->
    <!-- property name="netbeans.host" value="http://bits.netbeans.org"/ -->
    <!-- property name="netbeans.path" value="download/trunk/nightly/latest/zip"/ -->
    <!-- property name="netbeans.user" value=".netbeans/dev"/ -->

    <!-- NetBeans latest daily -->
    <property name="netbeans.host" value="http://smetiste.czech.sun.com"/>
    <property name="netbeans.path" value="builds/netbeans/trunk/daily/latest/zip"/>
    <property name="netbeans.user" value=".netbeans/dev"/>

    <!-- Sierra latest continuous -->
    <property name="sierra.host" value="http://nephrite.russia.sun.com:8080"/>
    <property name="sierra.path" value="hudson/job/sierra/ws/cache/latest"/>
    <property name="sierra.file" value="sierra.zip"/>

    <property name="test.cvs.host" value=":pserver:guest@cvs.dev.java.net:/cvs"/>
    <property name="test.cvs.path" value="open-jbi-components/driver-tests/bpelse"/>
    <property name="test.cvs.branch" value="-A"/>
    <property name="test.cvs.modules" value="
        ${test.cvs.path}/assign
        ${test.cvs.path}/benchmark
        ${test.cvs.path}/blueprints
        ${test.cvs.path}/BpelToBpel
        ${test.cvs.path}/CAPSIntegration
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
        ${test.cvs.path}/i18n
        ${test.cvs.path}/if
        ${test.cvs.path}/implementMultiOperations
        ${test.cvs.path}/JavaEEIntegration
        ${test.cvs.path}/MessageExchange
        ${test.cvs.path}/misc
        ${test.cvs.path}/nmproperty
        ${test.cvs.path}/PartnerLinks
        ${test.cvs.path}/pick
        ${test.cvs.path}/POJOCalls
        ${test.cvs.path}/referenced
        ${test.cvs.path}/repeatuntil
        ${test.cvs.path}/rethrow
        ${test.cvs.path}/samples
        ${test.cvs.path}/scalability
        ${test.cvs.path}/scenarios
        ${test.cvs.path}/SchemaElemDecl
        ${test.cvs.path}/ScopeTermination
        ${test.cvs.path}/systemicqualities
        ${test.cvs.path}/TerminationHandler
        ${test.cvs.path}/TestsForBugs
        ${test.cvs.path}/wait
        ${test.cvs.path}/WaitingRequestLifeSpan
        ${test.cvs.path}/while
        ${test.cvs.path}/xpathfunctions
        ${test.cvs.path}/xsddatatypes
        ${test.cvs.path}/xslt
    "/>
    <property name="home" value="../.."/>
    <property name="cache" value="${home}/../cache"/>
    <property name="sierra.build"  value="main"/>
    <property name="netbeans.zip" value="netbeans-6.8.zip"/>
    <property name="repository" value="http://hg.netbeans.org/main"/>
    <property name="sierra.user"  value=".netbeans/dev"/>
    <property name="jbi" location="${cache}/test/${test.cvs.path}"/>
    <property name="bpel.samples" value="${home}/bpel.samples/resources"/>
    <property name="xslt.samples" value="${home}/xslt.samples/resources"/>
    <property name="build.number" value="${home}/nbbuild/netbeans/platform/build_number"/>
</project>
