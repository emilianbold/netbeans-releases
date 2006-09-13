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

    <xsl:key name="module" match="TestBag" use="concat(@module,parent::*/@runID)"/>
    <xsl:key name="name_runOrder" match="Data/PerformanceData" use="concat(@name, @runOrder, ../../../../@runID)"/>

    <xsl:param name="truncated"/>
    <xsl:param name="mappedHostname"/>

    <xsl:template match="/">
        <xsl:call-template name="html-page">
            <xsl:with-param name="html-title">Summary from <xsl:value-of select="/XTestResultsReport/SystemInfo/@host"/> run at <xsl:value-of select="/XTestResultsReport/@timeStamp"/></xsl:with-param>
        </xsl:call-template>
    </xsl:template>




    <xsl:template match="XTestResultsReport">
        <H1>XTest Results Report</H1>
        <BLOCKQUOTE>
            <H2>
                run on <xsl:value-of select="@host"/>
            </H2>
        </BLOCKQUOTE>
        <UL>
            <xsl:if test="@project">
                <LI>Tested project: <xsl:value-of select="@project"/></LI>
            </xsl:if>
            <xsl:if test="@build">
                <LI>Tested build: <xsl:value-of select="@build"/></LI>
            </xsl:if>
            <!--
            <LI><A HREF="systeminfo.html">System Info</A></LI>
            -->
		
        </UL>
        <hr/>
	
        <table border="0">
            <tr>
                <xsl:for-each select="TestRun">
                    <td valign="top" align="center">
                        <h3>
                            <xsl:if test="@name"><xsl:value-of select="@name"/></xsl:if>
                            <xsl:if test="not(@name)">Test Run</xsl:if>
                            , run at <xsl:value-of select="@timeStamp"/>
                        </h3>

                        <table border="0">
                            <tr class="pass"><td align="center"><b>Module/Testbag/Suite/PerfData</b></td><td align="center"><b>Value</b></td><td align="center"><b>Threshold</b></td></tr>
                            <xsl:for-each select="TestBag[not(./@module = preceding-sibling::TestBag/@module)]">
                                <xsl:sort select="@module"/>
                                <tr bgcolor="#A6CAF0"><td colspan="3"><xsl:value-of select="@module"/></td></tr>

                                <xsl:variable name="currentModule" select="@module"/>	
			
                                <xsl:for-each select = "parent::*/TestBag[@module=$currentModule]">
                                    <xsl:sort data-type="text" select="@name"/>
                                    <tr bgcolor="#90B0D0"><td colspan="3">&#160;&#160;<xsl:value-of select="@name"/></td></tr>
                                    <xsl:for-each select="UnitTestSuite">
                                        <tr bgcolor="#A6CAF0"><td colspan="3">&#160;&#160;&#160;&#160;<xsl:value-of select="@name"/></td></tr>

                                        <xsl:for-each select="Data">
                                            <xsl:for-each select="PerformanceData[generate-id()=generate-id(key('name_runOrder', concat(@name, @runOrder, ../../../../@runID)))]">
                                                <xsl:sort select="concat(@name,@runOrder)"/>

                                                <xsl:variable name="currentName" select="@name"/>
                                                <xsl:variable name="currentOrder" select="@runOrder"/>
                                                
                                                <xsl:variable name="summary" select="sum(parent::*/PerformanceData[concat(@name,@runOrder)=concat($currentName,$currentOrder)]/@value)"/> 
                                                <xsl:variable name="count" select="count(parent::*/PerformanceData[concat(@name,@runOrder)=concat($currentName,$currentOrder)]/@value)"/>
                                                
                                                <xsl:variable name="maximum">
                                                    <xsl:for-each select="parent::*/PerformanceData[concat(@name,@runOrder)=concat($currentName,$currentOrder)]">
                                                        <xsl:sort select="@value" data-type="number" order="descending"/>
                                                        <xsl:if test="position()=1">
                                                            <xsl:value-of select="@value"/>
                                                        </xsl:if>
                                                    </xsl:for-each>
                                                </xsl:variable>
                                                
                                                <xsl:variable name="minimum">
                                                    <xsl:for-each select="parent::*/PerformanceData[concat(@name,@runOrder)=concat($currentName,$currentOrder)]">
                                                        <xsl:sort select="@value" data-type="number" order="ascending"/>
                                                        <xsl:if test="position()=1">
                                                            <xsl:value-of select="@value"/>
                                                        </xsl:if>
                                                    </xsl:for-each>
                                                </xsl:variable>

                                                <xsl:variable name="average">
                                                    <xsl:choose>
                                                        <xsl:when test="$count &gt; 3">
                                                            <xsl:value-of select="($summary - $minimum - $maximum) div ($count - 2)"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="$summary div $count"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>                                                
                                                </xsl:variable>
                                                
                                                <xsl:for-each select="key('name_runOrder', concat(@name, @runOrder, ../../../../@runID))">
                                                    <xsl:choose>
                                                        <xsl:when test="position()=last()">
                                                            <xsl:choose>
                                                                <xsl:when test="$average &gt; @threshold and not(@threshold=0)">
                                                                    <TR valign="top" class="fail" style="font-weight: normal;">
                                                                        <TD>&#160;&#160;&#160;&#160;&#160;&#160;<xsl:value-of select="@name"/>&#160;[<xsl:value-of select="@runOrder"/>]</TD>
                                                                        <TD align="right"><xsl:value-of select="format-number($average, '#')"/>&#160;&#160;<xsl:value-of select="@unit"/></TD>
                                                                        <TD align="center"><xsl:value-of select="@threshold"/>&#160;&#160;<xsl:value-of select="@unit"/></TD>
                                                                    </TR>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <TR valign="top" class="pass">
                                                                        <TD>&#160;&#160;&#160;&#160;&#160;&#160;<xsl:value-of select="@name"/>&#160;[<xsl:value-of select="@runOrder"/>]</TD>
                                                                        <TD align="right"><xsl:value-of select="format-number($average, '#')"/>&#160;&#160;<xsl:value-of select="@unit"/></TD>
                                                                        <TD align="center">
                                                                            <xsl:choose>
                                                                                <xsl:when test="@threshold &gt; 0">
                                                                                    <xsl:value-of select="@threshold"/>&#160;&#160;<xsl:value-of select="@unit"/>
                                                                                </xsl:when>
                                                                                <xsl:otherwise>
                                                                                    &#160;
                                                                                </xsl:otherwise>
                                                                            </xsl:choose>
                                                                        </TD>
                                                                    </TR>
                                                                </xsl:otherwise>
                                                            </xsl:choose>      

                                                        </xsl:when>
                                                    </xsl:choose>
                                                </xsl:for-each>
                                                    
                                            </xsl:for-each>
                                        </xsl:for-each>
                                    </xsl:for-each>
                                </xsl:for-each>	
			
                            </xsl:for-each>		
                        </table>
                    </td>
                </xsl:for-each>		
            </tr>
        </table>
	
    </xsl:template>

</xsl:stylesheet>			