<?xml version="1.0"?>
<!DOCTYPE DOCUMENT [
	<!ENTITY __long_underline__  "---------------------------------------------------------------------">
	<!ENTITY __short_underline__ "---------">
]>
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
<xsl:output method="text"/>
<xsl:param name="onlyFailures">true</xsl:param>


<xsl:key name="module" match="TestBag" use="concat(@module,parent::*/@runID)"/>

<xsl:template match="XTestResultsReport">XTest Results Report:
run on <xsl:value-of select="SystemInfo/@host"/> at <xsl:value-of select="@timeStamp"/>

<xsl:if test="@project">
Tested project: <xsl:value-of select="@project"/>
</xsl:if>
<xsl:if test="@build">
Tested build: <xsl:value-of select="@build"/>
</xsl:if>


<xsl:apply-templates select="SystemInfo"/>


<xsl:if test="count(TestRun) &gt; 1">
&__long_underline__;
Summary:
Total Tests       : <xsl:value-of select="@testsTotal"/>
Expected Passes   : <xsl:value-of select="@testsPass - @testsUnexpectedPass"/>
Unexpected Passes : <xsl:value-of select="@testsUnexpectedPass"/>
Expected Fails    : <xsl:value-of select="@testsExpectedFail"/>
Unexpected Fails  : <xsl:value-of select="@testsFail - @testsExpectedFail"/>
Errors            : <xsl:value-of select="@testsError"/>
Success Rate      : <xsl:value-of select="format-number(@testsPass div @testsTotal,'0.00%')"/>
Run (when)        : <xsl:value-of select="@timeStamp"/>
Time (h:mm:ss)    : <xsl:call-template name="timeFormatterHMS">
				        <xsl:with-param name="time"><xsl:value-of select="(@time div 1000)"/></xsl:with-param>
			        </xsl:call-template>
</xsl:if>


<xsl:apply-templates select="TestRun">
	<xsl:sort select="TestRun/@timeStamp" order="descending"/>
</xsl:apply-templates>

-- end of report -- 	
 	
</xsl:template>



<xsl:template match="SystemInfo">

Host:             : <xsl:value-of select="@host"/>
Operating System  : <xsl:value-of select="@osName"/><xsl:text> </xsl:text><xsl:value-of select="@osVersion"/><xsl:text> </xsl:text><xsl:value-of select="@osArch"/>
Java Version      : <xsl:value-of select="@javaVersion"/>
Java Vendor       : <xsl:value-of select="@javaVendor"/>
User Language     : <xsl:value-of select="@userLanguage"/>
</xsl:template>



<xsl:template match="TestRun">
	<xsl:variable name="currentRunID" select="@runID"/>
	
&__long_underline__;
<!--
	<xsl:sort select="@timeStamp" order="descending"/>
-->
TestRun           <xsl:if test="@name">: <xsl:value-of select="@name"/></xsl:if>

<xsl:if test="@config">
Config            : <xsl:value-of select="@config"/>
</xsl:if>			        
Total Tests       : <xsl:value-of select="@testsTotal"/>
Expected Passes   : <xsl:value-of select="@testsPass - @testsUnexpectedPass"/>
Unexpected Passes : <xsl:value-of select="@testsUnexpectedPass"/>
Expected Fails    : <xsl:value-of select="@testsExpectedFail"/>
Unexpected Fails  : <xsl:value-of select="@testsFail - @testsExpectedFail"/>
Errors            : <xsl:value-of select="@testsError"/>
Success Rate      : <xsl:value-of select="format-number(@testsPass div @testsTotal,'0.00%')"/>
Run (when)        : <xsl:value-of select="@timeStamp"/>
Time (h:mm:ss)    : <xsl:call-template name="timeFormatterHMS">
				        <xsl:with-param name="time"><xsl:value-of select="(@time div 1000)"/></xsl:with-param>
			        </xsl:call-template>

<xsl:for-each select="TestBag[@unexpectedFailure]">
!!! Tests did not finish correctly in module <xsl:value-of select="@module"/>, in testbag <xsl:value-of select="@name"/> !!!
</xsl:for-each>

&__long_underline__;
Tests Details
&__long_underline__;	
	
### Unexpected Failures ###
<xsl:call-template name="test-details">
	<xsl:with-param name="test-result">fail</xsl:with-param>
	<xsl:with-param name="currentRunID" select="$currentRunID"/>
</xsl:call-template>


### Unexpected Passes ###
<xsl:call-template name="test-details">
	<xsl:with-param name="test-result">unexpected pass</xsl:with-param>
	<xsl:with-param name="currentRunID" select="$currentRunID"/>
</xsl:call-template>


### Errors ###
<xsl:call-template name="test-details">
	<xsl:with-param name="test-result">error</xsl:with-param>
	<xsl:with-param name="currentRunID" select="$currentRunID"/>
</xsl:call-template>


### Expected Failures ###
<xsl:call-template name="test-details">
	<xsl:with-param name="test-result">expected fail</xsl:with-param>
	<xsl:with-param name="currentRunID" select="$currentRunID"/>
</xsl:call-template>

<xsl:if test="$onlyFailures='false'">


### Passes ###
<xsl:call-template name="test-details">
	<xsl:with-param name="test-result">pass</xsl:with-param>
	<xsl:with-param name="currentRunID" select="$currentRunID"/>
</xsl:call-template>	
</xsl:if>
	

</xsl:template>

<xsl:template name="test-details">
	<xsl:param name="test-result"/>
	<xsl:param name="currentRunID"/>		
<xsl:variable name="uniqueModule" select="TestBag[generate-id(.)=generate-id(key('module',concat(./@module,$currentRunID))[1])]"/>	
<xsl:for-each select="$uniqueModule">
	<xsl:sort select="@module"/>	
<xsl:variable name="currentModule" select="@module"/>
<xsl:variable name="testCasesWithResult" select="parent::*/TestBag[@module=$currentModule]/UnitTestSuite/UnitTestCase[@result=$test-result]"/>
<xsl:if test="count($testCasesWithResult) &gt; 0">
<xsl:text>&#xA;</xsl:text>
<xsl:value-of select="$currentModule"/>
&__short_underline__;
<xsl:for-each select="$testCasesWithResult">
<xsl:value-of select="parent::*/@name"/>/<xsl:value-of select="@name"/>
<xsl:if test="not(parent::*/@name = @class)"> (test is located in class <xsl:value-of select="@class"/>)</xsl:if>
<xsl:if test="@message"> - message = '<xsl:value-of select="@message"/>'</xsl:if><xsl:text>&#xA;</xsl:text>
</xsl:for-each>

</xsl:if>
</xsl:for-each>	
	
</xsl:template>
<xsl:template name="timeFormatterHMS">
	<xsl:param name="time"/>
	<xsl:if test="number($time) != 'NaN'">
		<xsl:variable name="hours" select="floor($time div 3600)"/>
		<xsl:variable name="minutes" select="floor(($time - ( $hours * 3600) ) div 60)"/>
		<xsl:variable name="seconds" select="floor(($time - ($hours * 3600) - ($minutes * 60)))"/>
		<xsl:value-of select="$hours"/>:<xsl:value-of select="format-number($minutes,'00')"/>:<xsl:value-of select="format-number($seconds,'00')"/>
	</xsl:if>
</xsl:template>



</xsl:stylesheet>