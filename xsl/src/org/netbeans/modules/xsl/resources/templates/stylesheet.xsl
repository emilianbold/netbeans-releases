<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : __NAME__
    Created on : __DATE__, __TIME__
    Author     : __USER__
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:strip-space elements="..."/>
              
    <xsl:preserve-space elements="..."/>

    <!-- template rule matching source root element -->
    <xsl:template match="/">
        <xsl:apply-templates select="..."/>
    </xsl:template>

</xsl:stylesheet> 
