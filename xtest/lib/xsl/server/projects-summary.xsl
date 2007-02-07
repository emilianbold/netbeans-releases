<?xml version="1.0"?>
<!--
The contents of this file are subject to the terms of the Common Development
and Distribution License (the License). You may not use this file except in
compliance with the License.

 You can obtain a copy of the License at http://www.netbeans.org/cddl.html
or http://www.netbeans.org/cddl.txt.

When distributing Covered Code, include this CDDL Header Notice in each file
and include the License file at http://www.netbeans.org/cddl.txt.
If applicable, add the following below the CDDL Header, with the fields
enclosed by brackets [] replaced by your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

 The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 Microsystems, Inc. All Rights Reserved.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:java="http://xml.apache.org/xslt/java"
   exclude-result-prefixes="java">

<xsl:param name="pesWebDataFile" select="string('c:\tmp\newpes\webdata.xml')"/>


<xsl:include href="../library.xsl"/>

<xsl:key name="group" match="ManagedReport" use="@testingGroup"/>
<xsl:key name="groupAndType" match="ManagedReport" use="concat(@testingGroup,@testedType)"/>
<xsl:key name="groupAndProject" match="ManagedReport" use="concat(@testingGroup,@project)"/>
<xsl:key name="groupAndTypeAndProject" match="ManagedReport" use="concat(@testingGroup,@testedType,@project)"/>
<xsl:key name="groupAndTypeAndProjectAndBuild" match="ManagedReport" use="concat(@testingGroup,@testedType,@project,@build)"/>

<!-- not used -->
<!-- 
<xsl:key name="platform" match="ManagedReport" use="concat(@osName,@osVersion,@osArch)"/> 
<xsl:key name="platformAndBuild" match="ManagedReport" use="concat(@osName,@osVersion,@osArch,@build)"/> 
<xsl:key name="host" match="ManagedReport" use="@host"/>
-->

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">XTest Overall Results</xsl:with-param>
	</xsl:call-template>
</xsl:template>


<xsl:template match="ManagedGroup">
	<xsl:call-template name="MakeProjectsTestsSummaryTable"/>
</xsl:template>

<xsl:template name="MakeProjectsTestsSummaryTable">
	
	<H1>XTest Overall Results:</H1>	
	<xsl:if test="/ManagedGroup/@description">
		<H3><xsl:value-of select="/ManagedGroup/@description"/></H3>
	</xsl:if>
	<P>	
		<UL>
			<LI>This page was generated at: <xsl:value-of select="string(java:java.util.Date.new())"/></LI>
		</UL>
	</P>
	
	
	
	<!-- for each testing group -->
	<xsl:variable name="uniqueTestingGroup" select="//ManagedReport[generate-id(.)=generate-id(key('group',./@testingGroup)[1])]"/>
	
	<xsl:choose>
	<xsl:when test="count(//ManagedReport) &gt; 0">
	<xsl:for-each select="$uniqueTestingGroup">
		<xsl:sort select="@testingGroup" order = "descending"/>
		<xsl:variable name="currentTestingGroup" select="@testingGroup"/>		
			<H2>Department: <xsl:value-of select="@testingGroup"/></H2>
		
		<TABLE width="90%" cellspacing="2" cellpadding="5" border="0" >	
			<xsl:variable name="uniqueTestedType" select="//ManagedReport[generate-id(.)=generate-id(key('groupAndType',concat($currentTestingGroup,./@testedType))[1])]"/>
			<TR align="center">
				<TD rowspan="3" bgcolor="#A6CAF0" width="30%"><B>Tested Projects</B></TD>
				<TD colspan="{count($uniqueTestedType)*4}" bgcolor="#A6CAF0">
					<B>Overall results for <xsl:value-of select="$currentTestingGroup"/></B>
				</TD>
			</TR>
			<TR align="center">
				<xsl:for-each select="$uniqueTestedType">
					<xsl:sort select="@testedType"/>
					<TD class="pass" colspan="4"><B>test type: <xsl:value-of select="@testedType"/></B></TD>					
				</xsl:for-each>
			</TR>
			<TR align="center">
				<xsl:for-each select="$uniqueTestedType">					
					<TD class="pass"><B>last build</B></TD>
					<TD class="pass"><B>pass</B></TD>
					<TD class="pass"><B>total</B></TD>
					<TD class="pass"><B>project report</B></TD>
				</xsl:for-each>
			</TR>
			
			<!-- now for each tested projects by this group -->
			<xsl:variable name="uniqueProject" select="//ManagedReport[generate-id(.)=generate-id(key('groupAndProject',concat($currentTestingGroup,./@project))[1])]"/>
			
			<xsl:for-each select="$uniqueProject">
				<xsl:sort select="@project"/>
				<xsl:variable name="currentProject" select="@project"/>
				
				<TR></TR>
				
				<TR align="center">
					<TD class="pass"><B><xsl:value-of select="@project"/></B></TD>
					
					<!-- now get information from the latest build and create the link -->
					<!---
					<xsl:variable name="uniqueTestedType" select="//ManagedReport[generate-id(.)=generate-id(key('groupAndType',concat($currentTestingGroup,@testedType))[1])]"/>
					-->
					<xsl:for-each select="$uniqueTestedType">
						<xsl:sort select="@testedType"/>
						<xsl:variable name="currentType" select="@testedType"/>	
						<xsl:variable name="builds" select="key('groupAndTypeAndProject',concat($currentTestingGroup,$currentType,$currentProject))"/>						
						<xsl:for-each select="$builds">							
							<xsl:sort select="@build"  order = "descending"/>
							<xsl:if test="position()=1">
							
								<xsl:variable name="lastBuild" select="@build"/>
								<TD class="pass">
									<xsl:value-of select="$lastBuild"/>
								</TD>
									<xsl:variable name="expression" select="key('groupAndTypeAndProjectAndBuild',concat($currentTestingGroup,$currentType,$currentProject,$lastBuild))"/>
								<TD class="pass">
									<xsl:value-of select="format-number(sum(($expression)/@testsPass) div sum(($expression)/@testsTotal),'0.00%')"/>
								</TD>
								<TD class="pass">
									<xsl:value-of select="sum($expression/@testsTotal)"/>
								</TD>
								<TD class="pass">									                                  
                                   <xsl:variable name="URL" select="concat($currentProject,'/',/ManagedGroup/@name,'-',$currentTestingGroup,'-',$currentType,'.html')"/>
                                   <xsl:variable name="normalizedURL" select="java:org.netbeans.xtest.util.FileUtils.normalizeName($URL)"/>
                                   <A HREF="{$normalizedURL}">report</A>                                 
                                </TD>
						    </xsl:if>								
					    </xsl:for-each>
						<xsl:if test="count($builds)=0">								
							<TD colspan="4" class="pass">-</TD>
						</xsl:if>
					</xsl:for-each>

				</TR>

			</xsl:for-each>

			
		</TABLE>
		<BR/>
	</xsl:for-each>
	</xsl:when>
	<xsl:otherwise>
		<BLOCKQUOTE><BLOCKQUOTE>
			<H3>No results have been submitted yet</H3>
		</BLOCKQUOTE></BLOCKQUOTE>
	</xsl:otherwise>
	</xsl:choose>

	<HR width="90%"/>

	<xsl:if test="$pesWebDataFile">
		<xsl:if test="count(document($pesWebDataFile,/*)/ManagedWeb/ManagedGroup) &gt; 1">
		<BR/>
		<H2>Results from other projects:</H2>    				
		<UL>
			<xsl:variable name="currentName" select="/ManagedGroup/@name"/>
			<!--
			<LI>current name = <xsl:value-of select="$currentName"/></LI>
			-->
    		<xsl:for-each select="document($pesWebDataFile,/*)/ManagedWeb/ManagedGroup"> 
    			<xsl:if test="string(@name)!=string($currentName)">
    				<xsl:choose>
    				<xsl:when test="./@main != 'true'"> 
    					<xsl:variable name="URL" select="concat('group-',./@name,'.html')"/>
    					<xsl:variable name="normalizedURL" select="java:org.netbeans.xtest.util.FileUtils.normalizeName($URL)"/>  				
		    			<LI><A HREF="{$normalizedURL}"><xsl:value-of select="./@description"/></A></LI>
		    		</xsl:when>		    		
	    			<xsl:otherwise>
	    				<LI><A HREF="index.html"><xsl:value-of select="./@description"/></A></LI>
	    			</xsl:otherwise>
	    			</xsl:choose>
	    		</xsl:if>
			</xsl:for-each>
	
		</UL>
		</xsl:if>
	</xsl:if>
	

</xsl:template>




</xsl:stylesheet>