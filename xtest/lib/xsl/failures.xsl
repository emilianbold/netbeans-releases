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
        <xsl:variable name="SuiteNameShort">
            <!-- XXX could instead use library.xsl#make-FQN-breakable -->
            <xsl:call-template name="strip-package">
                <xsl:with-param name="val" select="$SuiteName"/>
            </xsl:call-template>
        </xsl:variable>
	<TR valing="top">
		<xsl:attribute name="class"><xsl:value-of select="translate(@result,' ','-')"/></xsl:attribute>
		<TD>
			<A HREF="../{$ParentTestRun/@runID}/{$ParentTestBag/@bagID}/htmlresults/suites/TEST-{$SuiteName}.html#{@name}">
                            <xsl:value-of select="$SuiteNameShort"/>.<xsl:value-of select="@name"/>
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

    <xsl:template name="strip-package">
        <xsl:param name="val"/>
        <xsl:choose>
            <xsl:when test="contains($val, '.')">
                <xsl:call-template name="strip-package">
                    <xsl:with-param name="val" select="substring-after($val, '.')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$val"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
