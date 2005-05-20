<?xml version="1.0" encoding="UTF-8" ?>
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
    <xsl:output method="html"/>

    <xsl:template match="/" >
        <html>
        <head>
            <!-- projects.netbeans.org -->
           <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
           <title>All NetBeans Classes</title>
           <link rel="stylesheet" href="org-openide-util/javadoc.css" type="text/css" title="style" />
        </head>

        <body>
        <font size="+1" CLASS="FrameHeadingFont">
            <b>NetBeans API Classes</b>
        </font>
        
        <TABLE BORDER="0" WIDTH="100%" SUMMARY="">
        <TR>
        <TD NOWRAP=""><FONT CLASS="FrameItemFont">
        
            <xsl:for-each select="//class" >
                <xsl:sort order="ascending" select="@name" />
                <xsl:call-template name="class" />
            </xsl:for-each>
            
        </FONT></TD>
        </TR>
        </TABLE>
            
        </body>
        </html>
    </xsl:template>
    
    <xsl:template name="class">
        <a>
            <xsl:attribute name="href"><xsl:value-of select="@url" /></xsl:attribute>
            <xsl:attribute name="target">classFrame</xsl:attribute>
            
            <xsl:choose>
                <xsl:when test="@interface = 'true'" >
                    <i><xsl:value-of select="@name" /></i>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@name" />
                </xsl:otherwise>
            </xsl:choose>
        </a>
        <br/>
    </xsl:template>
    
</xsl:stylesheet>


