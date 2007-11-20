<?xml version='1.0' encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:UML="omg.org/UML/1.4" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
 <xsl:output method="text"/>


<!-- ========================================================== -->
<!-- Outputs a class' package statement                         -->
<!-- ========================================================== -->
<xsl:template match="UML:Class">
 <xsl:call-template name="PackageStatement"/>
</xsl:template>

<!-- ========================================================== -->
<!-- Outputs an Interface's package statement                   -->
<!-- ========================================================== -->
<xsl:template match="UML:Interface">
 <xsl:call-template name="PackageStatement"/>
</xsl:template>

<!-- ========================================================== -->
<!-- Outputs a class' package statement                         -->
<!-- ========================================================== -->

<xsl:template name="PackageStatement">
 <xsl:if test="parent::UML:Element.ownedElement/parent::UML:Package">
  <xsl:text>package </xsl:text>
  <xsl:apply-templates select="parent::UML:Element.ownedElement/parent::UML:Package" mode="NavToRoot"/>
  <xsl:text>;</xsl:text>
 </xsl:if>
</xsl:template>

<!-- ========================================================== -->
<!-- Helper function for navigating/outputting a class'         -->
<!-- package structure.                                         -->
<!-- ========================================================== -->

<xsl:template match="UML:Package" mode="NavToRoot">
 <xsl:if test="parent::UML:Element.ownedElement/parent::UML:Package">
  <xsl:apply-templates select="parent::UML:Element.ownedElement/parent::UML:Package" mode="NavToRoot"/>
  <xsl:text>.</xsl:text>
 </xsl:if>
<xsl:value-of select="@name"/>
</xsl:template>

</xsl:stylesheet>