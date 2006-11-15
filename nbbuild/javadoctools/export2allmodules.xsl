<?xml version="1.0" encoding="UTF-8" ?>
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
    <xsl:output method="html"/>

    <xsl:template match="/" >
        <html>
        <head>
            <!-- projects.netbeans.org -->
           <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
           <title>All NetBeans Classes</title>
           <link rel="stylesheet" href="org-openide-util/javadoc.css" type="text/css"/>
        </head>

        <body>


        <TABLE BORDER="0" WIDTH="100%" CELLPADDING="1" CELLSPACING="0" SUMMARY="">
        <TR>
        <TD COLSPAN="2" BGCOLOR="#EEEEFF" CLASS="NavBarCell1">
        <A NAME="navbar_top_firstrow"><!-- --></A>
        <TABLE BORDER="0" CELLPADDING="0" CELLSPACING="3" SUMMARY="">
          <TR ALIGN="center" VALIGN="top">
          <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    
            <a>
                <xsl:attribute name="href">overview-summary.html</xsl:attribute>
                <xsl:attribute name="target">classFrame</xsl:attribute>
                <FONT CLASS="NavBarFont1"><B>Overview</B></FONT>
            </a>
          </TD>
          <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    
            <a>
                <xsl:attribute name="href">allclasses-frame.html</xsl:attribute>
                <xsl:attribute name="target">packageFrame</xsl:attribute>
                <FONT CLASS="NavBarFont1"><B>AllClasses</B></FONT>
            </a>
          </TD>
          <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    
            <a>
                <xsl:attribute name="href">usecases.html</xsl:attribute>
                <xsl:attribute name="target">classFrame</xsl:attribute>
                <FONT CLASS="NavBarFont1"><B>UseCases</B></FONT>
            </a>
          </TD>
          </TR>
        </TABLE>
        </TD>
        <TD ALIGN="right" VALIGN="top" ROWSPAN="3"><EM>
        </EM>
        </TD>
        </TR>
        </TABLE>
        
        <TABLE BORDER="0" WIDTH="100%" SUMMARY="">
        <TR>
        <TD NOWRAP=""><FONT CLASS="FrameItemFont">
            <xsl:for-each select="//module[not (@name = '_no module_')]" >
                <xsl:sort order="ascending" select="@name" />
                <xsl:call-template name="module" />
            </xsl:for-each>
        </FONT></TD>
        </TR>
        </TABLE>
        
        </body>
        </html>
    </xsl:template>
    
    <xsl:template name="module">
        <span>
            <xsl:attribute name="style">
                <xsl:choose>
                    <xsl:when test="descendant::api[@category='stable' and @group='java']">background:#ffffff</xsl:when>
                    <xsl:when test="descendant::api[@category='official' and @group='java']">background:#ffffff</xsl:when>
                    <xsl:when test="descendant::api[@category='devel' and @group='java']">background:#ddcc80</xsl:when>
                    <xsl:when test="descendant::api[@category='deprecated' and @group='java']">text-decoration: line-through</xsl:when>
                    <xsl:otherwise>background:#e0c0c0</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <a>
                <xsl:attribute name="href"><xsl:value-of select="substring-before(@target,'/')" />/allclasses-frame.html</xsl:attribute>
                <xsl:attribute name="target">packageFrame</xsl:attribute>

                <xsl:value-of select="@name" />
            </a>
            (<a>
               <xsl:attribute name="href"><xsl:value-of select="substring-before(@target,'/')" />/overview-summary.html</xsl:attribute>
                <xsl:attribute name="target">classFrame</xsl:attribute>
                javadoc
            </a>)
        </span>
        <br/>
    </xsl:template>
    
</xsl:stylesheet>


