<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : api-questions-to-html.xsl
    Created on : November 4, 2002, 4:51 PM
    Author     : jarda
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>
    
    <xsl:param name="arch.stylesheet"/>
    <xsl:param name="arch.overviewlink"/>
    <xsl:param name="arch.footer"/>
    <xsl:param name="arch.target"/>

    <xsl:template match="/">
        <xsl:variable name="interfaces" select="//api[@type='export']" />
        <xsl:variable name="all_properties" select="//property" />

        <module>
            <xsl:attribute name="name"><xsl:value-of select="api-answers/@module" /></xsl:attribute>
            <xsl:attribute name="target"><xsl:value-of select="$arch.target" /></xsl:attribute>
            <xsl:attribute name="stylesheet"><xsl:value-of select="$arch.stylesheet" /></xsl:attribute>
            <xsl:attribute name="overviewlink"><xsl:value-of select="$arch.overviewlink" /></xsl:attribute>
            <xsl:attribute name="footer"><xsl:value-of select="$arch.footer" /></xsl:attribute>
            
            <description>
                <xsl:apply-templates select="api-answers/answer[@id='arch-what']/node()"/>
            </description>

            <xsl:for-each select="$interfaces">
                <xsl:call-template name="api" >
                    <xsl:with-param name="group" select="@group" />
                    <xsl:with-param name="type" select="@type" />
                </xsl:call-template>
            </xsl:for-each>
            <xsl:for-each select="$all_properties">
                <xsl:call-template name="api" >
                    <xsl:with-param name="group">property</xsl:with-param>
                    <xsl:with-param name="type">export</xsl:with-param>
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

        <api>
          <xsl:attribute name="name"><xsl:value-of select="$name" /></xsl:attribute>
          <xsl:attribute name="type"><xsl:value-of select="$type" /></xsl:attribute>
          <xsl:attribute name="category"><xsl:value-of select="$category" /></xsl:attribute>
          <xsl:if test="string-length($url)>0"><xsl:attribute name="url"><xsl:value-of select="$url" /></xsl:attribute>
          </xsl:if>
          <xsl:choose >
            <xsl:when test="$group"><xsl:attribute name="group"><xsl:value-of select="$group" /></xsl:attribute></xsl:when>
            <xsl:otherwise><xsl:attribute name="group">java</xsl:attribute></xsl:otherwise>
          </xsl:choose>

          <xsl:apply-templates />
        </api>
    </xsl:template>
     
    <xsl:template match="api">
        <api-ref>
            <xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
        </api-ref>
    </xsl:template>  
    
    <!-- Format random HTML elements as is: -->
    <xsl:template match="@*|node()">
       <xsl:copy  >
          <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
    </xsl:template>
  
</xsl:stylesheet> 
