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

<xsl:template name="copyright">
<xsl:comment>
                 Sun Public License Notice
 	
 The contents of this file are subject to the Sun Public License
 Version 1.0 (the "License"). You may not use this file except in
 compliance with the License. A copy of the License is available at
 http://www.sun.com/
 
 The Original Code is NetBeans. The Initial Developer of the Original
 Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 Microsystems, Inc. All Rights Reserved.
</xsl:comment>
</xsl:template>

<xsl:template name="timeFormatterHMS">
	<xsl:param name="time"/>
	<xsl:if test="number($time) != 'NaN'">
		<xsl:variable name="hours" select="floor($time div 3600)"/>
		<xsl:variable name="minutes" select="floor(($time - ( $hours * 3600) ) div 60)"/>
		<xsl:variable name="seconds" select="floor(($time - ($hours * 3600) - ($minutes * 60)))"/>
		<xsl:value-of select="$hours"/>:<xsl:value-of select="format-number($minutes,'00')"/>:<xsl:value-of select="format-number($seconds,'00')"/>
	</xsl:if>
</xsl:template>

<xsl:template name="timeFormatterMS">
	<xsl:param name="time"/>
	<xsl:if test="number($time) != 'NaN'">		
		<xsl:variable name="minutes" select="floor($time  div 60)"/>
		<xsl:variable name="seconds" select="($time - ($minutes * 60))"/>
		<xsl:value-of select="format-number($minutes,'00')"/>:<xsl:value-of select="format-number($seconds,'00.00')"/>
	</xsl:if>
</xsl:template>

<xsl:template name="html-page">
    <xsl:param name="html-title">XTest Report - unknown</xsl:param>
	<HTML>		
		<xsl:call-template name="copyright"/>
		<HEAD>
		<STYLE TYPE="text/css">
			BODY  {
  				font-family: Verdana, sans-serif, helvetica, arial;
  				font-size: 13px;
  				background-color: #FFFFFF;
			}
			TD	{
  				font-family: Verdana, sans-serif, helvetica, arial;
  				font-size: 13px;
			}

			TH	{
				font-family: helvetica, arial, sans-serif;
				font-size: 12pt;
			}
    		.error {
	    		font-weight:bold; background:#EEEEE0; color:purple;
	    		}
	    	.fail {
			    font-weight:bold; background:#EEEEE0; color:red;
    		}
	    	.unexpected-pass {
			    font-weight:bold; background:#EEEEE0; color:#FFB200;
    		}
    		.unknown {
			    font-weight:bold; background:#EEEEE0; color:red;
    		}
	    	.expected-fail {
			    font-weight:bold; background:#EEEEE0; color:brown;
    		}
    		.pass {
		    	background:#EEEEE0;
	    	}
    		.error-matrix {
	    		font-weight:bold; background:#e0a8e0; color:purple;
	    		}
	    	.fail-matrix {
			    font-weight:bold; background:#ffa0a0; color:red;
    		}
	    	.unexpected-pass-matrix {
			    font-weight:bold; background:#FFF0B3; color:#FFB200;
    		}
    		.unknown-matrix {
			    font-weight:bold; background:#EEEEE0; color:red;
    		}    		
	    	.expected-fail-matrix {
			    font-weight:bold; background:#F0D48A; color:brown;
    		}
    		.pass-matrix {
		    	background:#EEEEE0;
	    	}
	    		    	
	    	.header {
		    	background:#A6CAF0;
	    	}
	    	
	    	A:link {
  				color: #001692;
			}

			A:visited {
  				color: #001692;
			}

			A:hover {
  				color: red;
			}

			.default  {
				font-family: Verdana, sans-serif, helvetica, arial;
				font-size: 13px;
			}

			H1 {font-size: 18pt; font-weight : bold; color: #CC3333;}
			H2 {font-size: 14pt; font-weight: bold; color: #CC3333; }
			H3 {font-size: 10pt; font-weight: bold; color: #CC3333; }
			H4,H5,H6 {font-size: 10pt; font-weight: normal; color: #CC3333; }

			.h1 {font-size: 18pt; font-weight : bold; color: #CC3333;}
			.h2 {font-size: 14pt; font-weight: bold; color: #CC3333; }
			.h3 {font-size: 10pt; font-weight: bold; color: #CC3333; }
			.h4 {font-size: 10pt; font-weight: normal; color: #CC3333; }
	    	
	    	UL	{
				list-style-type: circle;
			}
			

			PRE,SAMP,CODE,TT {
  				font-family: Courier New, Monospaced;
  			}			
	    	
		</STYLE>
			<TITLE><xsl:value-of select="$html-title"/></TITLE>
		</HEAD>
		<BODY>		
			<xsl:apply-templates/>
		</BODY>
	</HTML>
</xsl:template>

<xsl:template name="summary-table">
    <xsl:param name="table-width">95%</xsl:param> 
    <TABLE width="{$table-width}" cellspacing="2" cellpadding="5" border="0">
		<xsl:call-template name="summary-header"/>
		<xsl:call-template name="summary-row">
			<xsl:with-param name="testsTotal" select="@testsTotal"/>
			<xsl:with-param name="testsPass" select="@testsPass"/>
			<xsl:with-param name="testsUnexpectedPass" select="@testsUnexpectedPass"/>
			<xsl:with-param name="testsFail" select="@testsFail"/>
			<xsl:with-param name="testsExpectedFail" select="@testsExpectedFail"/>
			<xsl:with-param name="testsError" select="@testsError"/>
			<xsl:with-param name="timeStamp" select="@timeStamp"/>
			<xsl:with-param name="time" select="@time"/>
		</xsl:call-template>
	</TABLE>
</xsl:template>



<xsl:template name="summary-header">
	<TR valign="top" bgcolor="#A6CAF0">
		<TD><B>Total Tests</B></TD>
		<TD><B>Expected Passes</B></TD>
		<TD><B>Unexpected Passes</B></TD>
		<TD><B>Expected Fails</B></TD>
		<TD><B>Unexpected Fails</B></TD>
		<TD><B>Errors</B></TD>
		<TD><B>Success Rate</B></TD>
		<TD><B>Run (when)</B></TD>
		<TD><B>Time (m:ss)</B></TD>
	</TR>
</xsl:template>

<xsl:template name="summary-row">
    <TR class="pass">			
			<TD><xsl:value-of select="@testsTotal"/></TD>
			<TD><xsl:value-of select="@testsPass - @testsUnexpectedPass"/></TD>
                        <TD><xsl:value-of select="@testsUnexpectedPass"/></TD>
			<TD><xsl:value-of select="@testsExpectedFail"/></TD>
			<TD><xsl:value-of select="@testsFail - @testsExpectedFail"/></TD>
			<TD><xsl:value-of select="@testsError"/></TD>				
			<TD><xsl:value-of select="format-number(@testsPass div @testsTotal,'0.00%')"/></TD>
			<TD><xsl:value-of select="@timeStamp"/></TD>
         		<!--
			<TD><xsl:value-of select="($time div 1000)"/></TD>
			-->
			<TD>
   			<xsl:call-template name="timeFormatterMS">
				<xsl:with-param name="time"><xsl:value-of select="(@time div 1000)"/></xsl:with-param>
			</xsl:call-template>
			</TD>
	</TR>
</xsl:template>


<xsl:template name="testbag-summary-header"> 
    <TR valign="top" bgcolor="#A6CAF0">
        <TD><B>TestBag Name</B></TD>
        <TD><B>Attributes</B></TD>
        <TD><B>Test Type</B></TD>
		<TD><B>Total Tests</B></TD>
		<TD><B>Expected Passes</B></TD>
		<TD><B>Unexpected Passes</B></TD>
		<TD><B>Expected Fails</B></TD>
		<TD><B>Unexpected Fails</B></TD>
		<TD><B>Errors</B></TD>
		<TD><B>Success Rate</B></TD>
		<TD><B>Run (when)</B></TD>
		<TD><B>Time (m:ss)</B></TD>
	</TR>

</xsl:template>

<xsl:template name="testbag-summary-row">
    <TR class="pass">
    	<xsl:if test="not(@testsTotal=@testsPass)">
            <xsl:attribute name="class">fail</xsl:attribute>
        </xsl:if>
   		<TD>
   		<xsl:if test="@bagID">
   		    <xsl:variable name="current-runID" select="parent::*/@runID"/>
   		    <A HREF="../{$current-runID}/{@bagID}/htmlresults/testbag.html">
   		    <xsl:value-of select="@name"/>
   		    </A>
   		</xsl:if>
   		<xsl:if test="not(@bagID)">
   		    <xsl:value-of select="@name"/>
   		</xsl:if>
   		</TD>	
	    <TD><xsl:value-of select="@testAttribs"/> </TD>	
	    <TD><xsl:value-of select="@testType"/></TD>
   		<TD><xsl:value-of select="@testsTotal"/></TD>
   		<TD><xsl:value-of select="@testsPass - @testsUnexpectedPass"/></TD>
                <TD><xsl:value-of select="@testsUnexpectedPass"/></TD>
                <TD><xsl:value-of select="@testsExpectedFail"/></TD>
                <TD><xsl:value-of select="@testsFail - @testsExpectedFail"/></TD>
   		<TD><xsl:value-of select="@testsError"/></TD>				
   		<TD><xsl:value-of select="format-number(@testsPass div @testsTotal,'0.00%')"/></TD>
   		<!--
                <TD><xsl:value-of select="@timeStamp"/></TD>
	    	-->
	       <!--
   		<TD><xsl:value-of select="(@time div 1000)"/></TD>
   		-->
   		<TD>
   			<xsl:call-template name="timeFormatterMS">
				<xsl:with-param name="time"><xsl:value-of select="(@time div 1000)"/></xsl:with-param>
			</xsl:call-template>
		</TD>
   	</TR>
</xsl:template>

<xsl:template name="testsuite-summary-header"> 
    <TR valign="top" bgcolor="#A6CAF0">
        <TD><B>TestSuite Name</B></TD>
		<TD><B>Total Tests</B></TD>
		<TD><B>Expected Passes</B></TD>
		<TD><B>Unexpected Passes</B></TD>
		<TD><B>Expected Fails</B></TD>
		<TD><B>Unexpected Fails</B></TD>
		<TD><B>Errors</B></TD>
		<TD><B>Success Rate</B></TD>
		<TD><B>Time (s)</B></TD>
	</TR>

</xsl:template>

<xsl:template name="testsuite-summary-row">
    <TR class="pass">
        <xsl:if test="not(@testsTotal=@testsPass)">
            <xsl:attribute name="class">fail</xsl:attribute>
        </xsl:if>
        
   		<TD>
   		<xsl:if test="@name">    		    
   		    <A HREF="suites/TEST-{@name}.html">
   		    <xsl:value-of select="@name"/>
   		    </A>
   		</xsl:if>
   		<xsl:if test="not(@name)">
   		    <xsl:value-of select="@name"/>
   		</xsl:if>
   		</TD>	
   		<TD><xsl:value-of select="@testsTotal"/></TD>
   		<TD><xsl:value-of select="@testsPass - @testsUnexpectedPass"/></TD>
                <TD><xsl:value-of select="@testsUnexpectedPass"/></TD>
                <TD><xsl:value-of select="@testsExpectedFail"/></TD>
                <TD><xsl:value-of select="@testsFail - @testsExpectedFail"/></TD>
   		<TD><xsl:value-of select="@testsError"/></TD>				
   		<TD><xsl:value-of select="format-number(@testsPass div @testsTotal,'0.00%')"/></TD>
   		<TD><xsl:value-of select="(@time div 1000)"/></TD>
   	</TR>
</xsl:template>


</xsl:stylesheet>
