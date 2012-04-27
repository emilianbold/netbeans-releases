<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" >
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
    <xsl:strip-space elements="*"/>
    <xsl:template match="/">
        <xsl:apply-templates select="filesystem/folder[@name='Services']/folder[@name='AntBasedProjectTypes']/file" mode="p"/>
    </xsl:template>
    <xsl:template match="file" mode="p">
        <xsl:text>nbproject.</xsl:text>
        <xsl:value-of select="attr[@name='type']/@stringvalue"/>
        <xsl:text>=org.netbeans.modules.project.ant.AntBasedGenericType
<!-- --></xsl:text>
    </xsl:template>
</xsl:stylesheet>
