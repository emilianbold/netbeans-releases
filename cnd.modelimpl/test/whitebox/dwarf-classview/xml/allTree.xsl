<!--
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 -->

<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : newstylesheet.xsl
    Created on : January 24, 2007, 8:38 PM
    Author     : ai155158
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:output method="html" encoding="Windows-1252"/>
    
    <xsl:variable name='depth'>1</xsl:variable>
    
    <xsl:template match="/node">
        <html>
            <head>
                <title>modified.xsl</title>
                <link href="style.css" rel="stylesheet" type="text/css" />
                <script src='tree.js'></script>
            </head>
            <body onLoad='initTree()'>
                <div style='border-top:#FF9843;border-left:white;border-bottom:#FF9843; border-right:white;border-style:solid;border-width:1.0pt;padding:0cm 0cm 0cm 0cm'>
                    <h2>
                        PROJECT: <xsl:value-of select="@displayedName"/>
                    </h2>
                </div>
                
                <table>
                    <tr>
                        <span class="info">
                            <td align="left">All nodes: </td>
                            <td align="left"><xsl:value-of select="@allNodes"/></td>
                        </span>
                        <span class="info_changed">
                            <td align="left">Changed nodes: </td>
                            <td align="left"><xsl:value-of select="@changedNodes"/></td>
                        </span>
                        <span class="info_extra">
                            <td align="left">Extra nodes: </td>
                            <td align="left"><xsl:value-of select="@extraNodes"/></td>
                        </span>
                        <span class="info_absent">
                            <td align="left">Absent nodes: </td>
                            <td align="left"><xsl:value-of select="@absentNodes"/></td>
                        </span>
                    </tr>
                </table>
                
                <div class="root" expanded="true">
                    <img border="0" height="16" onClick="toggle(this)" allign="middle" src="./images/rootExpanded.gif"/>
                    <img class="scope" src="./images/projectMy.png"/>
                    <span class="root">
                        <xsl:value-of select="@displayedName"/>
                    </span>
                    <xsl:apply-templates select="node">
                        <xsl:with-param name="level" select="$depth"/>
                    </xsl:apply-templates>
                </div>
                
            </body>
        </html>
    </xsl:template>
    
    
    <xsl:template match="node">
        <xsl:param name="level" />
        <xsl:variable name="children" select="count(child::*)" />
        
        <xsl:element name="div">
            <xsl:choose>
                <xsl:when test="$children > 0">
                    <xsl:attribute name="class">scope</xsl:attribute>
                    <xsl:attribute name="expanded">false</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="class">item</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            
            <xsl:if test="$level > 1">
                <xsl:attribute name="style">display:none</xsl:attribute>
            </xsl:if>
            
            <xsl:call-template name='drawDepthLines'>
                <xsl:with-param name='depth' select='$level'/>
                <xsl:with-param name='maxDepth' select='$level'/>
            </xsl:call-template>
            
            <xsl:choose>
                <xsl:when test="$children > 0">
                    <img class="toggler" height="16" onClick="toggle(this)" allign="middle" src="./images/collapsed.gif"/>
                </xsl:when>
                <xsl:otherwise>
                    <img class="itemLine" height="16" align="middle" border="0" src="./images/itemLine.gif"/>
                </xsl:otherwise>
            </xsl:choose>
            
            <xsl:call-template name="nodeItself">
                <xsl:with-param name="type"><xsl:value-of select="@type"/></xsl:with-param>
                <xsl:with-param name="children"><xsl:value-of select="$children"/></xsl:with-param>
            </xsl:call-template>
            
            <xsl:variable name="dNameClass">
                <xsl:choose>
                    <xsl:when test='@mark = "diffInNode"'>dName_changed</xsl:when>
                    <xsl:when test='@mark = "extraNode"'>dName_extra</xsl:when>
                    <xsl:when test='@mark = "absentNode"'>dName_absent</xsl:when>
                    <xsl:otherwise>dName</xsl:otherwise>
                </xsl:choose>    
            </xsl:variable>            
            
            <xsl:element name="span">
                <xsl:attribute name="class"><xsl:value-of select="$dNameClass"/></xsl:attribute>
                <xsl:attribute name="onClick">selectThisTreeNode(this)</xsl:attribute>
                <xsl:value-of select="@displayedName"/> <xsl:value-of select="@dwarfInfo"/>
            </xsl:element>
            
            <!--            <span class='dName' onClick='selectThisTreeNode(this)'>
                <xsl:value-of select="@displayedName"/>
            </span>
-->            
            <xsl:if test="$children > 0">
                <xsl:apply-templates select="node">
                    <xsl:with-param name="level" select="number($level)+1"/>
                </xsl:apply-templates>
            </xsl:if>
            
        </xsl:element>
        
    </xsl:template>
    
    <xsl:template name="nodeItself">
        <xsl:param name="type"/>
        <xsl:param name="children"/>
        
        <xsl:variable name="itemImage">
            <xsl:choose>
                <xsl:when test='$type = "namespace"'>./images/namespace_16.png</xsl:when>
                <xsl:when test='$type = "class"'>./images/class_16.png</xsl:when>
                <xsl:when test='$type = "struct"'>./images/struct_16.png</xsl:when>
                <xsl:when test='$type = "union"'>./images/union_16.png</xsl:when>
                <xsl:when test='$type = "function"'>./images/global_function.png</xsl:when>
                <xsl:when test='$type = "method"'>./images/methods.png</xsl:when>
                <xsl:when test='$type = "operator"'>./images/operator_16.png</xsl:when>
                <xsl:when test='$type = "variable"'>./images/global_variable.png</xsl:when>
                <xsl:when test='$type = "member"'>./images/fields.png</xsl:when>
                <xsl:when test='$type = "typedef"'>./images/typedef_16.png</xsl:when>
                <xsl:when test='$type = "enum"'>./images/enumeration_16.png</xsl:when>
                <xsl:otherwise>other</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:element name="img">
            <xsl:attribute name="src"><xsl:value-of select="$itemImage"/></xsl:attribute>
            <xsl:choose>
                <xsl:when test='children = 0'>
                    <xsl:attribute name="height">16</xsl:attribute>
                    <xsl:attribute name="align">top</xsl:attribute>
                    <xsl:attribute name="border">0</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="class">scope</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>
    
    <xsl:template name='drawDepthLines'>
        <xsl:param name='depth'/>
        <xsl:param name='maxDepth'/>
        <xsl:if test='$depth!=0'>
            <img class='treeLine' depth='{number($maxDepth)-number($depth)}' src='./images/depthLine.gif'/>
            <xsl:call-template name='drawDepthLines'>
                <xsl:with-param name='depth' select='number(number($depth)-1)'/>
                <xsl:with-param name='maxDepth' select='$maxDepth'/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>
