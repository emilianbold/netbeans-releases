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
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:nbm="http://www.netbeans.org/ns/nb-module-project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                exclude-result-prefixes="xalan p nbm">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
    
    <!-- XXX use xsl:attribute for attr ordering as in j2seproject -->

<xsl:comment>
                Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
Microsystems, Inc. All Rights Reserved.
</xsl:comment>

<xsl:comment> You may freely edit this file. </xsl:comment>
<xsl:comment> The names of existing targets are significant to the IDE. </xsl:comment>
        
<project name="{/p:project/p:configuration/nbm:data/nbm:path}" default="netbeans" basedir=".">
    <target name="netbeans" description="Build complete module.">
        <ant antfile="nbproject/build-impl.xml" target="netbeans"/>
    </target>
    <target name="compile-single">
        <ant antfile="nbproject/build-impl.xml" target="compile-single"/>
    </target>
    <target name="reload" description="Reload module from within IDE.">
        <ant antfile="nbproject/build-impl.xml" target="reload"/>
    </target>
    <target name="nbm" description="Build NBM archive.">
        <ant antfile="nbproject/build-impl.xml" target="nbm"/>
    </target>
    <xsl:if test="/p:project/p:configuration/nbm:data/nbm:javadoc">
        <target name="javadoc" description="Create Javadoc.">
            <ant antfile="nbproject/build-impl.xml" target="javadoc"/>
        </target>
        <target name="javadoc-nb">
            <ant antfile="nbproject/build-impl.xml" target="javadoc-nb"/>
        </target>
    </xsl:if>
    <xsl:if test="/p:project/p:configuration/nbm:data/nbm:unit-tests">
        <target name="test" description="Run unit tests.">
            <ant antfile="nbproject/build-impl.xml" target="test"/>
        </target>
        <target name="test-single">
            <ant antfile="nbproject/build-impl.xml" target="test-single"/>
        </target>
        <target name="debug-test-single">
            <ant antfile="nbproject/build-impl.xml" target="debug-test-single-nb"/>
        </target>
        <target name="compile-test-single">
            <ant antfile="nbproject/build-impl.xml" target="compile-test-single"/>
        </target>
    </xsl:if>
    <target name="clean" description="Clean build products.">
        <ant antfile="nbproject/build-impl.xml" target="clean"/>
    </target>
</project>

    </xsl:template>
    
</xsl:stylesheet>
