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
                xmlns:project="http://www.netbeans.org/ns/project"
                xmlns:nbm="http://www.netbeans.org/ns/nb-module-project"
                xmlns:xalan="http://xml.apache.org/xslt"
                exclude-result-prefixes="xalan project nbm">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

<xsl:comment> You may freely edit this file. </xsl:comment>
<xsl:comment> The names of existing targets are significant to the IDE. </xsl:comment>
        
<project name="{/project:project/project:configuration/nbm:data/nbm:path}" default="netbeans" basedir=".">
    <target name="netbeans" description="Build complete module.">
        <ant antfile="nbproject/build-impl.xml" target="netbeans"/>
    </target>
    <!-- XXX others... -->
    <target name="clean" description="Clean build products.">
        <ant antfile="nbproject/build-impl.xml" target="clean"/>
    </target>
</project>

    </xsl:template>
    
</xsl:stylesheet> 
