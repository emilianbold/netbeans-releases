<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : api-questions-to-html.xsl
    Created on : November 4, 2002, 4:51 PM
    Author     : jarda
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>

    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="api-answers/@module" /> - NetBeans Architecture Questions</title>
            </head>
            <body>
            
                <H1>NetBeans Architecture Answers for <xsl:value-of select="api-answers/@module" /><xsl:text> module</xsl:text></H1>
                
                <xsl:variable name="qver" select="substring-before(substring-after(api-answers/api-questions/@version,'Revision: '),' $')" />
                <xsl:variable name="aver" select="substring-before(substring-after(api-answers/@version,'Revision:'),' $')" />
                <xsl:variable name="afor" select="api-answers/@question-version" />
                
                <UL>
                <LI><B>Author:</B> <xsl:value-of select="api-answers/@author" /></LI>
                <LI><B>Version of answers:</B> <xsl:value-of select="$aver" /></LI>
                <LI><B>Answers for questions:</B> <xsl:value-of select="$afor" /></LI>
                <LI><B>Version of questions:</B> <xsl:value-of select="$qver" /></LI>
                </UL>
                
                <xsl:if test="not($qver=$afor)">
                    <B> WARNING: Version of questions is different than 
                        those that these answers are written for! 
                        Is: "<xsl:value-of select="$qver"/>" and should 
                        be "<xsl:value-of select="$afor" />".
                    </B>
                </xsl:if>
            
                <xsl:apply-templates />    
                
                <HR/>
                
                <h2>Interfaces table</h2>
                
                <xsl:variable name="all_interfaces" select="//api" />
                
                <xsl:if test="not($all_interfaces)" >
                    <B> WARNING: No imported or exported interfaces! </B>
                </xsl:if>
             
                <table border="1" >   
                    <thead>
                        <td>Name</td>
                        <td>Imported/Exported</td>
                        <td>Stability category</td>
                        <td>Reference URL</td>
                    </thead>
                
                    <xsl:for-each select="$all_interfaces">
                        <xsl:call-template name="api" />
                    </xsl:for-each>
                </table>
                
                <HR/>
                
                <xsl:variable name="all_properties" select="//property" />
                <xsl:if test="$all_properties">
                
                    <h2>Properties table</h2>
                
                    <table border="1" >   
                        <thead>
                            <td>Name</td>
                            <td>Stability category</td>
                            <td>Description</td>
                        </thead>

                        <xsl:for-each select="$all_properties">
                            <xsl:call-template name="property" />
                        </xsl:for-each>
                    </table>
                    <HR/>
                </xsl:if>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="category">
        <hr/>
        <h2>
            <xsl:value-of select="@name" />
        </h2>
        <UL>
            <xsl:for-each select="question">
                <xsl:call-template name="answer" />
            </xsl:for-each>
        </UL>
    </xsl:template>
    

    <xsl:template name="answer">
        <xsl:variable name="value" select="@id" />
    
        <p/>
        <b>Question (<xsl:value-of select="@id"/>):</b> <em><xsl:apply-templates select="./node()" /></em>
        <p/>
        
        <xsl:choose>
            <xsl:when test="count(//answer[@id=$value])" >
                <b>Answer:</b> <!-- <xsl:value-of select="//answer[@id=$value]" /> -->
                <xsl:apply-templates select="//answer[@id=$value]/node()" />
            </xsl:when>
            <xsl:otherwise>
                <b>WARNING:</b>
                <xsl:text> Question with id="</xsl:text>
                <i> 
                <xsl:value-of select="@id" />
                </i>
                <xsl:text>" has not been answered!</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="api">
        <!-- generates link to given API -->
        <xsl:variable name="name" select="@name" />
        
        <a>
            <xsl:attribute name="href" >
                <xsl:text>#api-</xsl:text><xsl:value-of select="$name" />
            </xsl:attribute>
            <xsl:value-of select="$name" />
        </a>
        
    </xsl:template>

    <xsl:template name="api">
        <xsl:variable name="name" select="@name" />
        <xsl:variable name="type" select="@type" />
        <xsl:variable name="category" select="@category" />
        <xsl:variable name="url" select="@url" />

        <tbody>
            <td>
                <a>
                    <xsl:attribute name="name" >
                        <xsl:text>api-</xsl:text><xsl:value-of select="$name" />
                    </xsl:attribute>
                    <xsl:value-of select="$name" />
                </a>
            </td>
            <td> <!-- imported/exported -->
                <xsl:choose>
                    <xsl:when test="$type='import'">Imported</xsl:when>
                    <xsl:when test="$type='export'">Exported</xsl:when>
                    <xsl:otherwise>WARNING: <xsl:value-of select="$type" /></xsl:otherwise>
                </xsl:choose>
            </td>
            <td> <!-- stability category -->
                <xsl:choose>
                    <xsl:when test="$category='official'">Official</xsl:when>
                    <xsl:when test="$category='stable'">Stable</xsl:when>
                    <xsl:when test="$category='devel'">Under Development</xsl:when>
                    <xsl:when test="$category='third'">Third party</xsl:when>
                    <xsl:when test="$category='standard'">Standard</xsl:when>
                    <xsl:when test="$category='friend'">Friend private</xsl:when>
                    <xsl:when test="$category='private'">Private</xsl:when>
                    <xsl:when test="$category='deprecated'">Deprecated</xsl:when>
                    <xsl:otherwise>WARNING: <xsl:value-of select="$category" /></xsl:otherwise>
                </xsl:choose>
            </td>
            
            <td> <!-- url -->
                <a>
                    <xsl:attribute name="href" >
                        <xsl:value-of select="$url" />
                    </xsl:attribute>
                    <xsl:value-of select="$url" />
                </a>
            </td>
        </tbody>
            
    </xsl:template>
    
    <xsl:template match="property">
        <!-- generates link to given API -->
        <xsl:variable name="name" select="@name" />
        
        <a>
            <xsl:attribute name="href" >
                <xsl:text>#property-</xsl:text><xsl:value-of select="$name" />
            </xsl:attribute>
            <xsl:value-of select="$name" />
        </a>
        
    </xsl:template>

    <xsl:template name="property">
        <xsl:variable name="name" select="@name" />
        <xsl:variable name="category" select="@category" />

        <tbody>
            <td>
                <a>
                    <xsl:attribute name="name" >
                        <xsl:text>property-</xsl:text><xsl:value-of select="$name" />
                    </xsl:attribute>
                    <xsl:value-of select="$name" />
                </a>
            </td>
            <td> <!-- stability category -->
                <xsl:choose>
                    <xsl:when test="$category='official'">Official</xsl:when>
                    <xsl:when test="$category='stable'">Stable</xsl:when>
                    <xsl:when test="$category='devel'">Under Development</xsl:when>
                    <xsl:when test="$category='third'">Third party</xsl:when>
                    <xsl:when test="$category='standard'">Standard</xsl:when>
                    <xsl:when test="$category='friend'">Friend private</xsl:when>
                    <xsl:when test="$category='private'">Private</xsl:when>
                    <xsl:when test="$category='deprecated'">Deprecated</xsl:when>
                    <xsl:otherwise>WARNING: <xsl:value-of select="$category" /></xsl:otherwise>
                </xsl:choose>
            </td>
            
            <td> <!-- description -->
                    <xsl:apply-templates select="./node()" />
            </td>
        </tbody>
            
    </xsl:template>

    <!-- Format random HTML elements as is: -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
  
  
    <xsl:template match="answer">
        <!-- ignore direct answers -->
    </xsl:template>
    <xsl:template match="hint">
        <!-- ignore direct answers -->
    </xsl:template>
        
</xsl:stylesheet> 
