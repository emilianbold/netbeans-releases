<?xml version="1.0" encoding="UTF-8"?>
<!--
                Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
            <fail unless="nbplatform.active">You must set platform.properties to name your active NB platform</fail>
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
