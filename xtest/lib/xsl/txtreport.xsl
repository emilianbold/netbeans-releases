<?xml version="1.0"?>
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

<xsl:include href="library.xsl"/>

<xsl:key name="module" match="TestBag" use="concat(@module,parent::*/@runID)"/>

<xsl:param name="truncated"/>
<xsl:param name="mappedHostname"/>

<xsl:template match="XTestResultsReport">
XTest Results Report run on <xsl:choose>
				<xsl:when test="not($mappedHostname)">
					<xsl:value-of select="SystemInfo/@host"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$mappedHostname"/>
				</xsl:otherwise>
			    </xsl:choose> at <xsl:value-of select="@timeStamp"/>
	
Summary
=======
<xsl:if test="@project">Tested project:   <xsl:value-of select="@project"/></xsl:if>

<xsl:if test="@build">Tested build:     <xsl:value-of select="@build"/></xsl:if>
Total Tests:      <xsl:value-of select="@testsTotal"/>	Success Rate:      <xsl:value-of select="format-number(@testsPass div @testsTotal,'0.00%')"/>
Expected Passes:  <xsl:value-of select="@testsPass - @testsUnexpectedPass"/>	Unexpected Passes: <xsl:value-of select="@testsUnexpectedPass"/>
Expected Fails:   <xsl:value-of select="@testsExpectedFail"/>	Unexpected Fails:  <xsl:value-of select="@testsFail - @testsExpectedFail"/>
Errors:           <xsl:value-of select="@testsError"/>			
Run (when):       <xsl:value-of select="@timeStamp"/>
Time (h:mm:ss):   <xsl:call-template name="timeFormatterHMS">
			<xsl:with-param name="time"><xsl:value-of select="(@time div 1000)"/></xsl:with-param>
		   </xsl:call-template>
<xsl:apply-templates/>
</xsl:template>

<!-- testrun details -->
<xsl:template match="TestRun">
<xsl:variable name="currentRunID" select="@runID"/>

Testrun
-------
<xsl:if test="@name">Name:             <xsl:value-of select="@name"/></xsl:if>
Total Tests:      <xsl:value-of select="@testsTotal"/>	Success Rate:      <xsl:value-of select="format-number(@testsPass div @testsTotal,'0.00%')"/>
Expected Passes:  <xsl:value-of select="@testsPass - @testsUnexpectedPass"/>	Unexpected Passes: <xsl:value-of select="@testsUnexpectedPass"/>
Expected Fails:   <xsl:value-of select="@testsExpectedFail"/>	Unexpected Fails:  <xsl:value-of select="@testsFail - @testsExpectedFail"/>
Errors:           <xsl:value-of select="@testsError"/>	
Run (when):       <xsl:value-of select="@timeStamp"/>
Time (h:mm:ss):   <xsl:call-template name="timeFormatterHMS">
		      <xsl:with-param name="time"><xsl:value-of select="(@time div 1000)"/></xsl:with-param>
		   </xsl:call-template>

<xsl:variable name="uniqueModule" select="TestBag[generate-id(.)=generate-id(key('module',concat(./@module,$currentRunID))[1])]"/>

<xsl:for-each select="$uniqueModule">
<xsl:sort select="@module"/>
<xsl:variable name="currentModule" select="@module"/>
<xsl:variable name="testsPass" select="sum(parent::*/TestBag[@module=$currentModule]/@testsPass)"/>
<xsl:variable name="testsUnexpectedPass" select="sum(parent::*/TestBag[@module=$currentModule]/@testsUnexpectedPass)"/>
<xsl:variable name="testsFail" select="sum(parent::*/TestBag[@module=$currentModule]/@testsFail)"/>
<xsl:variable name="testsExpectedFail" select="sum(parent::*/TestBag[@module=$currentModule]/@testsExpectedFail)"/>
<xsl:variable name="testsTotal" select="sum(parent::*/TestBag[@module=$currentModule]/@testsTotal)"/>

   Module: <xsl:value-of select="@module"/>
   Total Tests:      <xsl:value-of select="$testsTotal"/>	Success Rate:      <xsl:value-of select="format-number($testsPass div $testsTotal,'0.00%')"/>
   Expected Passes:  <xsl:value-of select="$testsPass - $testsUnexpectedPass"/>	Unexpected Passes: <xsl:value-of select="$testsUnexpectedPass"/>
   Expected Fails:   <xsl:value-of select="$testsExpectedFail"/>	Unexpected Fails:  <xsl:value-of select="$testsFail - $testsExpectedFail"/>
   Errors:           <xsl:value-of select="sum(parent::*/TestBag[@module=$currentModule]/@testsError)"/>
   Time (h:mm:ss):   <xsl:call-template name="timeFormatterHMS">
			   <xsl:with-param name="time"><xsl:value-of select="(sum(parent::*/TestBag[@module=$currentModule]/@time) div 1000)"/></xsl:with-param>
		      </xsl:call-template>

</xsl:for-each>
</xsl:template>

	

</xsl:stylesheet>