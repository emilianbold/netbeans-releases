<?xml version="1.0" encoding="UTF-8"?>
<!--
                Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
Microsystems, Inc. All Rights Reserved.
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:project="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                exclude-result-prefixes="xalan project">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
    
    <!-- Annoyingly, the JAXP impl in JRE 1.4.2 seems to randomly reorder attrs. -->
    <!-- (I.e. the DOM tree gets them in an unspecified order?) -->
    <!-- As a workaround, use xsl:attribute for all but the first attr. -->
    <!-- This seems to produce them in the order you want. -->
    <!-- Tedious, but appears to do the job. -->
    <!-- Important for build.xml, which is very visible; not so much for build-impl.xml. -->

<xsl:comment> You may freely edit this file. </xsl:comment>
<xsl:comment> (If you delete it and reopen the project it will be recreated.) </xsl:comment>
<xsl:comment> The names of existing targets are significant to the IDE and should not be removed. </xsl:comment>
<xsl:comment> (For example, 'javadoc' is run when you choose Build -> Generate Javadoc.) </xsl:comment>
        
<xsl:variable name="name" select="/project:project/project:name"/>
<project name="{$name}">
    <xsl:attribute name="default">default</xsl:attribute>
    <xsl:attribute name="basedir">.</xsl:attribute>
    <description>
        Builds, and runs the project <xsl:value-of select="/project:project/project:display-name"/>.
    </description>
    <target name="default">
        <xsl:attribute name="depends">dist,javadoc</xsl:attribute>
        <xsl:attribute name="description">Build whole project.</xsl:attribute>
    </target>
    <target name="dist">
        <xsl:attribute name="description">Build WAR file.</xsl:attribute>
        <ant antfile="nbproject/build-impl.xml">
            <xsl:attribute name="target">dist</xsl:attribute>
        </ant>
    </target>
    <target name="compile">
        <xsl:attribute name="description">Build deployable directory structure.</xsl:attribute>
        <ant antfile="nbproject/build-impl.xml">
            <xsl:attribute name="target">compile</xsl:attribute>
        </ant>
    </target>
    <target name="compile-jsps">
        <xsl:attribute name="description">Compile JavaServer Pages into a temporary area.</xsl:attribute>
        <ant antfile="nbproject/build-impl.xml">
            <xsl:attribute name="target">compile-jsps</xsl:attribute>
        </ant>
    </target>
    <target name="compile-single-jsp">
        <xsl:attribute name="description">Compile single JavaServer Page into a temporary area.</xsl:attribute>
        <ant antfile="nbproject/build-impl.xml">
            <xsl:attribute name="target">compile-single-jsp</xsl:attribute>
        </ant>
    </target>
    <target name="compile-single">
        <ant antfile="nbproject/build-impl.xml">
            <xsl:attribute name="target">compile-single</xsl:attribute>
            <property name="is.test">
                <xsl:attribute name="value">false</xsl:attribute>
            </property>
        </ant>
    </target>
    <target name="validate-single">
        <ant antfile="nbproject/build-impl.xml">
            <xsl:attribute name="target">validate-single</xsl:attribute>
            <property name="is.test">
                <xsl:attribute name="value">false</xsl:attribute>
            </property>
        </ant>
    </target>
    <target name="run">
        <xsl:attribute name="description">Run a main class.</xsl:attribute>
        <ant antfile="nbproject/build-impl.xml">
            <xsl:attribute name="target">run</xsl:attribute>
        </ant>
    </target>
    <target name="debug">
        <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
        <ant antfile="nbproject/build-impl.xml">
            <xsl:attribute name="target">debug-nb</xsl:attribute>
        </ant>
    </target>
    <target name="javadoc">
        <xsl:attribute name="description">Build Javadoc.</xsl:attribute>
        <ant antfile="nbproject/build-impl.xml">
            <xsl:attribute name="target">javadoc-nb</xsl:attribute>
        </ant>
    </target>
    <target name="debug-fix">
        <ant antfile="nbproject/build-impl.xml">
            <xsl:attribute name="target">debug-fix-nb</xsl:attribute>
        </ant>
    </target>
    <target name="clean">
        <xsl:attribute name="description">Clean build products.</xsl:attribute>
        <ant antfile="nbproject/build-impl.xml">
            <xsl:attribute name="target">clean</xsl:attribute>
        </ant>
    </target>
</project>

    </xsl:template>
    
</xsl:stylesheet> 
