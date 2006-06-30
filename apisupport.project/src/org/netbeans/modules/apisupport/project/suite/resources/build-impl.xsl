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
                xmlns:sproject="http://www.netbeans.org/ns/nb-module-suite-project/1"
                exclude-result-prefixes="xalan p">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
        <xsl:comment><![CDATA[
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***
]]></xsl:comment>
        <xsl:variable name="name" select="/p:project/p:configuration/sproject:data/sproject:name"/>
        <project name="{$name}-impl">
            <xsl:attribute name="basedir">..</xsl:attribute>
            <property file="nbproject/private/platform-private.properties"/>
            <property file="nbproject/platform.properties"/>
            <macrodef name="property" uri="http://www.netbeans.org/ns/nb-module-suite-project/1">
                <attribute name="name"/>
                <attribute name="value"/>
                <sequential>
                    <property name="@{{name}}" value="${{@{{value}}}}"/>
                </sequential>
            </macrodef>
            <!-- Do not do this, as it is legal to define harness.dir and netbeans.dest.dir explicitly:
            <fail unless="nbplatform.active">You must set platform.properties to name your active NB platform</fail>
            -->
            <property file="${{user.properties.file}}"/>
            <sproject:property name="harness.dir" value="nbplatform.${{nbplatform.active}}.harness.dir"/>
            <sproject:property name="netbeans.dest.dir" value="nbplatform.${{nbplatform.active}}.netbeans.dest.dir"/>
            <fail message="You must define 'nbplatform.${{nbplatform.active}}.harness.dir'">
                <condition>
                    <not>
                        <available file="${{harness.dir}}" type="dir"/>
                    </not>
                </condition>
            </fail>
            <import file="${{harness.dir}}/suite.xml"/>
        </project>
    </xsl:template>
</xsl:stylesheet>
