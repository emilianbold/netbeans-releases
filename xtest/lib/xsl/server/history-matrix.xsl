<?xml version="1.0"?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

 The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

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