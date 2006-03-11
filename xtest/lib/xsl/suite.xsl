<?xml version="1.0"?>
<!--
                 Sun Public License Notice
 
 The contents of this file are subject to the Sun Public License
 Version 1.0 (the "License"). You may not use this file except in
 compliance with the License. A copy of the License is available at
 http://www.sun.com/
 
 The Original Code is NetBeans. The Initial Developer of the Original
 Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 Microsystems, Inc. All Rights Reserved.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:include href="library.xsl"/>

    <xsl:key name="name_runOrder" match="PerformanceData" use="concat(@name, @runOrder)"/>

    <xsl:param name="truncated"/>
    <xsl:param name="includeExceptions">true</xsl:param>

    <xsl:template match="/">
        <xsl:call-template name="html-page">
            <xsl:with-param name="html-title">TestSuite <xsl:value-of select="/UnitTestSuite/@name"/></xsl:with-param>
        </xsl:call-template>	
    </xsl:template>



    <xsl:template match="UnitTestSuite">	
        <H2>Test Suite <xsl:value-of select="@name"/></H2>
        <P><A HREF="../../htmlresults/testbag.html">go back to testbag</A></P>
        <P>
            <H3>Summary:</H3>
            <TABLE width="95%" cellspacing="2" cellpadding="5" border="0">
                <TR valign="top" bgcolor="#A6CAF0">
                    <TD><B>Total tests</B></TD>
                    <TD><B>Expected Passes</B></TD>
                    <TD><B>Unexpected Passes</B></TD>
                    <TD><B>Expected Fails</B></TD>
                    <TD><B>Unexpected Fails</B></TD>
                    <TD><B>Errors</B></TD>
                    <TD><B>Success Rate</B></TD>
                    <TD><B>Run(when)</B></TD>
                    <TD><B>Time(ms)</B></TD>
                </TR>
                <TR class="pass">			
                    <TD><xsl:value-of select="@testsTotal"/></TD>
                    <TD><xsl:value-of select="@testsPass - @testsUnexpectedPass"/></TD>
                    <TD><xsl:value-of select="@testsUnexpectedPass"/></TD>
                    <TD><xsl:value-of select="@testsExpectedFail"/></TD>
                    <TD><xsl:value-of select="@testsFail - @testsExpectedFail"/></TD>
                    <TD><xsl:value-of select="@testsError"/></TD>				
                    <TD><xsl:value-of select="format-number(@testsPass div @testsTotal,'0.00%')"/></TD>
                    <TD><xsl:value-of select="@timeStamp"/></TD>
                    <TD><xsl:value-of select="@time"/></TD>
                </TR>
            </TABLE>
        </P>
        <xsl:if test="@unexpectedFailure">
            <h3>Unexpected Failure:</h3>
            <p><blockquote><xsl:value-of select="@unexpectedFailure"/></blockquote></p>
        </xsl:if>
        <HR/>
        <P>
            <H3>Individual Tests:</H3>
            <TABLE width="95%" cellspacing="2" cellpadding="5" border="0">
                <TR valign="top" bgcolor="#A6CAF0">
                    <TD width="18%"><B>Name</B></TD>
                    <TD width="7%"><B>Status</B></TD>
                    <TD width="70%"><B>Message</B></TD>
                    <xsl:if test="not(boolean($truncated))">
                        <TD width="5%"><B>Workdir</B></TD>
                    </xsl:if>
                    <TD nowrap="nowrap" width="5%"><B>Time(ms)</B></TD>
                </TR>
                <xsl:apply-templates select="UnitTestCase" mode="table"/>		
            </TABLE>				
        </P>
        <xsl:if test="(boolean($includeExceptions))">
            <P>
                <BR/>
                <xsl:if test="@testsTotal!=@testsPass">
                    <H3>Details for failed test/tests with errors:</H3>
                    <BR/>
                    <xsl:apply-templates select="UnitTestCase" mode="innerText"/>
                </xsl:if>
            </P>
        </xsl:if>
        <xsl:if test="Data/PerformanceData">
            <P>
                <H3>Measured Performance Data:</H3>
                <TABLE cellspacing="2" cellpadding="5" border="0">
                    <TR valign="top" bgcolor="#A6CAF0">
                        <TD ><B>Name [run order]</B></TD>
                        <TD align="center"><B>Value</B></TD>
                        <TD align="center"><B>Threshold</B></TD>
                    </TR>
                    <xsl:for-each select="Data/PerformanceData[generate-id()=generate-id(key('name_runOrder', concat(@name, @runOrder)))]">

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
                                                
                        <xsl:sort select="concat(@name,@runOrder)"/>

                        <xsl:for-each select="key('name_runOrder', concat(@name, @runOrder))">
                            <xsl:choose>
                                <xsl:when test="position()=last()">
                                    <xsl:choose>
                                        <xsl:when test="$average &gt; @threshold and not(@threshold=0)">
                                            <TR valign="top" class="fail" style="font-weight: normal;">
                                                <TD>&#160;&#160;<xsl:value-of select="@name"/>&#160;[<xsl:value-of select="@runOrder"/>]</TD>
                                                <TD align="right"><xsl:value-of select="format-number($average, '#')"/>&#160;&#160;<xsl:value-of select="@unit"/></TD>
                                                <TD align="center"><xsl:value-of select="@threshold"/>&#160;&#160;<xsl:value-of select="@unit"/></TD>
                                            </TR>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <TR valign="top" class="pass">
                                                <TD>&#160;&#160;<xsl:value-of select="@name"/>&#160;[<xsl:value-of select="@runOrder"/>]</TD>
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
                </TABLE>				
            </P>
        </xsl:if>
    </xsl:template>

    <xsl:template match="UnitTestCase" mode="table">
        <A NAME="@name"/>
        <TR valign="top">
            <xsl:attribute name="class">
                <xsl:value-of select="translate(@result,' ','-')"/>
            </xsl:attribute>
            <TD><xsl:value-of select="@name"/></TD>
            <TD>
                <xsl:if test="text()">
                    <xsl:if test="boolean($includeExceptions)">
                        <A><xsl:attribute name="href">#<xsl:value-of select="@class"/>.<xsl:value-of select="@name"/></xsl:attribute><xsl:value-of select="@result"/></A>
                    </xsl:if>
                    <xsl:if test="not(boolean($includeExceptions))">	
                        <xsl:value-of select="@result"/>
                    </xsl:if>
                </xsl:if>
                <xsl:if test="not(text())">
                    <xsl:value-of select="@result"/>
                </xsl:if>
            </TD>
            <TD>
                <xsl:if test="@failReason">
                    <xsl:value-of select="@failReason"/>: 
                </xsl:if>
                <xsl:value-of select="@message"/>
            </TD>
            <xsl:if test="not(boolean($truncated))">
                <TD>
                    <xsl:if test="@workdir">
                        <A><xsl:attribute name="href">../../user/<xsl:value-of select="translate(@workdir,'\','/')"/>/</xsl:attribute>Yes</A>
                    </xsl:if>
                    <xsl:if test="not(@workdir)">
                        No
                    </xsl:if>
                </TD>
            </xsl:if>
            <TD><xsl:value-of select="@time"/></TD>
        </TR>
    </xsl:template>

    <xsl:template match="UnitTestCase" mode="innerText">
        <xsl:if test="text()">
            <P>
                <A>
                    <xsl:attribute name="name">
                        <xsl:value-of select="@class"/>.<xsl:value-of select="@name"/>
                    </xsl:attribute>
                </A>
                <H5><xsl:value-of select="@name"/>:</H5>
                <PRE>		 
                    <xsl:value-of select="."/>
                </PRE>
            </P>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>