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

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">Summary from <xsl:value-of select="/XTestResultsReport/SystemInfo/@host"/> run at <xsl:value-of select="/XTestResultsReport/@timeStamp"/></xsl:with-param>
	</xsl:call-template>
</xsl:template>




<xsl:template match="XTestResultsReport">
	<H1>XTest Results Report</H1>
	<BLOCKQUOTE>
		<H2>
			run on <xsl:value-of select="@host"/>
			at <xsl:value-of select="@timeStamp"/>
		</H2>
	</BLOCKQUOTE>
	<UL>
		<xsl:if test="@project">
			<LI>Tested project: <xsl:value-of select="@project"/></LI>
		</xsl:if>
		<xsl:if test="@build">
			<LI>Tested build: <xsl:value-of select="@build"/></LI>
		</xsl:if>
		<!--
		<LI><A HREF="systeminfo.html">System Info</A></LI>
		-->
		
	</UL>
        <hr/>
	
	<table border="0">
	<tr>
	<xsl:for-each select="TestRun">
	<td valign="top">
		<h3>
		<xsl:if test="@name"><xsl:value-of select="@name"/></xsl:if>
		<xsl:if test="not(@name)">Test Run</xsl:if>
		, run at <xsl:value-of select="@timeStamp"/>
		</h3>

	        <table border="1">
	        <tr><td align="center"><b>Module/Testbag/Suite/PerfData</b></td><td align="center"><b>Value</b></td></tr>
		<xsl:for-each select="TestBag[not(./@module = preceding-sibling::TestBag/@module)]">
			<xsl:sort select="@module"/>
			<tr><td><xsl:value-of select="@module"/></td><td>&#160;</td></tr>

			<xsl:variable name="currentModule" select="@module"/>	
			
			<xsl:for-each select = "parent::*/TestBag[@module=$currentModule]">
				<xsl:sort data-type="text" select="@name"/>
				        <tr><td>&#160;&#160;&#160;<xsl:value-of select="@name"/>
				        </td><td>&#160;</td></tr>
  			   		<xsl:for-each select="UnitTestSuite">
  			   		      <tr><td>&#160;&#160;&#160;&#160;&#160;&#160;<xsl:value-of select="@name"/>
			   		      </td><td>&#160;</td></tr>

			   		      <xsl:for-each select="Data">
			   		        <xsl:for-each select="PerformanceData">
  			   		        <tr><td>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;<xsl:value-of select="@name"/>
			   		        </td><td align="right"><xsl:value-of select="@value"/>&#160;<xsl:value-of select="@unit"/></td></tr>
			   		        </xsl:for-each>
			   	              </xsl:for-each>
			   	        </xsl:for-each>
			</xsl:for-each>	
			
		</xsl:for-each>		
		</table>
	</td>
	</xsl:for-each>		
	</tr>
	</table>
	
</xsl:template>





</xsl:stylesheet>			