<?xml version="1.0" encoding="UTF-8" ?>
<!--
                Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
Microsystems, Inc. All Rights Reserved.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" />
    <xsl:param name="code.name.base" />
    
    
    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="//api[@type='export' and @group='java' and @category='official']" >
                <xsl:call-template name="print-properties" >
                    <xsl:with-param name="api" select="//api[@type='export' and @group='java' and @category='official']" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="//api[@type='export' and @group='java' and @category='stable']" >
                <xsl:call-template name="print-properties" >
                    <xsl:with-param name="api" select="//api[@type='export' and @group='java' and @category='stable']" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="//api[@type='export' and @group='java' and @category='devel']" >
                <xsl:call-template name="print-properties" >
                    <xsl:with-param name="api" select="//api[@type='export' and @group='java' and @category='devel']" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="//api[@type='export' and @group='java' and @category='friend']" >
                <xsl:call-template name="print-properties" >
                    <xsl:with-param name="api" select="//api[@type='export' and @group='java' and @category='friend']" />
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="print-properties" >
        <xsl:param name="api" />
        
        <xsl:text>arch.</xsl:text>
        <xsl:value-of select="translate($code.name.base,'-','.')"/>
        <xsl:text>.name=</xsl:text>
        <xsl:value-of select="$api/@name"/>
        <xsl:text>
</xsl:text>

        <xsl:text>arch.</xsl:text>
        <xsl:value-of select="translate($code.name.base,'-','.')"/>
        <xsl:text>.category=</xsl:text>
        <xsl:value-of select="$api/@category"/>
        <xsl:text>
</xsl:text>
    </xsl:template>
</xsl:stylesheet> 
