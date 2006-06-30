<?xml version="1.0" encoding="UTF-8"?>
<!--
The contents of this file are subject to the terms of the Common Development
and Distribution License (the License). You may not use this file except in
compliance with the License.

You can obtain a copy of the License at http://www.netbeans.org/cddl.html
or http://www.netbeans.org/cddl.txt.

When distributing Covered Code, include this CDDL Header Notice in each file
and include the License file at http://www.netbeans.org/cddl.txt.
If applicable, add the following below the CDDL Header, with the fields
enclosed by brackets [] replaced by your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:nbmproject="http://www.netbeans.org/ns/nb-module-project/2"
                exclude-result-prefixes="xalan p">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
        <xsl:comment><![CDATA[
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***
]]></xsl:comment>
        <xsl:variable name="codenamebase" select="/p:project/p:configuration/nbmproject:data/nbmproject:code-name-base"/>
        <project name="{$codenamebase}-impl">
            <xsl:attribute name="basedir">..</xsl:attribute>
            <xsl:choose>
                <xsl:when test="/p:project/p:configuration/nbmproject:data/nbmproject:suite-component">
                    <property file="nbproject/private/suite-private.properties"/>
                    <property file="nbproject/suite.properties"/>
                    <fail unless="suite.dir">You must set 'suite.dir' to point to your containing module suite</fail>
                    <property file="${{suite.dir}}/nbproject/private/platform-private.properties"/>
                    <property file="${{suite.dir}}/nbproject/platform.properties"/>
                </xsl:when>
                <xsl:when test="/p:project/p:configuration/nbmproject:data/nbmproject:standalone">
                    <property file="nbproject/private/platform-private.properties"/>
                    <property file="nbproject/platform.properties"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">
                        Cannot generate build-impl.xml for a netbeans.org module!
                    </xsl:message>
                </xsl:otherwise>
            </xsl:choose>
            <macrodef name="property" uri="http://www.netbeans.org/ns/nb-module-project/2">
                <attribute name="name"/>
                <attribute name="value"/>
                <sequential>
                    <property name="@{{name}}" value="${{@{{value}}}}"/>
                </sequential>
            </macrodef>
            <!-- Skip this; it is legal to define netbeans.dest.dir and harness.dir explicitly:
            <xsl:choose>
                <xsl:when test="/p:project/p:configuration/nbmproject:data/nbmproject:suite-component">
                    <fail unless="nbplatform.active">Your suite must set platform.properties to name your active NB platform</fail>
                </xsl:when>
                <xsl:when test="/p:project/p:configuration/nbmproject:data/nbmproject:standalone">
                    <fail unless="nbplatform.active">You must set platform.properties to name your active NB platform</fail>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">
                        Cannot generate build-impl.xml for a netbeans.org module!
                    </xsl:message>
                </xsl:otherwise>
            </xsl:choose>
            -->
            <property file="${{user.properties.file}}"/>
            <nbmproject:property name="harness.dir" value="nbplatform.${{nbplatform.active}}.harness.dir"/>
            <nbmproject:property name="netbeans.dest.dir" value="nbplatform.${{nbplatform.active}}.netbeans.dest.dir"/>
            <fail message="You must define 'nbplatform.${{nbplatform.active}}.harness.dir'">
                <condition>
                    <not>
                        <available file="${{harness.dir}}" type="dir"/>
                    </not>
                </condition>
            </fail>
            <import file="${{harness.dir}}/build.xml"/>
        </project>
    </xsl:template>
</xsl:stylesheet>
