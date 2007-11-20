<?xml version='1.0' encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:UML="omg.org/UML/1.4" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
 <xsl:import href="java_operation_create.xsl"/>
 <xsl:import href="java_attribute_create.xsl"/>
 <xsl:import href="java_package_statement.xsl"/>
 <xsl:import href="java_class_head.xsl"/>
 <xsl:output method="text"/>

<!-- ========================================================== -->
<!-- Generates code for a class (including package statement    -->
<!-- and operations)                                            -->
<!-- ========================================================== -->

<xsl:template match="//UML:Class">
 
 <!-- package statement -->
 <xsl:call-template name="PackageStatement"/>
 <xsl:text>&#xA;</xsl:text>

 <!-- Class Head including extends/implements -->
 <xsl:call-template name="class_head"/>
 <xsl:text>&#xA;{</xsl:text>

 <!-- Class Attributes -->
 <xsl:apply-templates select="UML:Element.ownedElement/UML:Attribute"/>

 <!-- Class Operations -->
 <xsl:apply-templates select="UML:Element.ownedElement/UML:Operation"/>

 <!-- Nested Classes  -->
 <xsl:apply-templates select="UML:Element.ownedElement/UML:Class"/>

 <!-- Close class body -->
 <xsl:text>&#xA;}&#xA;</xsl:text>

</xsl:template>

<!-- ========================================================== -->
<!-- Generates code for an interface (including package         -->
<!-- statement and operations)                                  -->
<!-- ========================================================== -->

<xsl:template match="//UML:Interface">
 
 <!-- package statement -->
 <xsl:call-template name="PackageStatement"/>
 <xsl:text>&#xA;</xsl:text>

 <!-- Interface Head including extends/implements -->
 <xsl:call-template name="interface_head"/>
 <xsl:text>&#xA;{</xsl:text>

 <!-- Class Attributes -->
 <xsl:apply-templates select="UML:Element.ownedElement/UML:Attribute"/>

 <!-- Interface Operations -->
 <xsl:apply-templates select="UML:Element.ownedElement/UML:Operation"/>

 <!-- Close class body -->
 <xsl:text>&#xA;}&#xA;</xsl:text>

</xsl:template>

</xsl:stylesheet>
