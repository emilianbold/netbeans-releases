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


<xsl:include href="../library.xsl"/>

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">Results of <xsl:value-of select="//ManagedReport/@testedType"/> tests for <xsl:value-of select="//ManagedReport/@project"/> 
	tested by <xsl:value-of select="//ManagedReport/@testingGroup"/></xsl:with-param>
	</xsl:call-template>
</xsl:template>



<xsl:template match="XTestWebReport">
	<xsl:call-template name="MakeBuildsSummaryTable"/>
</xsl:template>

<xsl:template name="MakeBuildsSummaryTable">
	<xsl:variable name="differentHosts" select="//ManagedReport[not(./@host = preceding-sibling::ManagedReport/@host)]"/>
	<xsl:variable name="differentPlatforms" select="//ManagedReport[not(./@host = preceding-sibling::ManagedReport/@host)]"/>
	<H2>Results of <xsl:value-of select="//ManagedReport/@testedType"/> tests for <xsl:value-of select="//ManagedReport/@project"/> 
	tested by <xsl:value-of select="//ManagedReport/@testingGroup"/></H2>	
	<BR/>
	<BR/>

	<TABLE width="90%" cellspacing="2" cellpadding="5" border="0" >		
		<TR align="center">
			<TD></TD>
			<TD colspan="{count($differentHosts)}" bgcolor="#A6CAF0">
				<B>Testing Machines</B>
			</TD>
		</TR>
		<TR align="center" valign="top">
			<TD bgcolor="#A6CAF0"><B>Build</B></TD>		
			<xsl:for-each select="$differentHosts">
				<xsl:sort select="@host"/>			
				<TD class="pass">
					<B><xsl:value-of select="@host"/></B>	
				</TD>			
			</xsl:for-each>
		</TR>	
		<xsl:for-each select="//ManagedReport[not(./@build = preceding-sibling::ManagedReport/@build)]">
			<xsl:sort select="@build" order = "descending"/>
			<xsl:variable name="currentBuild" select="@build"/>					
			<TR align="center" valign="top" class="pass">
				<TD valign="center"><B><xsl:value-of select="@build"/></B></TD>
				<xsl:for-each select="$differentHosts">
					<xsl:sort select="@host"/>			
					<xsl:variable name="currentHost" select="@host"/>
					<TD>
						<xsl:variable name="expression" select="//ManagedReport[(@host=$currentHost)and(@build=$currentBuild)]"/>					
						<xsl:for-each select="$expression">										
							<A HREF="{@webLink}">
								Q:<xsl:value-of select="format-number(@testsPass div @testsTotal,'0.00%')"/>
								T:<xsl:value-of select="@testsTotal"/><BR/>	
								D:<xsl:value-of select="@timeStamp"/>
								<xsl:if test="@comment">
									C:<xsl:value-of select="@comment"/>
								</xsl:if>
							</A>
							<xsl:if test="count($expression) &gt; 1">
								<BR/>
							</xsl:if>
						</xsl:for-each>								
						<xsl:if test="count($expression) = 0">
							-
						</xsl:if>
					</TD>			
			</xsl:for-each>					
			</TR>
		</xsl:for-each>
		
	</TABLE>
	
	<P>
	<H5>Legend:</H5>
	<UL>
		<LI>Q: - quality of the product - (passed tests)/(total tests) in %</LI>
		<LI>T: - total number of run tests</LI>
		<LI>D: - date and time when the tests were run</LI>
	</UL>
	<BR/>
	</P>
	
	<HR width="90%"/>
	<H5>Hosts details:</H5>
	<UL>
		<xsl:for-each select="$differentHosts">
			<xsl:sort select="@host"/>
			<LI>
				<B><xsl:value-of select="@host"/>:</B>
					<xsl:value-of select="@osName"/>,
					<xsl:value-of select="@osVersion"/>,
				    <xsl:value-of select="@osArch"/>
			</LI>
		</xsl:for-each>
	</UL>
</xsl:template>


</xsl:stylesheet>