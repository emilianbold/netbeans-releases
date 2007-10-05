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

<!-- is this xsl with old builds ? -->
<xsl:param name="oldBuilds"/>


<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">Results of <xsl:value-of select="//ManagedReport/@testedType"/> tests for <xsl:value-of select="//ManagedReport/@project"/> 
	tested by <xsl:value-of select="//ManagedReport/@testingGroup"/></xsl:with-param>
	</xsl:call-template>
</xsl:template>



<xsl:template match="ManagedGroup">
	<xsl:call-template name="MakeBuildsSummaryTable"/>
</xsl:template>

<xsl:template name="MakeBuildsSummaryTable">
		
	
	<H2>
		Results of <xsl:value-of select="//ManagedReport/@testedType"/> tests for <xsl:value-of select="//ManagedReport/@project"/> 	
		tested by <xsl:value-of select="//ManagedReport/@testingGroup"/>
	</H2>	

	<UL>
		<xsl:variable name="buildURL" select="concat(/ManagedGroup/@name,'-',ManagedReport/@testingGroup,'-',ManagedReport/@testedType,'.html')"/>
		<xsl:variable name="normalizedBuildURL" select="java:org.netbeans.xtest.util.FileUtils.normalizeName($buildURL)"/>
		<LI><A HREF="../index.html">XTest Overall Results</A></LI>		
		<xsl:if test="(/ManagedGroup/@historyMatrices &gt; 0) and (not($oldBuilds))">
			<LI><A HREF="#history">History Matrices</A></LI>
		</xsl:if>
		<xsl:if test="(/ManagedGroup/@currentBuilds &gt; -1) and (not($oldBuilds))">
			<LI><A HREF="old-{$normalizedBuildURL}">Older Builds</A></LI>
		</xsl:if>
		<xsl:if test="$oldBuilds">
			<LI><A HREF="{$normalizedBuildURL}">Current builds</A></LI>
		</xsl:if>
	</UL>
	<BR/>

	<TABLE width="98%" cellspacing="2" cellpadding="5" border="0" >		
		<TR align="center">
			<TD bgcolor="#A6CAF0" rowspan="2"><B>Build</B></TD>		
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
		
		
		
				
		<xsl:variable name="uniqueBuildList" select="//ManagedReport[generate-id(.)=generate-id(key('build',./@build)[1])]"/>
		
		<xsl:for-each select="$uniqueBuildList">
			<xsl:sort select="@build" order = "descending"/>
			<xsl:variable name="currentBuild" select="@build"/>			
			<xsl:if test="((not($oldBuilds))and(/ManagedGroup/@currentBuilds &gt;= position())) or ( ($oldBuilds='true') and(/ManagedGroup/@currentBuilds &lt; position())) or (/ManagedGroup/@currentBuilds='-1')">
			
			<xsl:variable name="uniquePlatormsInBuild" select="//ManagedReport[generate-id(.)=generate-id(key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))[1])]"/>			
			<xsl:variable name="platformCount" select="count($uniquePlatormsInBuild)"/>
			
			<!--
			<TR>
			<TD colspan="9">
			<TABLE cellspacing="2" cellpadding="5" border="0" WIDTH="100%">
			-->
			<TR></TR>
			
			<xsl:for-each select="$uniquePlatormsInBuild">
				<xsl:sort select="@osName"/>
				<xsl:sort select="@osVersion"/>
				<xsl:sort select="@osArch"/>
								
				<TR align="center">
					<xsl:if test="position() = 1">
						<TD bgcolor="#A6CAF0" rowspan="{$platformCount}">
							<B>
								<xsl:variable name="singleBuildURL" select="concat(@build,'/',//ManagedReport/@testingGroup,'-',//ManagedReport/@testedType,'/index.html')"/>
								<xsl:variable name="normalizedSingleBuildURL" select="java:org.netbeans.xtest.util.FileUtils.normalizeName($singleBuildURL)"/>	
								<A HREF="{$normalizedSingleBuildURL}"><xsl:value-of select="@build"/></A>
							</B>
						</TD>
						<xsl:variable name="buildPassed" select="sum(key('build',$currentBuild)/@testsPass)"/>
						<xsl:variable name="buildTotal" select="sum(key('build',$currentBuild)/@testsTotal)"/>						
						<TD class="pass" rowspan="{$platformCount}"><xsl:value-of select="format-number($buildPassed div $buildTotal,'0.00%')"/></TD>
						<TD class="pass" rowspan="{$platformCount}"><xsl:value-of select="$buildTotal"/></TD>
					</xsl:if>
					<TD class="pass">
						<B><xsl:value-of select="@osName"/>-<xsl:value-of select="@osVersion"/>-<xsl:value-of select="@osArch"/></B>
					</TD>
					<xsl:variable name="passed" select="sum(key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))/@testsPass)"/>
					<xsl:variable name="unexpectedPasses" select="sum(key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))/@testsUnexpectedPass)"/>
					<xsl:variable name="expectedFails" select="sum(key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))/@testsExpectedFail)"/>
					<xsl:variable name="failed" select="sum(key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))/@testsFail)"/>
					<xsl:variable name="errors" select="sum(key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))/@testsError)"/>
					<xsl:variable name="total" select="sum(key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))/@testsTotal)"/>
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
						<xsl:for-each select="key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))">
                        
							<A HREF="../{@webLink}">								
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
			<!-- build -->
			</xsl:if>
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
	<BR/>
	<HR width="90%"/>
	<xsl:if test="not($oldBuilds) and (/ManagedGroup/@historyMatrices &gt; 0)">
		<A name="history">
		<H5>History Matrices for:</H5>
		</A>
		<UL>
			<xsl:variable name="differentHosts" select="//ManagedReport[generate-id(.)=generate-id(key('host',./@host)[1])]"/>
			<xsl:for-each select="$differentHosts">
				<xsl:sort select="@host"/>
				<LI>				
					<B>
                      <xsl:if test="/ManagedGroup/@historyMatrices &gt; 0">                                       		
                      	 <xsl:variable name="MURL" select="concat('matrix-',/ManagedGroup/@name,'-',./@testingGroup,'-',./@testedType,'-',./@host,'.html')"/>
                      	 <xsl:variable name="normalizedMURL" select="java:org.netbeans.xtest.util.FileUtils.normalizeName($MURL)"/>
                         <A HREF="{$normalizedMURL}"><xsl:value-of select="@host"/></A>
                       </xsl:if>
                                           
                    </B>
				</LI>
			</xsl:for-each>
		</UL>
	</xsl:if>	
</xsl:template>

</xsl:stylesheet>