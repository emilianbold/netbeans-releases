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
