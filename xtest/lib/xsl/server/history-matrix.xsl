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

<xsl:key name="platform" match="ManagedReport" use="concat(@osName,@osVersion,@osArch)"/> 

<xsl:include href="../library.xsl"/>

<xsl:param name="includeExceptions"/>

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">History Matrix for <xsl:value-of select="XTestHistoryMatrix/@host"/> running <xsl:value-of select="XTestHistoryMatrix/@testedType"/> tests against <xsl:value-of select="XTestHistoryMatrix/@project"/> by <xsl:value-of select="XTestHistoryMatrix/@testingGroup"/></xsl:with-param>
	</xsl:call-template>
</xsl:template>


<xsl:template match="XTestHistoryMatrix">
<H1>History Matrix for <xsl:value-of select="@host"/> running <xsl:value-of select="@testedType"/> tests against <xsl:value-of select="@project"/> by <xsl:value-of select="@testingGroup"/></H1>

<xsl:for-each select="//HMTest[not(./@repositoryName = preceding-sibling::HMTest/@repositoryName)]">
	<xsl:sort select="@repositoryName"/>
	<xsl:variable name="currentRepository" select="@repositoryName"/>
	<H2><xsl:value-of select="@repositoryName"/></H2>
	<xsl:for-each select="//HMTest[($currentRepository=@repositoryName) and not(./@module = preceding-sibling::HMTest[$currentRepository=@repositoryName]/@module)]">
		<xsl:sort select="@module"/>
		<xsl:variable name="currentModule" select="@module"/>
		<H3><xsl:value-of select="@module"/></H3>
		
		<xsl:variable name="buildCount" select="count(HMTestedBuild)"/>
		
		<TABLE cellspacing="2" cellpadding="5" border="0">
		<TR>
		<TD bgcolor="#A6CAF0" width="15%"><B>Test Name</B></TD>
			<xsl:for-each select = "HMTestedBuild">
				<TD bgcolor="#A6CAF0"><B><xsl:value-of select="@build"/></B></TD>
			</xsl:for-each>
		</TR>
		<xsl:for-each select="//HMTest[($currentRepository=@repositoryName) and ($currentModule=@module) and not (./@testType = preceding-sibling::HMTest[($currentRepository=@repositoryName) and ($currentModule=@module)]/@testType)]">
			<xsl:sort select="@testType"/>
			<xsl:variable name="currentTestType" select="@testType"/>
			<xsl:for-each select="//HMTest[($currentRepository=@repositoryName) and ($currentModule=@module) and ($currentTestType = @testType) and not (./@testBagName = preceding-sibling::HMTest[($currentRepository=@repositoryName) and ($currentModule=@module) and ($currentTestType=@testType)]/@testBagName)]">
				<xsl:sort select="@testBagName"/>
				<xsl:variable name="currentTestBagName" select="@testBagName"/>
				<xsl:for-each select="//HMTest[($currentRepository=@repositoryName) and ($currentModule=@module) and ($currentTestType = @testType) and ($currentTestBagName = @testBagName) and not (./@suiteName = preceding-sibling::HMTest[($currentRepository=@repositoryName) and ($currentModule=@module) and ($currentTestType = @testType) and ($currentTestBagName = @testBagName)]/@suiteName)]">
					<xsl:sort select="@suiteName"/>
					<xsl:variable name="currentSuiteName" select="@suiteName"/>
					<TR>
						<TD class="pass"></TD>
						<TD class="pass" colspan="{$buildCount}"><B>type: <xsl:value-of select="@testType"/>, test bag: <xsl:value-of select="@testBagName"/>, suite: <xsl:value-of select="@suiteName"/></B></TD>
					</TR>
					<xsl:for-each select="//HMTest[($currentRepository=@repositoryName) and ($currentModule=@module) and ($currentTestType = @testType) and ($currentTestBagName = @testBagName) and ($currentSuiteName = @suiteName)]">
						<xsl:sort select="@class"/>
						<xsl:sort select="@name"/>						
						<TR>
							<TD class="pass"><B><xsl:value-of select="@class"/>/<xsl:value-of select="@name"/></B></TD>
							<xsl:for-each select = "HMTestedBuild">				
								<xsl:choose>
									<xsl:when test="@result">										
										<TD class="{translate(@result,' ','-')}-matrix">
                                                                                    <xsl:if test="boolean($includeExceptions)">
											<A HREF="../{@path}"><xsl:value-of select="@result"/></A>
                                                                                    </xsl:if>
                                                                                    <xsl:if test="not(boolean($includeExceptions))">
											<xsl:value-of select="@result"/>
                                                                                    </xsl:if>
										</TD>
									</xsl:when>
									<xsl:otherwise>
										<TD class="error-matrix">N/A</TD>
									</xsl:otherwise>
								</xsl:choose>						
							</xsl:for-each>
						</TR>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:for-each>
		</xsl:for-each>
		</TABLE>
	</xsl:for-each>

</xsl:for-each>


</xsl:template>

</xsl:stylesheet>