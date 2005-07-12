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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="apichanges.xsl" />
    
    <xsl:output method="xml" omit-xml-declaration="yes"/>

    <!-- Sep 1, 1997 is the start of the NetBeans epoch  -->
    <xsl:param name="changes-since-year" select="'1997'" />
    <xsl:param name="changes-since-month" select="'09'" />
    <xsl:param name="changes-since-day" select="'01'" />
    <!-- relative path to the api changes document -->
    <xsl:param name="changes-since-url" select="'.'" />
    <!-- amount of changes to print -->
    <xsl:param name="changes-since-amount" select="'65535'" />

    <!-- Main document structure: -->
    <xsl:template match="/" name="api-changes" >
        <!-- amount of changes to print -->
        <xsl:param name="changes-since-amount" select="$changes-since-amount" />
         <xsl:text>

         
</xsl:text>
        <xsl:comment>Search for dates that are later or equal to <xsl:value-of select="$changes-since-year" 
          />-<xsl:value-of select="$changes-since-month" />-<xsl:value-of select="$changes-since-day" /> in
          <xsl:value-of select="$changes-since-url" />
        </xsl:comment>
        <xsl:apply-templates select="//change" mode="changes-since" >
            <xsl:with-param name="changes-since-amount" select="$changes-since-amount" />
            <xsl:sort data-type="number" order="descending" select="date/@year"/>
            <xsl:sort data-type="number" order="descending" select="date/@month"/>
            <xsl:sort data-type="number" order="descending" select="date/@day"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- Summarizing links to changes: -->
    <xsl:template match="change" mode="changes-since" >
        <xsl:param name="changes-since-amount" select="$changes-since-amount" />
        
        <xsl:variable name="day" select="date/@day" />
        <xsl:variable name="month" select="date/@month" />
        <xsl:variable name="year" select="date/@year" />

        <xsl:variable name="number-of-newer" select="count(
            //change[ 
              (number(date/@year) > number($year)) or
              (number(date/@year) = number($year) and number(date/@month) > number($month)) or
              (number(date/@year) = number($year) and number(date/@month) = number($month) and number(date/@day) > number($day))
            ]
        )" />
         
         <xsl:text>
</xsl:text>
        <xsl:comment>Checking date <xsl:value-of select="$year" 
          />-<xsl:value-of select="$month" />-<xsl:value-of select="$day" 
          /> with count of newer <xsl:value-of select="$number-of-newer" />
        </xsl:comment>
       <xsl:choose>
            <xsl:when test="number($number-of-newer) >= number($changes-since-amount)" >
                <xsl:comment>Skipped as the amount of changes is too big</xsl:comment>
            </xsl:when>
            <xsl:when test="number(date/@year) > number($changes-since-year)">
                <xsl:comment>year ok</xsl:comment>
                <xsl:call-template name="print-change" />
            </xsl:when>
            <xsl:when test="number($changes-since-year) = number(date/@year)">
                <xsl:comment>year equal</xsl:comment>
                <xsl:choose>
                    <xsl:when test="number(date/@month) > number($changes-since-month)">
                        <xsl:comment>month ok</xsl:comment>
                        <xsl:call-template name="print-change" />
                    </xsl:when>
                    <xsl:when test="number($changes-since-month) = number(date/@month)">
                        <xsl:comment>month equal</xsl:comment>
                        <xsl:if test="number(date/@day) >= number($changes-since-day) ">
                            <xsl:comment>day ok</xsl:comment>
                            <xsl:call-template name="print-change" />
                        </xsl:if>
                    </xsl:when>
                 </xsl:choose>
            </xsl:when>
         </xsl:choose>
         
    </xsl:template>
        
    <xsl:template name="print-change" >
        <xsl:text>
</xsl:text>
        <change>
            <xsl:attribute name="id"><xsl:call-template name="change-id"/></xsl:attribute>
            <xsl:attribute name="url"><xsl:value-of select="$changes-since-url" /></xsl:attribute>
            <xsl:copy-of select="*" />
        </change>
    </xsl:template>

</xsl:stylesheet>
