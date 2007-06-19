<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:s="http://xml.netbeans.org/schema/JAXBWizConfig" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes" xmlns:xalan="http://xml.apache.org/xslt"  xalan:indent-amount="4"/>
    <xsl:template match="/">
        <xsl:element name="project">
            <xsl:attribute name="name"><xsl:value-of select="s:schemas/@projectName"/>_jaxb</xsl:attribute>
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">.</xsl:attribute>            
            <xsl:element name="target">
                <xsl:attribute name="name">xjc-typedef-target</xsl:attribute>
                <xsl:attribute name="depends">-init-project</xsl:attribute>
                <typedef classname="com.sun.tools.xjc.XJC2Task" name="xjc">
                    <classpath path="${{libs.jaxb20.classpath}}"/>
                </typedef>
            </xsl:element> 
            <xsl:element name="target">
                <xsl:attribute name="name">jaxb-code-generation</xsl:attribute>
                <xsl:attribute name="depends">xjc-typedef-target</xsl:attribute>            
                <mkdir dir="build/generated/addons/jaxb"/>
                <mkdir dir="build/classes"/>
                <xsl:apply-templates select="s:schemas/s:schema"/>
                <javac destdir="build/classes" srcdir="build/generated/addons/jaxb" source="${{javac.source}}"  target="${{javac.target}}">
                    <classpath path="${{libs.jaxb20.classpath}}"/>
                </javac>
            </xsl:element> 
        </xsl:element>
    </xsl:template>
    <xsl:template match="s:schema">
        <xsl:element name="xjc">
            <xsl:if test="string-length(@package) > 0">
                <xsl:attribute name="package"><xsl:value-of select="./@package"/></xsl:attribute>
            </xsl:if>
            <xsl:attribute name="destdir"><xsl:value-of select="/s:schemas/@destdir"/></xsl:attribute>
            <xsl:apply-templates select="s:schema-sources/s:schema-source"/>
            <xsl:element name="arg">
                <xsl:attribute name="value"><xsl:value-of select="./@type"/></xsl:attribute>
            </xsl:element>
            <xsl:for-each select="s:xjc-options/s:xjc-option">
                <xsl:if test="./@value='true'">
                    <xsl:apply-templates select="."/>    
                </xsl:if>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>
    <xsl:template match="s:schema-source">
        <xsl:element name="schema">
            <xsl:attribute name="file"><xsl:value-of select="./@location"/></xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="s:xjc-option">
        <xsl:element name="arg">
            <xsl:attribute name="value"><xsl:value-of select="./@name"/></xsl:attribute>
        </xsl:element>                        
    </xsl:template>
</xsl:stylesheet>
