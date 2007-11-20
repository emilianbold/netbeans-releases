<?xml version='1.0' encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:UML="omg.org/UML/1.4" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:uriHelper="urn:uriHelper" version="1.0">
 <xsl:output method="text"/>

<!-- ========================================================== -->
<!-- Generates code for an attribute                            -->
<!-- ========================================================== -->

<xsl:template name="java_attribute" match="//UML:Attribute">

 <!-- output the attribute's comment if it has one -->
 <xsl:call-template name="attribute_comment"/>

 <!-- attribute visibility -->
 <xsl:choose>
  <xsl:when test="@visibility='package'"/>
  <xsl:when test="@visibility">
   <xsl:value-of select="@visibility"/>
   <xsl:text> </xsl:text>
  </xsl:when>
  <xsl:otherwise>
   <xsl:text>public </xsl:text>
  </xsl:otherwise>
 </xsl:choose>

 <!-- final specifier (is Const?) -->
 <xsl:call-template name="attribute_final_specifier"/>

 <!-- static specifier -->
 <xsl:call-template name="attribute_static_specifier"/>
 
 <!-- attribute type -->
 <xsl:value-of select="id(uriHelper:RetrieveRawID(string(@type)))/@name"/>
 <xsl:text> </xsl:text>

 <!-- attribute name -->
 <xsl:value-of select="@name"/>

 <!-- array specifier (e.g. [] or [][], etc.) -->
 <xsl:call-template name="array_specifier"/>

 <!-- attribute initializer -->
 <xsl:call-template name="attribute_initializer"/>

 <xsl:text>;</xsl:text>
</xsl:template>

<!-- ========================================================== -->
<!-- Generates code for an association based attribute          -->
<!-- ========================================================== -->

<xsl:template name="java_navigable_end" match="//UML:NavigableEnd">
 <xsl:call-template name="java_attribute"/>
</xsl:template>

<!-- ========================================================== -->
<!-- Generates []s for arrays (multidimensional arrays too)     -->
<!-- ========================================================== -->

<xsl:template name="array_specifier">
 <xsl:if test="UML:TypedElement.multiplicity/UML:Multiplicity/UML:Multiplicity.range/UML:MultiplicityRange">
  <xsl:for-each select="UML:TypedElement.multiplicity/UML:Multiplicity/UML:Multiplicity.range/UML:MultiplicityRange">
   <xsl:text>[]</xsl:text>
  </xsl:for-each>
 </xsl:if>
</xsl:template>

<!-- ========================================================== -->
<!-- Generates an attribute's initializer if it has one         -->
<!-- ========================================================== -->

<xsl:template name="attribute_initializer">
 <xsl:if test="string(UML:Attribute.default/UML:Expression/UML:Expression.body)">
  <xsl:text> = </xsl:text><xsl:value-of select="UML:Attribute.default/UML:Expression/UML:Expression.body"/>
 </xsl:if>
</xsl:template>

<!-- ========================================================== -->
<!-- Generates an attribute's comment if it has one             -->
<!-- ========================================================== -->

<xsl:template name="attribute_comment">
 <xsl:if test="UML:Element.ownedElement/UML:TaggedValue[@name='documentation']/UML:TaggedValue.dataValue">
  <xsl:text>// </xsl:text><xsl:value-of select="UML:Element.ownedElement/UML:TaggedValue[@name='documentation']/UML:TaggedValue.dataValue"/>
  <xsl:text>&#xA;</xsl:text>
 </xsl:if>
</xsl:template>


<!-- ========================================================== -->
<!-- Generates an attribute's "final" specifier                 -->
<!-- ========================================================== -->

<xsl:template name="attribute_final_specifier">
 <xsl:if test="@isFinal='true'">
  <xsl:text>final </xsl:text>
 </xsl:if>
</xsl:template>

<!-- ========================================================== -->
<!-- Generates an attribute's "static" specifier                -->
<!-- ========================================================== -->

<xsl:template name="attribute_static_specifier">
 <xsl:if test="@isStatic='true'">
  <xsl:text>static </xsl:text>
 </xsl:if>
</xsl:template>

</xsl:stylesheet>