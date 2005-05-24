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
                xmlns:project="http://www.netbeans.org/ns/project/1"
                xmlns:sproject="http://www.netbeans.org/ns/nb-module-suite-project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                exclude-result-prefixes="xalan project sproject">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
        <xsl:comment> You may freely edit this file. See harness/README in the NetBeans platform </xsl:comment>
        <xsl:comment> for some information on what you could do (e.g. targets to override). </xsl:comment>
        <xsl:comment> If you delete this file and reopen the project it will be recreated. </xsl:comment>
        <xsl:variable name="name" select="/project:project/project:configuration/sproject:data/sproject:name"/>
        <project name="{$name}"><!-- XXX consider sanitizing as per PropertyUtils.getUsablePropertyName -->
            <!-- XXX default attr? -->
            <xsl:attribute name="basedir">.</xsl:attribute>
            <description>Builds the module suite <xsl:value-of select="$name"/>.</description>
            <import file="nbproject/build-impl.xml"/>
        </project>
    </xsl:template>
</xsl:stylesheet>
