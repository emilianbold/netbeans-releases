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

<xsl:key name="platform" match="ManagedReport" use="concat(@osName,@osVersion,@osArch)"/> 

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
	<xsl:variable name="differentOSNames" select="//ManagedReport[not(@osName = preceding-sibling::ManagedReport[@osName=string(self::osName)])]"/>
	<xsl:variable name="differentOSArchs" select="//ManagedReport[not(@osArch = preceding-sibling::ManagedReport/@osArch)]"/>
	<xsl:variable name="differentOSVersions" select="//ManagedReport[not(@osVersion = preceding-sibling::ManagedReport/@osVersion)]"/>
	
	
	
	<H2>Results of <xsl:value-of select="//ManagedReport/@testedType"/> tests for <xsl:value-of select="//ManagedReport/@project"/> 
	
	tested by <xsl:value-of select="//ManagedReport/@testingGroup"/></H2>	
	<BR/>
	<BR/>

	<TABLE width="90%" cellspacing="2" cellpadding="5" border="0" >		
		<TR align="center">
			<TD bgcolor="#A6CAF0" rowspan="3"><B>Build</B></TD>		
			<TD bgcolor="#A6CAF0" rowspan="2" colspan="2"><B>Build Totals</B></TD>				
			<TD colspan="{count($differentHosts) * 3}" bgcolor="#A6CAF0">
				<B>Tested Platforms (name - version - architecture)</B>
			</TD>
		</TR>
		<TR align="center" valign="top">
			<xsl:for-each select="//ManagedReport[not(@osName = preceding-sibling::ManagedReport/@osName)]">
				<xsl:sort select="@osName"/>
				<xsl:variable name="currentOSName" select="@osName"/>
				<xsl:for-each select="//ManagedReport[(@osName=$currentOSName) and not(@osVersion = preceding-sibling::ManagedReport[@osName=$currentOSName]/@osVersion)]">
					<xsl:sort select="@osVersion"/>
					<xsl:variable name="currentOSVersion" select="@osVersion"/>
					<xsl:for-each select="//ManagedReport[(@osName=$currentOSName)and(@osVersion=$currentOSVersion) and not(@osArch = preceding-sibling::ManagedReport[(@osName=$currentOSName)and(@osVersion=$currentOSVersion)]/@osArch)]">
						<xsl:sort select="@osArch"/>			
						<TD class="pass" colspan="3">
							<B>
								<xsl:value-of select="@osName"/>
								-
								<xsl:value-of select="@osVersion"/>
								-
								<xsl:value-of select="@osArch"/>
							</B>	
						</TD>			
					</xsl:for-each>
				</xsl:for-each>
			</xsl:for-each>
		</TR>
		<TR align="center" valign="top">
			<TD bgcolor="#A6CAF0">
				<B>Passed</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Total</B>
			</TD>
			<xsl:for-each select="//ManagedReport[not(@osName = preceding-sibling::ManagedReport/@osName)]">
				<xsl:sort select="@osName"/>
				<xsl:variable name="currentOSName" select="@osName"/>
				<xsl:for-each select="//ManagedReport[(@osName=$currentOSName) and not(@osVersion = preceding-sibling::ManagedReport[@osName=$currentOSName]/@osVersion)]">
					<xsl:sort select="@osVersion"/>
					<xsl:variable name="currentOSVersion" select="@osVersion"/>
					<xsl:for-each select="//ManagedReport[(@osName=$currentOSName)and(@osVersion=$currentOSVersion) and not(@osArch = preceding-sibling::ManagedReport[(@osName=$currentOSName)and(@osVersion=$currentOSVersion)]/@osArch)]">
						<xsl:sort select="@osArch"/>			
						<TD class="pass">
							<B>Passed</B>	
						</TD>
						<TD class="pass">
							<B>Total</B>	
						</TD>
						<TD class="pass">
							<B>Machines</B>	
						</TD>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:for-each>
		</TR>
		
		<xsl:for-each select="//ManagedReport[not(./@build = preceding-sibling::ManagedReport/@build)]">
			<xsl:sort select="@build" order = "descending"/>
			<xsl:variable name="currentBuild" select="@build"/>
			<TR align="center" valign="center" class="pass">
				<TD>
					<B><xsl:value-of select="@build"/></B>
				</TD>
				<xsl:variable name="buildTestsPass" select="sum(//ManagedReport[$currentBuild = @build]/@testsPass)"/>
				<xsl:variable name="buildTestsTotal" select="sum(//ManagedReport[$currentBuild = @build]/@testsTotal)"/>				
				<xsl:variable name="buildQuality" select="format-number($buildTestsPass div $buildTestsTotal,'0.00%')"/>
				<TD>
					<xsl:value-of select="$buildQuality"/>
				</TD>
				<TD>
					<xsl:value-of select="$buildTestsTotal"/>					
				</TD>
			<xsl:for-each select="//ManagedReport[not(@osName = preceding-sibling::ManagedReport/@osName)]">
				<xsl:sort select="@osName"/>
				<xsl:variable name="currentOSName" select="@osName"/>
				<xsl:for-each select="//ManagedReport[(@osName=$currentOSName) and not(@osVersion = preceding-sibling::ManagedReport[@osName=$currentOSName]/@osVersion)]">
					<xsl:sort select="@osVersion"/>
					<xsl:variable name="currentOSVersion" select="@osVersion"/>
					<xsl:for-each select="//ManagedReport[(@osName=$currentOSName)and(@osVersion=$currentOSVersion) and not(@osArch = preceding-sibling::ManagedReport[(@osName=$currentOSName)and(@osVersion=$currentOSVersion)]/@osArch)]">
						<xsl:sort select="@osArch"/>
						<xsl:variable name="currentOSArch" select="@osArch"/>
										
								<xsl:variable name="expression" select="//ManagedReport[(@osName=$currentOSName)and(@osVersion=$currentOSVersion)and(@osArch=$currentOSArch)and(@build=$currentBuild)]"/>
								<xsl:if test="count($expression) &gt; 0">								
									<xsl:variable name="buildPlatformTestsPass" select="sum($expression/@testsPass)"/>
									<xsl:variable name="buildPlatformTestsTotal" select="sum($expression/@testsTotal)"/>
									<xsl:variable name="buildPlatformQuality" select="format-number($buildPlatformTestsPass div $buildPlatformTestsTotal,'0.00%')"/>
									<TD class="pass" valign="center" align="center">
										<xsl:value-of select="$buildPlatformQuality"/>
									</TD>
									<TD class="pass" valign="center" align="center">
										<xsl:value-of select="$buildPlatformTestsTotal"/>
									</TD>
									<TD class="pass" valign="center" align="center">
									<xsl:for-each select="$expression">
										<A HREF="{@webLink}">
											<xsl:value-of select="@host"/>
										</A>
										<xsl:if test="count($expression) &gt; 1">
											<BR/>
										</xsl:if>
									</xsl:for-each>
									</TD>
								</xsl:if>
								<!--
								<xsl:value-of select="count(//ManagedReport[(@osName=$currentOSName)and(@osVersion=$currentOSVersion)and(@osArch=$currentOSArch)and(@build=$currentBuild)])"/>
								-->
								<xsl:if test="count($expression) = 0">
									<!--
									<TD colspan="3">
										-
									</TD>
									-->
									<TD>-</TD>
									<TD>-</TD>
									<TD>-</TD>
								</xsl:if>
							
					</xsl:for-each>
				</xsl:for-each>
			</xsl:for-each>
				<!--
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
			-->		
			</TR>
		</xsl:for-each>
		
	</TABLE>
	
	<!--
	<P>
	<H5>Legend:</H5>
	<UL>
		<LI>Q: - quality of the product - (passed tests)/(total tests) in %</LI>
		<LI>T: - total number of run tests</LI>
		<LI>D: - date and time when the tests were run</LI>
	</UL>
	<BR/>
	</P>
	-->
	
	<HR width="90%"/>
	<H5>History Matrices for:</H5>
	<UL>
		<xsl:for-each select="$differentHosts">
			<xsl:sort select="@host"/>
			<LI>				
				<B><A HREF="matrix-{@project}-{@testingGroup}-{@testedType}-{@host}.html"><xsl:value-of select="@host"/></A></B>
			</LI>
		</xsl:for-each>
	</UL>
	
</xsl:template>

</xsl:stylesheet>