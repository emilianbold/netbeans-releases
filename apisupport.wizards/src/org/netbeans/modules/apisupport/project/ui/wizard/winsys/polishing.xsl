<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:atom="http://www.w3.org/2005/Atom"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
>
    <xsl:output method="xml" indent="yes" xalan:indent-amount="4"/>

    <!-- skip active-tc element -->
    <xsl:template match="active-tc"/>

    <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
