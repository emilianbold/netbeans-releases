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
<xsl:stylesheet version="1.0" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xalan="http://xml.apache.org/xalan"
    	xmlns:java="http://xml.apache.org/xslt/java"
		exclude-result-prefixes="xalan java">

<xsl:key name="platform" match="ManagedReport" use="concat(@osName,@osVersion,@osArch)"/> 
<xsl:key name="build" match="ManagedReport" use="@build"/>
<xsl:key name="platformAndBuild" match="ManagedReport" use="concat(@osName,@osVersion,@osArch,@build)"/> 
<xsl:key name="host" match="ManagedReport" use="@host"/>

<xsl:include href="../library.xsl"/>

<!-- global variables -->
<xsl:variable name="testedType" select="//ManagedReport/@testedType"/>
<xsl:variable name="testingGroup" select="//ManagedReport/@testingGroup"/>
<xsl:variable name="buildNumber" select="//ManagedReport/@build"/>

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">
			Test results for <xsl:value-of select="//ManagedReport/@project"/> 
			build <xsl:value-of select="$buildNumber"/>
		</xsl:with-param>
	</xsl:call-template>
</xsl:template>



<xsl:template match="ManagedGroup">
	<xsl:call-template name="MakeBuildSummaryTable"/>
</xsl:template>

<xsl:template name="MakeBuildSummaryTable">
		
	<xsl:variable name="project" select="//ManagedReport/@project"/>
	<xsl:variable name="groupName" select="/ManagedGroup/@name"/>
	
	<H2>
		Test results for <xsl:value-of select="$project"/> 
		build <xsl:value-of select="$buildNumber"/> (<xsl:value-of select="$testingGroup"/>-<xsl:value-of select="$testedType"/> tests)
	</H2>	

	<UL>		
		<LI><A HREF="../../../index.html">XTest Overall Results</A></LI>
		<xsl:variable name="groupIndex" select="concat('../../',$groupName,'-',$testingGroup,'-',$testedType,'.html')"/>
		<xsl:variable name="normalizedGroupIndex" select="java:org.netbeans.xtest.util.FileUtils.normalizeName($groupIndex)"/>
		<LI><A HREF="{$normalizedGroupIndex}"><xsl:value-of select="$project"/> results	(<xsl:value-of select="$testingGroup"/>-<xsl:value-of select="$testedType"/>) </A></LI>
	</UL>


	<TABLE width="98%" cellspacing="2" cellpadding="5" border="0" >		
		<TR align="center">				
			<TD bgcolor="#A6CAF0" rowspan="1" colspan="2"><B>Build Totals</B></TD>				
			<TD colspan="8" bgcolor="#A6CAF0">
				<B>Tested Platforms</B>
			</TD>
		</TR>
		
		<TR align="center">
			<TD bgcolor="#A6CAF0">
				<B>Passed</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Total</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Operating System</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Passed</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Total</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Unexpected Passes</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Expected Fails</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Unexpected Fails</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Errors</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Testing Host(s)</B>
			</TD>
		</TR>
				
		
		
			
			<xsl:variable name="uniquePlatorms" select="//ManagedReport[generate-id(.)=generate-id(key('platform',concat(./@osName,./@osVersion,./@osArch))[1])]"/>			
			<xsl:variable name="platformCount" select="count($uniquePlatorms)"/>
			
			<!--
			<TR>
			<TD colspan="9">
			<TABLE cellspacing="2" cellpadding="5" border="0" WIDTH="100%">
			-->
			<TR></TR>
			
			<xsl:for-each select="$uniquePlatorms">
				<xsl:sort select="@osName"/>
				<xsl:sort select="@osVersion"/>
				<xsl:sort select="@osArch"/>
								
				<TR align="center">
					<xsl:if test="position() = 1">
						
						<xsl:variable name="buildPassed" select="sum(//ManagedReport/@testsPass)"/>
						<xsl:variable name="buildTotal" select="sum(//ManagedReport/@testsTotal)"/>						
						<TD class="pass" rowspan="{$platformCount}"><xsl:value-of select="format-number($buildPassed div $buildTotal,'0.00%')"/></TD>
						<TD class="pass" rowspan="{$platformCount}"><xsl:value-of select="$buildTotal"/></TD>
					</xsl:if>
					<TD class="pass">
						<B><xsl:value-of select="@osName"/>-<xsl:value-of select="@osVersion"/>-<xsl:value-of select="@osArch"/></B>
					</TD>
					<xsl:variable name="passed" select="sum(key('platform',concat(./@osName,./@osVersion,./@osArch))/@testsPass)"/>
					<xsl:variable name="unexpectedPasses" select="sum(key('platform',concat(./@osName,./@osVersion,./@osArch))/@testsUnexpectedPass)"/>
					<xsl:variable name="expectedFails" select="sum(key('platform',concat(./@osName,./@osVersion,./@osArch))/@testsExpectedFail)"/>
					<xsl:variable name="failed" select="sum(key('platform',concat(./@osName,./@osVersion,./@osArch))/@testsFail)"/>
					<xsl:variable name="errors" select="sum(key('platform',concat(./@osName,./@osVersion,./@osArch))/@testsError)"/>
					<xsl:variable name="total" select="sum(key('platform',concat(./@osName,./@osVersion,./@osArch))/@testsTotal)"/>
					<TD class="pass">
						<xsl:value-of select="format-number($passed div $total,'0.00%')"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$total"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$unexpectedPasses"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$expectedFails"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$failed - $expectedFails"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$errors"/>
					</TD>
					<TD class="pass">
						<xsl:for-each select="key('platform',concat(./@osName,./@osVersion,./@osArch))">
                        
							<A HREF="../../../{@webLink}">								
							<xsl:choose>
							  	<xsl:when test="@mappedHostname">
							   		<xsl:value-of select="@mappedHostname"/>
							   	</xsl:when>								       
							   	<xsl:otherwise>
							   	 	<xsl:value-of select="@host"/>
							   	</xsl:otherwise>
							</xsl:choose>
							</A>
                        
							<BR/>
						</xsl:for-each>
					</TD>
				</TR>
				
				
			</xsl:for-each>
			<!--
			</TABLE>
			</TD>
			</TR>
			-->

		
	</TABLE>

	<BR/>
	<HR width="90%"/>

</xsl:template>

</xsl:stylesheet>