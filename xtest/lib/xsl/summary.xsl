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
			run on 
				<xsl:choose>
					<xsl:when test="not($mappedHostname)">
						<xsl:value-of select="SystemInfo/@host"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$mappedHostname"/>
					</xsl:otherwise>
				</xsl:choose>	
			at <xsl:value-of select="@timeStamp"/>
		</H2>
	</BLOCKQUOTE>
	<H2>Summary</H2>
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

	<TABLE width="98%" cellspacing="2" cellpadding="5" border="0">
		<TR bgcolor="#A6CAF0" align="center" >
			<TD colspan="8"><B>Summary</B></TD>
		</TR>
		<TR valign="top" bgcolor="#A6CAF0">
			<TD><B>Test Run</B></TD>
			<TD><B>Total Tests</B></TD>
			<TD><B>Passed</B></TD>
			<TD><B>Failures</B></TD>
			<TD><B>Errors</B></TD>
			<TD><B>Success Rate</B></TD>
			<TD><B>Run (when)</B></TD>
			<TD><B>Time (s)</B></TD>
		</TR>
		<!-- summary over all test runs -->
		<TR class="pass">
			<TD><B>Summary:</B></TD>		
			<TD><B><xsl:value-of select="@testsTotal"/></B></TD>
			<TD><B><xsl:value-of select="@testsPass"/></B></TD>
			<TD><B><xsl:value-of select="@testsFail"/></B></TD>
			<TD><B><xsl:value-of select="@testsError"/></B></TD>				
			<TD><B><xsl:value-of select="format-number(@testsPass div @testsTotal,'0.00%')"/></B></TD>
			<TD><B><xsl:value-of select="@timeStamp"/></B></TD>
			<TD><B><xsl:value-of select="(@time div 1000)"/></B></TD>
		</TR>
		<TR></TR>
		<!-- testrun details -->
		<xsl:for-each select="TestRun">
			<!--
			<xsl:sort select="@timeStamp" order="descending"/>
			-->
			<TR class="pass">
				<TD>
					<A href="#{@runID}">
						<xsl:if test="@name">
							<xsl:value-of select="@name"/>
						</xsl:if>
						<xsl:if test="not(@name)">
							Test Run
						</xsl:if>
					</A>
				</TD>		
				<TD><xsl:value-of select="@testsTotal"/></TD>
				<TD><xsl:value-of select="@testsPass"/></TD>
				<TD><xsl:value-of select="@testsFail"/></TD>
				<TD><xsl:value-of select="@testsError"/></TD>				
				<TD><xsl:value-of select="format-number(@testsPass div @testsTotal,'0.00%')"/></TD>
				<TD><xsl:value-of select="@timeStamp"/></TD>
				<TD><xsl:value-of select="(@time div 1000)"/></TD>
			</TR>
		</xsl:for-each>
	</TABLE>
	<P></P>
	<HR/>
	<xsl:apply-templates select="TestRun">
		<xsl:sort select="TestRun/@timeStamp" order="descending"/>
	</xsl:apply-templates>
</xsl:template>

<xsl:template match="TestRun">
	<xsl:variable name="currentRunID" select="@runID"/>
	<A NAME="{$currentRunID}"><H2>Test Run</H2></A>
	<UL>
		<xsl:if test="@name">
			<LI>Name:<xsl:value-of select="@name"/></LI>
		</xsl:if>
		<xsl:if test="@config">
			<LI>Config:<xsl:value-of select="@config"/></LI>
		</xsl:if>
			<LI>Run (when): <xsl:value-of select="@timeStamp"/></LI>
		<xsl:if test="not(boolean($truncated))">
                   <xsl:if test="string(@antLogs)='true'">
			<LI><A HREF="../{@runID}/logs/">Logs from builds scripts</A></LI>
		   </xsl:if>
                </xsl:if>   
		<xsl:for-each select="TestBag[@unexpectedFailure]">
			<LI><B><FONT color="#FF0000">
				!!! Tests did not finish correctly in module <xsl:value-of select="@module"/>, 
				 in testbag <A HREF="../{parent::*/@runID}/{@bagID}/htmlresults/testbag.html"><xsl:value-of select="@name"/></A> !!!
			</FONT></B></LI>
		</xsl:for-each>

	</UL>
	
	<xsl:variable name="uniqueModule" select="TestBag[generate-id(.)=generate-id(key('module',concat(./@module,$currentRunID))[1])]"/>
	
	<!-- summary for the testrun -->
	<TABLE width="98%" cellspacing="2" cellpadding="5" border="0">
		<TR valign="top" bgcolor="#A6CAF0">
			<TD><B>Module</B></TD>
			<TD><B>Total Tests</B></TD>
			<TD><B>Passed</B></TD>
			<TD><B>Failed</B></TD>
			<TD><B>Error</B></TD>
			<TD><B>Success Rate</B></TD>
			<!--
			<TD><B>Run (when)</B></TD>
			-->
			<TD><B>Time (s)</B></TD>
		</TR>
		<!-- data -->
		<TR class="pass">
			<TD><B>Summary:</B></TD>
			<TD><B><xsl:value-of select="@testsTotal"/></B></TD>
			<TD><B><xsl:value-of select="@testsPass"/></B></TD>
			<TD><B><xsl:value-of select="@testsFail"/></B></TD>
			<TD><B><xsl:value-of select="@testsError"/></B></TD>				
			<TD><B><xsl:value-of select="format-number(@testsPass div @testsTotal,'0.00%')"/></B></TD>
			<!--
			<TD><xsl:value-of select="@timeStamp"/></TD>
			-->
			<TD><xsl:value-of select="(@time div 1000)"/></TD>
		</TR>
		<TR></TR>
		<xsl:for-each select="$uniqueModule">
			<xsl:sort select="@module"/>
			<xsl:variable name="currentModule" select="@module"/>
			<TR class="pass">
				<TD><A HREF="#{parent::*/@runID}-{@module}"><xsl:value-of select="@module"/></A></TD>
				<xsl:variable name="testsPass" select="sum(parent::*/TestBag[@module=$currentModule]/@testsPass)"/>
				<xsl:variable name="testsTotal" select="sum(parent::*/TestBag[@module=$currentModule]/@testsTotal)"/>
				<TD><xsl:value-of select="$testsTotal"/></TD>
				<TD><xsl:value-of select="$testsPass"/></TD>
				<TD><xsl:value-of select="sum(parent::*/TestBag[@module=$currentModule]/@testsFail)"/></TD>
				<TD><xsl:value-of select="sum(parent::*/TestBag[@module=$currentModule]/@testsError)"/></TD>
				<TD><xsl:value-of select="format-number($testsPass div $testsTotal,'0.00%')"/></TD>
				<TD><xsl:value-of select="(sum(parent::*/TestBag[@module=$currentModule]/@time) div 1000)"/></TD>
			</TR>
		</xsl:for-each>
	</TABLE>
	
	<!--
	<xsl:call-template name="summary-table">
			<xsl:with-param name="table-width">95%</xsl:with-param>
	</xsl:call-template>
	-->
	<!--
	<H4>Runned TestBags:</H4>
	-->
	<BLOCKQUOTE>		
		<xsl:apply-templates select="$uniqueModule">
			<xsl:sort select="@module"/>
		</xsl:apply-templates>
		
	</BLOCKQUOTE>
	<P><BR/></P>
</xsl:template>

<xsl:template match="TestBag">
	<xsl:variable name="currentModule" select="@module"/>		
	<A NAME="{parent::*/@runID}-{@module}"><H3>Module: <xsl:value-of select="@module"/></H3></A>
	
	<TABLE width="95%" cellspacing="2" cellpadding="5" border="0">
	  	<TR valign="top" bgcolor="#A6CAF0">
        	<TD><B>TestBag</B></TD>
        	<TD><B>Attributes</B></TD>
        	<TD><B>Test Type</B></TD>
			<TD><B>Total Tests</B></TD>
			<TD><B>Passed</B></TD>
			<TD><B>Failures</B></TD>
			<TD><B>Errors</B></TD>
			<TD><B>Success Rate</B></TD>
			<TD><B>Time (s)</B></TD>
		</TR>
		<TR class="pass">
			<TD><B>Summary:</B></TD>
			<TD align="center">-</TD>
			<TD align="center">-</TD>
			<xsl:variable name="testsPass" select="sum(parent::*/TestBag[@module=$currentModule]/@testsPass)"/>
			<xsl:variable name="testsTotal" select="sum(parent::*/TestBag[@module=$currentModule]/@testsTotal)"/>
			<TD><B><xsl:value-of select="$testsTotal"/></B></TD>
			<TD><B><xsl:value-of select="$testsPass"/></B></TD>
			<TD><B><xsl:value-of select="sum(parent::*/TestBag[@module=$currentModule]/@testsFail)"/></B></TD>
			<TD><B><xsl:value-of select="sum(parent::*/TestBag[@module=$currentModule]/@testsError)"/></B></TD>
			<TD><B><xsl:value-of select="format-number($testsPass div $testsTotal,'0.00%')"/></B></TD>
			<TD><B><xsl:value-of select="(sum(parent::*/TestBag[@module=$currentModule]/@time) div 1000)"/></B></TD>
		</TR>

		<xsl:for-each select = "parent::*/TestBag[@module=$currentModule]">
			<xsl:sort data-type="text" select="@name"/>
				<xsl:call-template name="testbag-summary-row"/>
		</xsl:for-each>		
	</TABLE>	
</xsl:template>

</xsl:stylesheet>