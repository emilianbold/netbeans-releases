<?xml version="1.0"?>
<!--
                 Sun Public License Notice
 
 The contents of this file are subject to the Sun Public License
 Version 1.0 (the "License"). You may not use this file except in
 compliance with the License. A copy of the License is available at
 http://www.sun.com/
 
 The Original Code is NetBeans. The Initial Developer of the Original
 Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 Microsystems, Inc. All Rights Reserved.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="library.xsl"/>

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">Summary from <xsl:value-of select="/XTestResultsReport/SystemInfo/@host"/> run at <xsl:value-of select="/XTestResultsReport/@timeStamp"/></xsl:with-param>
	</xsl:call-template>
</xsl:template>

<xsl:template match="XTestResultsReport">
	<H1>XTest Results Report</H1>
	<BLOCKQUOTE>
		<H2>run on <xsl:value-of select="SystemInfo/@host"/> at <xsl:value-of select="@timeStamp"/></H2>
	</BLOCKQUOTE>
	<H2>Summary</H2>
	<xsl:call-template name="summary-table"/>	
	<UL>
		<LI><A HREF="systeminfo.html">System Info</A></LI>
		<xsl:if test="@project">
			<LI>Tested project: <xsl:value-of select="@project"/></LI>
		</xsl:if>
		<xsl:if test="@build">
			<LI>Tested build: <xsl:value-of select="@build"/></LI>
		</xsl:if>		
	</UL>
	<HR/>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="TestRun">
	<A NAME="{@runID}"><H2>Test Run</H2></A>
	<UL>
		<xsl:if test="@name">
			<LI>Name:<xsl:value-of select="@name"/></LI>
		</xsl:if>
		<xsl:if test="@config">
			<LI>Config:<xsl:value-of select="@config"/></LI>
		</xsl:if>
		<xsl:for-each select="TestBag[@unexpectedFailure]">
			<LI><B><FONT color="#FF0000">
				!!! Tests did not finish correctly in module <xsl:value-of select="@module"/>, 
				 in testbag <A HREF="../{parent::*/@runID}/{@bagID}/htmlresults/testbag.html"><xsl:value-of select="@name"/></A> !!!
			</FONT></B></LI>
		</xsl:for-each>

	</UL>
	<xsl:call-template name="summary-table">
			<xsl:with-param name="table-width">95%</xsl:with-param>
	</xsl:call-template>
	<!--
	<H4>Runned TestBags:</H4>
	-->
	<BLOCKQUOTE>		
		<xsl:apply-templates select="TestBag[not(./@module = preceding-sibling::TestBag/@module)]">
			<xsl:sort select="@module"/>
		</xsl:apply-templates>
		
		<!--		
		<xsl:for-each select = "TestBag">		
		<xsl:sort data-type="text" select="@module"/>
		<xsl:variable name="current-module" select="@module"/>		
		<xsl:variable name="current-bagID" select="@bagID"/>
		<xsl:variable name="first-occurance-of-module-bagID" select="//TestBag[@module=$current-module]/@bagID"/>
		
		bagID = <xsl:value-of select="$current-bagID"/><BR/>
		module = <xsl:value-of select="$current-module"/><BR/>
		first occcurance of this module = <xsl:value-of select="$first-occurance-of-module-bagID"/><BR/>
		
		<xsl:if test="string($current-bagID)=string($first-occurance-of-module-bagID)">			
			Module = <xsl:value-of select="$current-module"/>
			<TABLE width="90%" cellspacing="2" cellpadding="5" border="0">
				<xsl:call-template name="testbag-summary-header"/>
				<xsl:for-each select = "//TestBag">
					<xsl:sort data-type="text" select="@name"/>						
						<xsl:call-template name="testbag-summary-row">							
							<xsl:with-param name="current-module"><xsl:value-of select="$current-module"/></xsl:with-param>
						</xsl:call-template>
				</xsl:for-each>		
			</TABLE>	
		</xsl:if>
		</xsl:for-each>
		-->
	</BLOCKQUOTE>
</xsl:template>

<xsl:template match="TestBag">
	<xsl:variable name="current-module" select="@module"/>		
	<A NAME="{parent::*/@runID}-{@module}"><H3>Module: <xsl:value-of select="@module"/></H3></A>
	
	<TABLE width="90%" cellspacing="2" cellpadding="5" border="0">
	<xsl:call-template name="summary-header"/>
	<xsl:call-template name="summary-row">
			<xsl:with-param name="table-width">90%</xsl:with-param>
			<xsl:with-param name="testsTotal" select="sum(parent::*/TestBag[@module=$current-module]/@testsTotal)"/>
			<xsl:with-param name="testsPass" select="sum(parent::*/TestBag[@module=$current-module]/@testsPass)"/>
			<xsl:with-param name="testsFail" select="sum(parent::*/TestBag[@module=$current-module]/@testsFail)"/>
			<xsl:with-param name="testsError" select="sum(parent::*/TestBag[@module=$current-module]/@testsError)"/>
			<xsl:with-param name="time" select="sum(parent::*/TestBag[@module=$current-module]/@time)"/>			
	</xsl:call-template>
	</TABLE>
	<TABLE width="90%" cellspacing="2" cellpadding="5" border="0">
		<xsl:call-template name="testbag-summary-header"/>
		<xsl:for-each select = "parent::*/TestBag[@module=$current-module]">
			<xsl:sort data-type="text" select="@name"/>
				<xsl:call-template name="testbag-summary-row"/>
		</xsl:for-each>		
	</TABLE>	
</xsl:template>

</xsl:stylesheet>