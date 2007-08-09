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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.
-->
<xsl:stylesheet xmlns:s="http://xml.netbeans.org/schema/JAXBWizConfig" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes" xmlns:xalan="http://xml.apache.org/xslt"  xalan:indent-amount="4"/>
    <xsl:template match="/">
        <xsl:comment>
            *** GENERATED FROM xml_binding_cfg.xml - DO NOT EDIT  ***
            *** Configure thru JAXB Wizard.                       ***
        </xsl:comment>
        <xsl:element name="project">
            <xsl:attribute name="name"><xsl:value-of select="s:schemas/@projectName"/>_jaxb</xsl:attribute>
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">.</xsl:attribute>            
            <xsl:element name="target">
                <xsl:attribute name="name">xjc-typedef-target</xsl:attribute>
                <xsl:attribute name="depends">-init-project</xsl:attribute>
                <typedef classname="com.sun.tools.xjc.XJCTask" name="xjc">
                    <classpath path="${{libs.jaxb20.classpath}}"/>
                </typedef>
            </xsl:element>
            <xsl:element name="target">
                <xsl:attribute name="name">jaxb-clean-code-generation</xsl:attribute>
                <xsl:attribute name="depends">clean,jaxb-code-generation</xsl:attribute>            
            </xsl:element>
            <xsl:element name="target">
                <xsl:attribute name="name">jaxb-code-generation</xsl:attribute>
                <xsl:attribute name="depends">xjc-typedef-target</xsl:attribute>            
                <mkdir dir="build/generated/addons/jaxb"/>
                <mkdir dir="build/generated/jaxbCache"/>
                <mkdir dir="${{build.classes.dir}}"/>
                <xsl:apply-templates select="s:schemas/s:schema"/>
                <javac destdir="${{build.classes.dir}}" srcdir="build/generated/addons/jaxb" source="${{javac.source}}"  target="${{javac.target}}">
                    <classpath path="${{libs.jaxb20.classpath}}"/>
                </javac>
            </xsl:element>             
        </xsl:element>
    </xsl:template>
    <xsl:template match="s:schema">
        <xsl:element name="mkdir">
            <xsl:attribute name="dir">build/generated/jaxbCache/<xsl:value-of select="./@name"/></xsl:attribute>
        </xsl:element>
        
        <xsl:element name="xjc">
            <xsl:if test="string-length(@package) &gt; 0">
                <xsl:attribute name="package"><xsl:value-of select="./@package"/></xsl:attribute>
            </xsl:if>
            <xsl:attribute name="destdir">build/generated/jaxbCache/<xsl:value-of select="./@name"/></xsl:attribute>            

            <xsl:for-each select="s:catalog">
                <xsl:if test="string-length(./@location) &gt; 0">
                    <xsl:apply-templates select="."/>    
                </xsl:if>
            </xsl:for-each>            
            
            <xsl:element name="arg">
                <xsl:attribute name="value"><xsl:value-of select="./@type"/></xsl:attribute>
            </xsl:element>
            <xsl:for-each select="s:xjc-options/s:xjc-option">
                <xsl:if test="./@value='true'">
                    <xsl:apply-templates select="."/>    
                </xsl:if>
            </xsl:for-each>
            
            <xsl:apply-templates select="s:schema-sources/s:schema-source"/>
            
            <xsl:apply-templates select="s:bindings"/>             
            
            <xsl:element name="depends">
                <xsl:attribute name="file"><xsl:value-of select="s:schema-sources/s:schema-source/@location"/></xsl:attribute>
            </xsl:element>

            <xsl:element name="produces">
                <xsl:attribute name="dir">build/generated/jaxbCache/<xsl:value-of select="./@name"/></xsl:attribute>
            </xsl:element>
        </xsl:element>
        
        <xsl:element name="copy">
            <xsl:attribute name="todir"><xsl:value-of select="/s:schemas/@destdir"/></xsl:attribute>
            <xsl:element name="fileset">
                <xsl:attribute name="dir">build/generated/jaxbCache/<xsl:value-of select="./@name"/></xsl:attribute>
            </xsl:element>
        </xsl:element>
        
    </xsl:template>
    <xsl:template match="s:schema-source">
        <xsl:element name="schema">
            <xsl:attribute name="file"><xsl:value-of select="./@location"/></xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="s:bindings">
        <xsl:if test="count(s:binding) &gt; 0">
            <xsl:for-each select="s:binding">
                <xsl:element name="binding">
                    <xsl:attribute name="file"><xsl:value-of select="./@location"/></xsl:attribute>
                </xsl:element>                
            </xsl:for-each>
        </xsl:if>            
    </xsl:template>    
    
    <xsl:template match="s:xjc-option">
        <xsl:element name="arg">
            <xsl:attribute name="value"><xsl:value-of select="./@name"/></xsl:attribute>
        </xsl:element>                        
    </xsl:template>
    <xsl:template match="s:catalog">
            <xsl:attribute name="catalog"><xsl:value-of select="./@location"/></xsl:attribute>          
    </xsl:template>    
</xsl:stylesheet>
