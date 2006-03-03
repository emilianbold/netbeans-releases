<?xml version="1.0" encoding="UTF-8" ?>
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


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes" />
    

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <project basedir="." default="all" name="coverage"> 
            <xsl:apply-templates select="testconfig/config"/> 
        </project>
    </xsl:template>
    
    <xsl:template match="config">
        <target name="{@name}" >
       <xsl:apply-templates select="module"/>
        </target>   
    </xsl:template>
    
    <xsl:template match="module">
        <xsl:element name="ant">
          <xsl:attribute name="dir"  ><xsl:text>../../</xsl:text><xsl:value-of select="@name"/>/test</xsl:attribute>
          <xsl:attribute name="target">excludeFailedTests</xsl:attribute>
        
          <xsl:element name="property">
                <xsl:attribute name="name">xtest.testtype</xsl:attribute>  
                <xsl:attribute name="value"><xsl:value-of select="@testtypes"/></xsl:attribute>
          </xsl:element>
          <xsl:element name="property">
                <xsl:attribute name="name">xtest.attribs</xsl:attribute>  
                <xsl:attribute name="value"><xsl:value-of select="@attributes"/></xsl:attribute>
          </xsl:element>
        </xsl:element>
    </xsl:template>    
</xsl:stylesheet>
