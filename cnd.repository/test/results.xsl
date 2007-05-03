<!--
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 -->

<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" 
        media-type="text/html" 
        doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
        doctype-system="DTD/xhtml1-strict.dtd"
        cdata-section-elements="script style"
        indent="yes"
        encoding="ISO-8859-1"/>
    
    <xsl:template match="/repository-tests">
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <title>Repository tests from <xsl:value-of select="@date"/></title> 
                <style>
                    *{font-family: tahoma; font-size: 12px; color: #333333;}
                    a:link, a:hover {color: #9B002C; text-decoration:none}
                    a:active, a:visited {color: #852641;}			
                    .red {color: #EE0000; }
                    .green {color: #22EE22; }
                </style>			
            </head>
            <body>
                <h3>Tests from <xsl:value-of select="@date"/></h3>
                <table> 
                    <xsl:apply-templates select="test"/>
                </table>
                <h3>Memory Working Set</h3>
                <table> 
                    <xsl:apply-templates select="test-mws"/>
                </table>
            </body>
        </html>        
    </xsl:template>
    
    <xsl:template match="test">
        <tr><td align="right" valign="top" nowrap=""><xsl:value-of select="@name"/>:</td><xsl:apply-templates select="result"/></tr>
        <tr><td align="right" valign="top">logs:</td><td><xsl:apply-templates select="log"/></td></tr>
    </xsl:template>
    
    <xsl:template match="test-mws">
        <tr><td nowrap="">
            <xsl:value-of select="project/text()"/> on <xsl:value-of select="memory/text()"/>
            <xsl:choose>
                <xsl:when test="repository/text()='true'"> with</xsl:when>
                <xsl:when test="repository/text()='false'"> w/o</xsl:when>
            </xsl:choose>:
        </td>
        <xsl:choose>
            <xsl:when test="result">
                <xsl:apply-templates select="result"/>
                <td>(<xsl:value-of select="round(parsetime div 1000)"/> s)</td>
            </xsl:when>
            <xsl:otherwise>
                <td colspan="2" class="red">crushed</td>
            </xsl:otherwise>
        </xsl:choose>
        <td><xsl:apply-templates select="log"/></td>
        </tr>
    </xsl:template>
    
    <xsl:template match="log">
        <a>
            <xsl:attribute name="href"><xsl:value-of select="text()"/></xsl:attribute>
            <xsl:value-of select="@name"/>
        </a><br/>
    </xsl:template>
    
    <xsl:template match="result">
        <td>
            <xsl:choose>
                <xsl:when test="text()='passed'">
                    <xsl:attribute name="class">green</xsl:attribute>
                    passed
                </xsl:when>
                <xsl:when test="text()='failed'">
                    <xsl:attribute name="class">red</xsl:attribute>
                    failed
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="class">red</xsl:attribute>
                    N/A
                </xsl:otherwise>		
            </xsl:choose>
        </td>
    </xsl:template>
    
    <xsl:template match="param">
        <tr><td align="right" valign="top"><xsl:value-of select="@name"/>:</td><td><xsl:value-of select="@value"/></td></tr>
    </xsl:template>
</xsl:stylesheet>
