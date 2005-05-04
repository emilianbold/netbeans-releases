<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : coverage.xsl
    Created on : April 20, 2005, 1:49 PM
    Author     : pzajac
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes" />
    

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <project basedir="." default="all" name="coverage"> 
            <property name="netbeans.dest.src.dir" location="../../nbbuild/netbeans"/> 
            <property name="tmp.netbeans.dir"  location="coveragebuild"/> 
            <property name="coverage.results"  location="coverageResults"/> 
            <xsl:apply-templates select="testconfig/config"/> 
        </project>
    </xsl:template>
    
    <xsl:template match="config">
        <target name="{@name}" >
       <xsl:apply-templates select="module"/>
        </target>   
    </xsl:template>
    
    <xsl:template match="module">
        <xsl:element name="delete">
            <xsl:attribute name="dir">
                <xsl:text disable-output-escaping="yes">${tmp.netbeans.dir}</xsl:text>
            </xsl:attribute>
        </xsl:element>        
        <xsl:element name="mkdir">
          <xsl:attribute name="dir">
            <xsl:text disable-output-escaping="yes">${tmp.netbeans.dir}</xsl:text>
          </xsl:attribute>
        </xsl:element>  
        <xsl:element name="copy">
            <xsl:attribute name="todir">
                <xsl:text disable-output-escaping="yes">${tmp.netbeans.dir}</xsl:text>
             </xsl:attribute>
             <xsl:element name="fileset">
                 <xsl:attribute name="dir">
                    <xsl:text disable-output-escaping="yes">${netbeans.dest.src.dir}</xsl:text>
                 </xsl:attribute>
             </xsl:element>   

        </xsl:element>
        <xsl:element name="chmod"> 
            <xsl:attribute name="file">${tmp.netbeans.dir}/bin/netbeans</xsl:attribute>
            <xsl:attribute name="perm">+x</xsl:attribute>
       </xsl:element>      
        <xsl:element name="chmod"> 
            <xsl:attribute name="file">${tmp.netbeans.dir}/platform5/lib/nbexec</xsl:attribute>
            <xsl:attribute name="perm">+x</xsl:attribute>
       </xsl:element>
            
        <echo>tmp.netbeans.dir = ${tmp.netbeans.dir}</echo>          
        <echo>netbeans.dest.dir = ${netbeans.dest.dir}</echo>
        <xsl:element name="ant">
          <xsl:attribute name="dir"  ><xsl:text>../../</xsl:text><xsl:value-of select="@name"/>/test</xsl:attribute>
          <xsl:attribute name="target">coverage</xsl:attribute>
        
          <xsl:element name="property">
                <xsl:attribute name="name">xtest.testtype</xsl:attribute>  
                <xsl:attribute name="value"><xsl:value-of select="@testtypes"/></xsl:attribute>
          </xsl:element>
          <xsl:element name="property">
                <xsl:attribute name="name">xtest.attribs</xsl:attribute>  
                <xsl:attribute name="value"><xsl:value-of select="@attributes"/></xsl:attribute>
          </xsl:element>
          <xsl:element name="property">
                <xsl:attribute name="name">netbeans.dest.dir</xsl:attribute>  
               <xsl:attribute name="location"><xsl:text disable-output-escaping="yes">${tmp.netbeans.dir}</xsl:text>
               </xsl:attribute>
          </xsl:element>
        </xsl:element>
          
        <xsl:element name="copy">
            <xsl:attribute name="todir">${coverage.results}/<xsl:value-of select="@name"/></xsl:attribute>
            <xsl:element name="fileset">
                <xsl:attribute name="dir">../../<xsl:value-of select="@name"/>/test/coverage</xsl:attribute>        
            </xsl:element>    
        </xsl:element>
   
      
    </xsl:template>    
</xsl:stylesheet>
