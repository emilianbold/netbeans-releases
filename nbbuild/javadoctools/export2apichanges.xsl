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
    <xsl:import href="apichanges.xsl" />
    
    <xsl:output method="html"/>
    <xsl:param name="date" />
    <xsl:param name="changes-since-year"  />
    <xsl:param name="changes-since-month"  />
    <xsl:param name="changes-since-day"  />
    <xsl:param name="include-introduction" select="'true'" />
    <xsl:param name="url-prefix" select="''" />

    <xsl:template match="/" >
      <xsl:choose>
        <xsl:when test="$include-introduction='true'" >
            <html>
            <head>
                <!-- projects.netbeans.org -->
               <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
               <title>NetBeans API Changes since Last Release</title>
                <link rel="stylesheet" href="netbeans.css" type="text/css"/>

              <link REL="icon" href="http://www.netbeans.org/favicon.ico" type="image/ico" />
              <link REL="shortcut icon" href="http://www.netbeans.org/favicon.ico" />

            </head>

            <body>


            <center>
                <h1>NetBeans API Changes since Last Release</h1>
                <h3>Current Development Version</h3>
                <xsl:if test="$date" >
                    <xsl:value-of select="$date" />
                    <p/>
                </xsl:if>
            </center>

            This document highlights changes in <a href="index.html">NetBeans APIs</a> 
            since previous version (e.g. since
                <xsl:value-of select="$changes-since-day" />
                <xsl:text> </xsl:text>
                <xsl:choose>
                    <xsl:when test="$changes-since-month=1">Jan</xsl:when>
                    <xsl:when test="$changes-since-month=2">Feb</xsl:when>
                    <xsl:when test="$changes-since-month=3">Mar</xsl:when>
                    <xsl:when test="$changes-since-month=4">Apr</xsl:when>
                    <xsl:when test="$changes-since-month=5">May</xsl:when>
                    <xsl:when test="$changes-since-month=6">Jun</xsl:when>
                    <xsl:when test="$changes-since-month=7">Jul</xsl:when>
                    <xsl:when test="$changes-since-month=8">Aug</xsl:when>
                    <xsl:when test="$changes-since-month=9">Sep</xsl:when>
                    <xsl:when test="$changes-since-month=10">Oct</xsl:when>
                    <xsl:when test="$changes-since-month=11">Nov</xsl:when>
                    <xsl:when test="$changes-since-month=12">Dec</xsl:when>
                </xsl:choose> 
                <xsl:text> </xsl:text>
                <xsl:value-of select="$changes-since-year" /> 
                <xsl:text>):</xsl:text>
            <xsl:call-template name="do-the-table" />
            </body>
            </html>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="do-the-table" />
        </xsl:otherwise>
      </xsl:choose>
      
    </xsl:template>
    
    <xsl:template name="do-the-table" >
        <ul>
            <xsl:apply-templates select="//change" mode="global-overview">
                <xsl:sort data-type="number" order="descending" select="date/@year"/>
                <xsl:sort data-type="number" order="descending" select="date/@month"/>
                <xsl:sort data-type="number" order="descending" select="date/@day"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="change" mode="global-overview">
        <li>
            <xsl:if test="date">(<xsl:apply-templates select="date"/>)<xsl:text> </xsl:text></xsl:if>
            <a><xsl:attribute name="href"><xsl:value-of select="$url-prefix"/><xsl:value-of select="@url"/>#<xsl:value-of select="@id"/></xsl:attribute>
                <xsl:value-of select="substring-before(@url,'/')"/>
            </a>: <xsl:apply-templates select="summary/node()"/>
        </li>
    </xsl:template>
    
</xsl:stylesheet>


