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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="library.xsl"/>

<xsl:param name="includeIDELog"/>

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">TestBag <xsl:value-of select="/TestBag/@name"/></xsl:with-param>
	</xsl:call-template>
</xsl:template>


<xsl:template match="TestBag">
	<H2>Test Bag: <xsl:value-of select="@name"/></H2>
	<P><A HREF="../../../htmlresults/testreport.html">go back to report</A></P>
	<xsl:call-template name="summary-table">
			<xsl:with-param name="table-width">95%</xsl:with-param>
	</xsl:call-template>
	<UL>
	<xsl:if test="@testAttributes">
		<LI>Attributes: <xsl:value-of select="@testAttribs"/></LI>
	</xsl:if>
	<LI>Executor: <xsl:value-of select="@executor"/></LI>
	<LI>Module: <xsl:value-of select="@module"/></LI>
	<LI>Test Type: <xsl:value-of select="@testType"/></LI>
        <xsl:if test="boolean($includeIDELog)">
  	  <xsl:if test="string(@ideUserDir)='true'">
		<LI><A HREF="../sys/ide/">IDE Logs</A></LI>
	  </xsl:if>
        </xsl:if>  
	<!-- end of hack -->
	<xsl:if test="@unexpectedFailure">
                <FONT color="#FF0000">
        	<LI><B><xsl:value-of select="@unexpectedFailure"/></B></LI>
                <UL>                    
                        <xsl:for-each select="//UnitTestSuite[@unexpectedFailure]">
                            <xsl:sort select="@name"/>
                            <LI>
                                <A HREF="suites/TEST-{@name}.html">
                                    <xsl:value-of select="@name"/>
                                </A>
                            </LI>
                        </xsl:for-each>                    
                </UL>
                </FONT>
	</xsl:if>
	</UL>
	<HR/>
	<H2>Suites:</H2>
		<TABLE width="95%" cellspacing="2" cellpadding="5" border="0">	
			<xsl:call-template name="testsuite-summary-header"/>
			<xsl:apply-templates select="UnitTestSuite">
				<xsl:sort select="@name"/>
			</xsl:apply-templates>	
		</TABLE>
</xsl:template>

<xsl:template match="UnitTestSuite">
	<xsl:call-template name="testsuite-summary-row"/>
</xsl:template>

</xsl:stylesheet>
