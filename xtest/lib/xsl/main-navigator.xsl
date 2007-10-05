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
		<xsl:with-param name="html-title">Summary from <xsl:value-of select="/XTestResultsReport/SystemInfo/@host"/> run at <xsl:value-of select="/XTestResultsReport/@timeStamp"/></xsl:with-param>
	</xsl:call-template>
</xsl:template>

<xsl:template match="XTestResultsReport">
	<H4>
		<I>
		<B>XTest Results Report:</B><BR/><BR/>
		<xsl:if test="@project">
			Tested project:<B><xsl:value-of select="@project"/></B><BR/>
		</xsl:if>
		<xsl:if test="@build">
			Tested build:<B><xsl:value-of select="@build"/></B><BR/>
		</xsl:if>
		Run on: <B><xsl:value-of select="SystemInfo/@host"/></B><BR/>
		Run at: <B><xsl:value-of select="@timeStamp"/></B><BR/>						
		</I>
		<UL>			
			<LI><A HREF="systeminfo.html" TARGET="report">System Info</A></LI>	
			<BR/><BR/>					
			<LI><A HREF="testreport.html" TARGET="report">Full Results Report</A></LI>			
			<UL>							
				<xsl:for-each select="TestRun">			
				<LI>
					<A HREF="testreport.html#{@runID}" TARGET="report">
						<xsl:if test="@name">
							<xsl:value-of select="@name"/>
						</xsl:if>
						<xsl:if test="not(@name)">
							Test Run
						</xsl:if>
					</A>
					</LI>
				<UL>
					<xsl:for-each select="TestBag[not(./@module = preceding-sibling::TestBag/@module)]">
						<xsl:sort select="@module"/>
						<LI><A HREF="testreport.html#{parent::*/@runID}-{@module}" TARGET="report"><xsl:value-of select="@module"/></A></LI>				
					</xsl:for-each>		
				</UL>
				</xsl:for-each>	
			</UL>			
			<BR/>
			<LI><A HREF="testreport-failures.html" TARGET="report">Failures Report</A></LI>	
			<UL>
				<xsl:if test="count(document('testreport-failures.xml',/*)/XTestResultsReport/TestRun)=0">
					<LI>No failures/errors encountered</LI>
				</xsl:if>
				<xsl:for-each select="document('testreport-failures.xml',/*)/XTestResultsReport/TestRun">
					<LI>
						<A HREF="testreport-failures.html#{@runID}" TARGET="report">
							<xsl:if test="@name">
								<xsl:value-of select="@name"/>
							</xsl:if>
							<xsl:if test="not(@name)">
								Test Run
							</xsl:if>
						</A>
					</LI>
					<UL>
						<xsl:for-each select="TestBag[not(./@module = preceding-sibling::TestBag/@module)]">
							<xsl:sort select="@module"/>
							<LI><A HREF="testreport-failures.html#{parent::*/@runID}-{@module}" TARGET="report"><xsl:value-of select="@module"/></A></LI>				
						</xsl:for-each>		
					</UL>				
				</xsl:for-each>
			</UL>
                        
                        <xsl:if test="count(document('testreport-performance.xml',/*)/XTestResultsReport/TestRun/TestBag/UnitTestSuite/Data/PerformanceData)!=0">
                			<BR/><LI><A HREF="testreport-performance.html" TARGET="report">Performance Results</A></LI>	
			</xsl:if>
		</UL>
		<UL>
			<LI><A HREF="../index.html" TARGET="_top">Home</A></LI>
		</UL>
	</H4>	
</xsl:template>

</xsl:stylesheet>