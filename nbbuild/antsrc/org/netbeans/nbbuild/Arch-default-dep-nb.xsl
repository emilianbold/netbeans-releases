<?xml version="1.0" encoding="UTF-8" ?>
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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>
    
    <!-- print out <api /> dependencies on all needed netbeans subprojects -->
    <xsl:template match="/" >
        <p>
            These modules are required in project.xml file:
            <ul>
                <xsl:apply-templates select="//dependency" />
            </ul>
        </p>
    </xsl:template>    
        
    <xsl:template match="dependency" >
        <li><api>
            <xsl:attribute name="type">import</xsl:attribute>
            <xsl:attribute name="group">java</xsl:attribute>
            <xsl:attribute name="category">
                <xsl:choose>
                    <xsl:when test="api-category" >
                        <xsl:value-of select="api-category/text()"/>
                    </xsl:when>
                    <xsl:otherwise>private</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:choose>
                    <xsl:when test="api-name" >
                        <xsl:apply-templates select="api-name/text()"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="code-name-base/text()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="url">
                <xsl:text>@</xsl:text>
                <xsl:value-of select="translate(code-name-base/text(),'.','-')"/>
                <xsl:text>@/overview-summary.html</xsl:text>
            </xsl:attribute>

            <xsl:if test="compile-dependency">
                The module is needed for compilation. 
            </xsl:if>
            <xsl:if test="run-dependency">
                The module is used during runtime. 
                <xsl:if test="run-dependency/specification-version">
                    Specification version 
                    <xsl:value-of select="run-dependency/specification-version/node()" />
                    is required.
                </xsl:if>
            </xsl:if>
        </api></li>
    </xsl:template>
</xsl:stylesheet> 

