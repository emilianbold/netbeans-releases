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
    
    <xsl:param name="arch.stylesheet"/>
    <xsl:param name="arch.overviewlink"/>
    <xsl:param name="arch.footer"/>

    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="api-answers/@module" /> - NetBeans Architecture Questions</title>
                <xsl:if test="$arch.stylesheet">
                    <link rel="stylesheet" type="text/css" href="{$arch.stylesheet}"/>
                </xsl:if>
            </head>
            <body>
            
                <xsl:if test="$arch.overviewlink">
                    <p class="overviewlink"><a href="{$arch.overviewlink}">Overview</a></p>
                </xsl:if>
            
                <h1>NetBeans Architecture Answers for <xsl:value-of select="api-answers/@module" /><xsl:text> module</xsl:text></h1>
                
                <xsl:variable name="qver" select="substring-before(substring-after(api-answers/api-questions/@version,'Revision: '),' $')" />
                <xsl:variable name="aver" select="substring-before(substring-after(api-answers/@version,'Revision:'),' $')" />
                <xsl:variable name="afor" select="api-answers/@question-version" />
                
                <ul>
                <li><b>Author:</b><xsl:text> </xsl:text><xsl:value-of select="api-answers/@author" /></li>
                <li><b>Version of answers:</b><xsl:text> </xsl:text><xsl:value-of select="$aver" /></li>
                <li><b>Answers for questions:</b><xsl:text> </xsl:text><xsl:value-of select="$afor" /></li>
                <li><b>Version of questions:</b><xsl:text> </xsl:text><xsl:value-of select="$qver" /></li>
                </ul>
                
                <xsl:if test="not($qver=$afor)">
                    <b> WARNING: Version of questions is different than 
                        those that these answers are written for! 
                        Is: "<xsl:value-of select="$qver"/>" and should 
                        be "<xsl:value-of select="$afor" />".
                    </b>
                </xsl:if>
            
                <xsl:apply-templates />    
                
                <hr/>
                
                <h2>Interfaces table</h2>
                
                <xsl:variable name="all_interfaces" select="//api" />
                
                <xsl:if test="not($all_interfaces)" >
                    <b> WARNING: No imported or exported interfaces! </b>
                </xsl:if>
             
                <table border="1" width="100%" >   
                    <thead>
                        <th valign="bottom" width="25%"><b>Interface Name</b></th>
                        <th valign="bottom" width="10%"><b>In/Out</b></th>
                        <th valign="bottom" width="10%"><b>Stability</b></th>
                        <th valign="bottom" ><b>Specified in What Document?</b></th>
                    </thead>
                
                    <xsl:for-each select="$all_interfaces">
                        <tr/>
                        <xsl:call-template name="api" />
                    </xsl:for-each>
                </table>
                
                <hr/>
                
                <xsl:variable name="all_properties" select="//property" />
                <xsl:if test="$all_properties">
                
                    <h2>Properties table</h2>
                
                    <table border="1" width="100%" >   
                        <thead>
                            <th valign="bottom" width="25%"><b>Name</b></th>
                            <th valign="bottom" width="10%"><b>Stability</b></th>
                            <th valign="bottom" ><b>Specified in What Document?</b></th>
                        </thead>

                        <xsl:for-each select="$all_properties">
                            <tr/>
                            <xsl:call-template name="property" />
                        </xsl:for-each>
                    </table>
                    <hr/>
                </xsl:if>
                
                <xsl:if test="$arch.footer">
                    <p><xsl:value-of select="$arch.footer"/></p>
                </xsl:if>
                
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="category">
        <hr/>
        <h2>
            <xsl:value-of select="@name" />
        </h2>
        <ul>
            <xsl:for-each select="question">
                <xsl:call-template name="answer" />
            </xsl:for-each>
        </ul>
    </xsl:template>
    

    <xsl:template name="answer">
        <xsl:variable name="value" select="@id" />
    
        <p/>
        <font color="gray" >
        <b>Question (<xsl:value-of select="@id"/>):</b> <em><xsl:apply-templates select="./node()" /></em>
        </font>
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
        <xsl:call-template name="api-line" >
            <xsl:with-param name="name" select="@name" />
            <xsl:with-param name="type" select="@type" />
            <xsl:with-param name="group">api</xsl:with-param>
            <xsl:with-param name="category" select="@category" />
            <xsl:with-param name="describe.url" select="@url" />
            <xsl:with-param name="describe.node" select="./node()" />
        </xsl:call-template>
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
        <xsl:call-template name="api-line" >
            <xsl:with-param name="group">property</xsl:with-param>
            <xsl:with-param name="name" select="@name" />
            <xsl:with-param name="category" select="@category" />
            <xsl:with-param name="describe.url" select="@url" />
            <xsl:with-param name="describe.node" select="./node()" />
        </xsl:call-template>
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
    
    
    <!-- the template to convert an instances of API into an HTML line in a table 
      describing the API -->

    <xsl:template name="api-line" >
       <xsl:param name="name" />
       <xsl:param name="group" />
       <xsl:param name="category" />
       <xsl:param name="type" /> <!-- can be left empty -->
       
       <xsl:param name="describe.url" />
       <xsl:param name="describe.node" />

        <tbody>
            <td>
                <a>
                    <xsl:attribute name="name" >
                        <xsl:value-of select="$group" /><xsl:text>-</xsl:text><xsl:value-of select="$name" />
                    </xsl:attribute>
                    <xsl:value-of select="$name" />
                </a>
            </td>
            <xsl:if test="$type" > 
                <td> <!-- imported/exported -->
                    <xsl:choose>
                        <xsl:when test="$type='import'">Imported</xsl:when>
                        <xsl:when test="$type='export'">Exported</xsl:when>
                        <xsl:otherwise>WARNING: <xsl:value-of select="$type" /></xsl:otherwise>
                    </xsl:choose>
                </td>
            </xsl:if>
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
                <xsl:call-template name="describe">
                  <xsl:with-param name="describe.url" select="$describe.url" />
                  <xsl:with-param name="describe.node" select="$describe.node" />
                </xsl:call-template>
            </td>
        </tbody>
    </xsl:template>  
    <xsl:template name="describe">
       <xsl:param name="describe.url" />
       <xsl:param name="describe.node" />
       
       
       <xsl:if test="$describe.url" >
            <a>
                <xsl:attribute name="href" >
                    <xsl:value-of select="$describe.url" />
                </xsl:attribute>
                <xsl:value-of select="$describe.url" />
            </a>
           
           <xsl:if test="$describe.node" >
               <p/>
           </xsl:if>
       </xsl:if>
       
       <xsl:if test="$describe.node" >
           <xsl:apply-templates select="$describe.node" />
       </xsl:if>
    </xsl:template>
        
</xsl:stylesheet> 
