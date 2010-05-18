<?xml version="1.0" encoding="UTF-8"?>

<!--
The XSLT is intended to delete all BPEL with TypeCast extensions in an old format.
The format was changed between GlassFish ESB v2.0 and v2.1

-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:sxed="http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/Editor"
                >

    <xsl:output method="xml" indent="yes"/>
    
    <!-- Process Editor NS declaration -->

    <!-- Copy everything -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- skip the sxed:editor element everywhere -->
    <xsl:template match="sxed:editor"/>

</xsl:stylesheet>
