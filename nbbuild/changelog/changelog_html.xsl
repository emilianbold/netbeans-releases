<?xml version="1.0" encoding="ASCII" ?>

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

<!--
     this is a sample stylesheet for xml->html output of NetBeans changelog module
     Original author: Rudolf Balada <Rudolf.Balada@sun.com> (C) 2003 Sun Microsystems, Inc.
     Note: this is my very first xsl
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xlink="http://www.w3c.org/1999/xlink">

<xsl:output method="html"
            indent="yes"
/>


<!-- root element -->
<xsl:template match="/">
<html>
<head>
<title>Changelog</title>
</head>

<xsl:choose>
  <xsl:when test='count(changelog/entry) &gt; 0'> <!-- there are entries -->
    <xsl:apply-templates select="changelog"/>
  </xsl:when>
  <xsl:otherwise> <!-- no changes -->
  <body link="#000000" alink="#000000" vlink="#000000" text="#000000">
<STYLE TYPE="text/css"><xsl:text><!--
A:link { color:#000000 }
A:active {color:#000000 }
A:visited { color: #000000}
H1 { font-family: arial,helvetica,sans-serif; font-size: 18pt; font-weight: bold;}
H2 { font-family: arial,helvetica,sans-serif; font-size: 14pt; font-weight: bold;}
BODY,TD { font-family: arial,helvetica,sans-serif; font-size: 10pt; }
TH { font-family: arial,helvetica,sans-serif; font-size: 11pt; font-weight: bold; }
//--></xsl:text></STYLE>
<h1>Changelog</h1>
    <TABLE BORDER="0" WIDTH="800" CELLPADDING="0" CELLSPACING="0" BGCOLOR="#000000">
      <TR>
        <TD bgcolor="#000000">
          <TABLE BORDER="0" WIDTH="100%" CELLPADDING="3" CELLSPACING="1" BGCOLOR="#000000">
            <TR><TD align="center" valign="middle" bgcolor="#ffffff"><B>No changes</B></TD></TR>
          </TABLE>
        </TD>
      </TR>
    </TABLE>
  </body>
  </xsl:otherwise>
</xsl:choose>
</html>
</xsl:template>

<!-- changelog -->
<xsl:template match="changelog">
  <body link="#000000" alink="#000000" vlink="#000000" text="#000000">
<STYLE TYPE="text/css"><xsl:text>&lt;!--
A:link { color:#000000 }
A:active {color:#000000 }
A:visited { color: #000000}
H1 { font-family: arial,helvetica,sans-serif; font-size: 18pt; font-weight: bold;}
H2 { font-family: arial,helvetica,sans-serif; font-size: 14pt; font-weight: bold;}
BODY,TD { font-family: arial,helvetica,sans-serif; font-size: 10pt; }
TH { font-family: arial,helvetica,sans-serif; font-size: 11pt; font-weight: bold; }
//--&gt;</xsl:text></STYLE>
<h1>Changelog</h1>
    <xsl:apply-templates select='query'/>
    <TABLE BORDER="0" WIDTH="800" CELLPADDING="0" CELLSPACING="0" BGCOLOR="#000000">
      <TR>
        <TD bgcolor="#000000">
          <TABLE BORDER="0" WIDTH="100%" CELLPADDING="3" CELLSPACING="1" BGCOLOR="#000000">
                <xsl:apply-templates select="entry"/> 
          </TABLE>
        </TD>
      </TR>
    </TABLE>
    <xsl:apply-templates select='summary'/>
  </body>
</xsl:template>

<!-- entry -->
<xsl:template match="entry">
            <TR>
              <TD colspan="2" bgcolor="#9999CC">
                <TABLE width="100%" cellpadding="0" cellspacing="0" border="0">
                  <TR>
                    <TD><xsl:apply-templates select="date"/>
                        <xsl:text> </xsl:text>
                        <xsl:apply-templates select="time"/>
                        <xsl:text> </xsl:text>
                        <xsl:apply-templates select="author"/>
                    </TD>
                    <TD align="right"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
                    </TD>
                  </TR>
                </TABLE>
              </TD>
            </TR>
            <TR>
              <TD width="20" rowspan="2" bgcolor="#CCCCFF"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;&nbsp;&nbsp;&nbsp;]]></xsl:text></TD>
              <TD bgcolor="#CCCCCC"><b><xsl:apply-templates select="msg"/></b></TD>
            </TR>
            <TR>
              <TD bgcolor="#EEEEEE">
                    <xsl:apply-templates select="file"/>
              </TD>
            </TR>
            <TR>
              <TD colspan="2" bgcolor="#ffffff"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;&nbsp;&nbsp;&nbsp;]]></xsl:text></TD>
            </TR>
</xsl:template>


<!-- changelog/query -->
<xsl:template match="changelog/query">
<xsl:if test='string-length(daterange/text()) &gt; 0'>
  <xsl:text>Date range: </xsl:text><xsl:value-of select='daterange/text()'/><BR/>
</xsl:if>
<xsl:if test='string-length(revisionfilter/text()) &gt; 0'>
  <xsl:text>Revision filter: </xsl:text><xsl:value-of select='revisionfilter/text()'/><BR/>
</xsl:if>
<xsl:if test='string-length(messagefilter/text()) &gt; 0'>
  <xsl:text>Message filter:  type:</xsl:text><xsl:value-of select='messagefilter/@messagetype'/><xsl:text> string: </xsl:text><xsl:value-of select='messagefilter/text()'/><BR/>
</xsl:if>
<xsl:if test='string-length(filefilter/text()) &gt; 0'>
  <xsl:text>File filter:  type:</xsl:text><xsl:value-of select='messagefilter/@filetype'/><xsl:text> string: </xsl:text><xsl:value-of select='filefilter/text()'/><BR/>
</xsl:if>
<xsl:if test='string-length(sort/@orderby) &gt; 0'>
  <xsl:text>Sort order by: </xsl:text><xsl:value-of select='sort/@orderby'/><xsl:text> </xsl:text><xsl:value-of select='sort/@direction'/><BR/>
</xsl:if>
</xsl:template>
<!-- -->

<!-- changelog/summary -->
<xsl:template match="changelog/summary">
<xsl:if test='string-length(changecount/text()) &gt; 0'>
  <xsl:text>Number of changes: </xsl:text><xsl:value-of select='changecount/text()'/><BR/>
</xsl:if>
<xsl:if test='count(developers/developer) &gt; 0'>
  <xsl:text>Developers: </xsl:text><BR/>
  <blockquote>
    <xsl:apply-templates select="developers/developer"/>
  </blockquote>
</xsl:if>
<xsl:if test='count(mostchangedfiles/mostchangedfile) &gt; 0'>
  <xsl:text>Most changed files: </xsl:text><BR/>
  <blockquote>
    <xsl:apply-templates select="mostchangedfiles/mostchangedfile"/>
  </blockquote>
</xsl:if>
<xsl:if test='count(mostactivedevelopers/mostactivedeveloper) &gt; 0'>
  <xsl:text>Most active developers: </xsl:text><BR/>
  <blockquote>
    <xsl:apply-templates select="mostactivedevelopers/mostactivedeveloper"/>
  </blockquote>
</xsl:if>
</xsl:template>

<!-- changelog/summary/developers/developer -->
<xsl:template match="changelog/summary/developers/developer">
  <xsl:value-of select='text()'/><BR/>
</xsl:template>

<!-- changelog/summary/mostchangedfiles/mostchangedfile -->
<xsl:template match="changelog/summary/mostchangedfiles/mostchangedfile">
  <xsl:value-of select='text()'/><BR/>
</xsl:template>

<!-- changelog/summary/mostactivedevelopers/mostactivedeveloper -->
<xsl:template match="changelog/summary/mostactivedevelopers/mostactivedeveloper">
  <xsl:value-of select='text()'/><BR/>
</xsl:template>

<!-- changelog/entry/file -->
<xsl:template match="changelog/entry/file">
<xsl:call-template name="makeReference">
  <xsl:with-param name="href">
  <xsl:text disable-output-escaping="yes">http://www.netbeans.org/source/browse/</xsl:text>
  <xsl:value-of select='substring-after(current()/name/text(), "/cvs/")'/>
  </xsl:with-param>
  <xsl:with-param name="title"><xsl:value-of select='substring-after(current()/name/text(), "/cvs/")'/>
  </xsl:with-param>
</xsl:call-template><xsl:text>:</xsl:text>
<xsl:text> (</xsl:text>
<xsl:call-template name="makeReference">
  <xsl:with-param name="href">
  <xsl:text disable-output-escaping="yes">http://www.netbeans.org/source/browse/</xsl:text>
  <xsl:value-of select='substring-after(current()/name/text(), "/cvs/")'/><xsl:text>?rev=</xsl:text><xsl:value-of select="current()/revision/text()"/><xsl:text>&amp;content-type=text/x-cvsweb-markup</xsl:text>
  </xsl:with-param>
  <xsl:with-param name="title"><xsl:value-of select="current()/revision/text()"/>
  </xsl:with-param>
</xsl:call-template><xsl:text>)</xsl:text>
<xsl:if test="not (position()=last())"><BR/></xsl:if><xsl:text>
</xsl:text>
</xsl:template>

<!-- changelog/entry/file/name -->
<xsl:template match="changelog/entry/file/name">
<xsl:value-of select="text()"/>
</xsl:template>

<!-- changelog/entry/file/branch -->
<xsl:template match="changelog/entry/file/branch">
<xsl:value-of select="text()"/>
</xsl:template>

<!-- changelog/entry/file/revision -->
<xsl:template match="changelog/entry/file/revision">
<xsl:value-of select="text()"/>
</xsl:template>

<!-- changelog/entry/author -->
<xsl:template match="changelog/entry/author">
<B>
<xsl:call-template name="makeReference">
  <xsl:with-param name="href">
  <xsl:text disable-output-escaping="yes">mailto:</xsl:text>
  <xsl:value-of select="text()"/><xsl:text>@netbeans.org</xsl:text>
  </xsl:with-param>
  <xsl:with-param name="title"><xsl:value-of select="text()"/>
  </xsl:with-param>
</xsl:call-template></B>
</xsl:template>

<!-- changelog/entry/date -->
<xsl:template match="changelog/entry/date">
<B><xsl:value-of select="text()"/></B>
</xsl:template>

<!-- changelog/entry/time -->
<xsl:template match="changelog/entry/time">
<B><xsl:value-of select="text()"/></B>
</xsl:template>

<!-- changelog/entry/msg -->
<!-- what about text wrapping ? -->
<xsl:template match="changelog/entry/msg">
<xsl:value-of select="text()"/>
</xsl:template>

<!--
     NAMED TEMPLATES
                     -->
<!-- makeLink -->
<xsl:template name="makeLink">
<xsl:param name="name"  select="''"/>
<xsl:param name="title" select="''"/>
<xsl:text disable-output-escaping="yes">&lt;A name="</xsl:text>
<xsl:value-of select="$name"/>
<xsl:text disable-output-escaping="yes">"&gt;</xsl:text>
<xsl:value-of select="$title"/>
<xsl:text disable-output-escaping="yes">&lt;/A&gt;</xsl:text>
</xsl:template>

<!-- makeReference -->
<xsl:template name="makeReference">
<xsl:param name="href"  select="''"/>
<xsl:param name="title" select="''"/>
<xsl:text disable-output-escaping="yes">&lt;A href="</xsl:text>
<xsl:value-of select="$href"/>
<xsl:text disable-output-escaping="yes">"&gt;</xsl:text>
<xsl:value-of select="$title"/>
<xsl:text disable-output-escaping="yes">&lt;/A&gt;</xsl:text>
</xsl:template>

</xsl:stylesheet>
