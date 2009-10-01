<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" omit-xml-declaration="no"/>

    <xsl:template match="filesystem">
        <xsl:element name="filesystem">
            <xsl:call-template name="with-path">
                <xsl:with-param name="path" select="''"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <xsl:template name="with-path">
        <xsl:param name="path"/>

        <xsl:for-each select="folder|file|attr">
            <xsl:variable name="mypath"><xsl:value-of select="$path"/>/<xsl:value-of select="@name"/></xsl:variable>
            <xsl:element name="{name()}">
                <xsl:attribute name="path"><xsl:value-of select="$mypath"/></xsl:attribute>
                <xsl:apply-templates select="@*|node()"/>
                <xsl:call-template name="with-path">
                    <xsl:with-param name="path" select="$mypath"/>
                </xsl:call-template>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="folder|file|attr"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
