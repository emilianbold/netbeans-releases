<?xml version='1.0' encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:UML="omg.org/UML/1.4" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
 <xsl:import href="java_package_statement.xsl"/>
 <xsl:output method="text"/>

<!-- ========================================================== -->
<!-- Outputs a class' package statement                         -->
<!-- ========================================================== -->
<xsl:template match="UML:Class">
 <xsl:call-template name="PackageStatement"/>
 <xsl:text>&#xA;</xsl:text>
</xsl:template>

<!-- ========================================================== -->
<!-- Outputs an interface's package statement                   -->
<!-- ========================================================== -->
<xsl:template match="UML:Interface">
 <xsl:call-template name="PackageStatement"/>
 <xsl:text>&#xA;</xsl:text>
</xsl:template>

</xsl:stylesheet>