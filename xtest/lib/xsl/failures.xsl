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

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">Test Failures Report from <xsl:value-of select="document('testreport.xml',/*)/XTestResultsReport/SystemInfo/@host"/> run at <xsl:value-of select="/XTestResultsReport/@timeStamp"/></xsl:with-param>
	</xsl:call-template>
</xsl:template>

<xsl:template match="XTestResultsReport">
	<H1>XTest Failures Report</H1>
	<BLOCKQUOTE>
		<H2>run on <xsl:value-of select="document('testreport.xml',/*)/XTestResultsReport/SystemInfo/@host"/> at <xsl:value-of select="@timeStamp"/></H2>
	</BLOCKQUOTE>
	<UL>
		<xsl:if test="@project">
			<LI>Tested project: <xsl:value-of select="@project"/></LI>
		</xsl:if>
		<xsl:if test="@build">
			<LI>Tested build: <xsl:value-of select="@build"/></LI>
		</xsl:if>	
	</UL>
	<HR/>	
	<xsl:if test="count(//TestRun)=0">
		<BLOCKQUOTE>
			<H3>No failures/errors encountered</H3>
		</BLOCKQUOTE>
	</xsl:if>
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
			<LI>Run (when): <xsl:value-of select="@timeStamp"/></LI>
		<xsl:for-each select="TestBag[@unexpectedFailure]">
			<LI><B><FONT color="#FF0000">
				!!! Tests did not finish correctly in module <xsl:value-of select="@module"/>, 
				 in testbag <A HREF="../{parent::*/@runID}/{@bagID}/htmlresults/testbag.html"><xsl:value-of select="@name"/></A> !!!
			</FONT></B></LI>
		</xsl:for-each>
		</UL>
    <BLOCKQUOTE>
		<xsl:call-template name="Module"/>
	</BLOCKQUOTE>
</xsl:template>

<xsl:template name="Module">
	<xsl:apply-templates select="TestBag[not(./@module = preceding-sibling::TestBag/@module)]" mode="module">
		<xsl:sort select="@module"/>
	</xsl:apply-templates>
</xsl:template>	



<xsl:template match="TestBag" mode="module">
	<A NAME="{parent::*/@runID}-{@module}"><H3>Module: <xsl:value-of select="@module"/></H3></A>
	<xsl:variable name="current-module" select="@module"/>		
	<TABLE width="95%" cellspacing="2" cellpadding="5" border="0">
		<TR valign="top" bgcolor="#A6CAF0">
			<TD width="18%"><B>Name</B></TD>				
			<TD width="7%"><B>Status</B></TD>
			<TD width="65%"><B>Message</B></TD>					
			<TD><B>TestBag</B></TD>
			<TD width="6%"><B>Test Type</B></TD>
		</TR>
		<xsl:for-each select = "parent::*/TestBag[@module=$current-module]">
			<xsl:sort data-type="text" select="@name"/>
				<xsl:apply-templates select="UnitTestSuite/*[@result!='pass']"/>	
		</xsl:for-each>		
				
	</TABLE>			
</xsl:template>

<xsl:template match="TestBag">
	<H4>TestBag: <xsl:value-of select="@name"/></H4>	
	<TABLE width="85%" cellspacing="2" cellpadding="5" border="0">
		<TR valign="top" bgcolor="#A6CAF0">
			<TD width="18%"><B>Name</B></TD>				
			<TD width="7%"><B>Status</B></TD>
			<TD width="70%"><B>Message</B></TD>					
		</TR>
			<xsl:apply-templates select="UnitTestSuite/*[@result!='pass']">		
				<!--
				<xsl:sort data-type="text" select="@name"/>				
				-->
			</xsl:apply-templates>
	</TABLE>			
</xsl:template>


<xsl:template match="UnitTestCase">
	<xsl:variable name="ParentTestBag" select="parent::*/parent::*"/>
	<xsl:variable name="ParentTestRun" select="parent::*/parent::*/parent::*"/>
	<xsl:variable name="SuiteName"><xsl:value-of select="parent::*/@name"/></xsl:variable>
	<TR valing="top">
		<xsl:attribute name="class"><xsl:value-of select="translate(@result,' ','-')"/></xsl:attribute>
		<TD>
			<A HREF="../{$ParentTestRun/@runID}/{$ParentTestBag/@bagID}/htmlresults/suites/TEST-{$SuiteName}.html#{@name}">
				<xsl:value-of select="parent::*/@name"/>.<xsl:value-of select="@name"/>
			</A>
		</TD>
		<TD><xsl:value-of select="@result"/></TD>
		<TD>
                    <xsl:if test="@failReason">
                        <xsl:value-of select="@failReason"/>: 
                    </xsl:if>
                    <xsl:value-of select="@message"/>
                </TD>
		<TD><A HREF="../{$ParentTestRun/@runID}/{$ParentTestBag/@bagID}/htmlresults/testbag.html"><xsl:value-of select="$ParentTestBag/@name"/></A></TD>
		<TD><xsl:value-of select="$ParentTestBag/@testType"/></TD>
	</TR>	
</xsl:template>


</xsl:stylesheet>