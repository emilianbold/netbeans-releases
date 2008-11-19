<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:param name="cluster.name"/>

    <xsl:template match="filesystem/folder[@name='Templates']/folder[@name='Project']">
        <xsl:apply-templates mode="project-wizard"/>
    </xsl:template>

    <xsl:template match="file" mode="project-wizard">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="folder" mode="project-wizard">
        <xsl:element name="folder">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='instantiatingIterator']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name">instantiatingIterator</xsl:attribute>
            <xsl:attribute name="methodvalue">mine</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='SystemFileSystem.localizingBundle']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name">SystemFileSystem.localizingBundle</xsl:attribute>
            <xsl:attribute name="stringvalue">org/netbeans/modules/ide/ergonomics/<xsl:value-of select="$cluster.name"/>.properties</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr" mode="project-wizard">
        <xsl:copy-of select="."/>
    </xsl:template>
</xsl:stylesheet>
