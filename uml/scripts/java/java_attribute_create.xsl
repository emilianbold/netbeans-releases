<?xml version='1.0' encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:UML="omg.org/UML/1.4" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
 <xsl:import href="java_attribute.xsl"/>
 <xsl:output method="text"/>

<!-- ========================================================== -->
<!-- Attribute                                              -->
<!-- ========================================================== -->

 <xsl:template match="//UML:Attribute">
  <xsl:text>&#xA;</xsl:text>
  <xsl:call-template name="java_attribute"/>
  <xsl:text>&#xA;</xsl:text>
 </xsl:template>

<!-- ========================================================== -->
<!-- Attribute (represented by a NavigableEnd)                  -->
<!-- ========================================================== -->

 <xsl:template match="//UML:NavigableEnd">
  <xsl:text>&#xA;</xsl:text>
  <xsl:call-template name="java_navigable_end"/>
  <xsl:text>&#xA;</xsl:text>
 </xsl:template>

</xsl:stylesheet>
