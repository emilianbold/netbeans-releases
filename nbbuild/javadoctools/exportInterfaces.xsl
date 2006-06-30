<?xml version="1.0" encoding="UTF-8" ?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" omit-xml-declaration="yes"/>

    <xsl:param name="arch.stylesheet"/>
    <xsl:param name="arch.overviewlink"/>
    <xsl:param name="arch.footer"/>
    <xsl:param name="arch.target"/>

    <xsl:template match="/">
        <xsl:variable name="interfaces" select="//api[@type='export']" />

        <module name="{api-answers/@module}"
                target="{$arch.target}"
                stylesheet="{$arch.stylesheet}"
                overviewlink="{$arch.overviewlink}"
                footer="{$arch.footer}">
            
            <description>
                <xsl:apply-templates select="api-answers/answer[@id='arch-what']/node()" mode="description"/>
            </description>

            <xsl:variable name="deploy-dependencies" select="api-answers/answer[@id='deploy-dependencies']"/>
            <xsl:if test="$deploy-dependencies">
                <deploy-dependencies>
                    <xsl:apply-templates select="$deploy-dependencies/node()"/>
                </deploy-dependencies>
            </xsl:if>
            
            <xsl:variable name="arch-usecases" select="api-answers/answer[@id='arch-usecases']"/>
            <xsl:if test="$arch-usecases">
                <arch-usecases>
                    <xsl:apply-templates select="$arch-usecases/node()"/>
                </arch-usecases>
            </xsl:if>            

            <xsl:for-each select="$interfaces">
                <xsl:call-template name="api" >
                    <xsl:with-param name="group" select="@group" />
                    <xsl:with-param name="type" select="@type" />
                </xsl:call-template>
            </xsl:for-each>

        </module>
    </xsl:template>

    <xsl:template name="api">
        <xsl:param name="group" />
        <xsl:param name="type" />
    
        <xsl:variable name="name" select="@name" />
        <xsl:variable name="category" select="@category" />
        <xsl:variable name="url" select="@url" />

        <api name="{$name}" type="{$type}" category="{$category}">
          <xsl:if test="string-length($url)>0"><xsl:attribute name="url"><xsl:value-of select="$url" /></xsl:attribute>
          </xsl:if>
          <xsl:choose >
            <xsl:when test="$group"><xsl:attribute name="group"><xsl:value-of select="$group" /></xsl:attribute></xsl:when>
            <xsl:otherwise><xsl:attribute name="group">java</xsl:attribute></xsl:otherwise>
          </xsl:choose>

          <xsl:apply-templates />
        </api>
    </xsl:template>
     
    <xsl:template match="api" mode="description">
        <api-ref name="{@name}"/>
    </xsl:template>  
    
    <!-- Format random HTML elements as is: -->
    <xsl:template match="@*|node()">
       <xsl:copy  >
          <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
    </xsl:template>
  
</xsl:stylesheet> 
