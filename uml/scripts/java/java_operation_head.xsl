<?xml version='1.0' encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:UML="omg.org/UML/1.4" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:uriHelper="urn:uriHelper" version="1.0">
 <xsl:output method="text"/>

<!-- ========================================================== -->
<!-- Generates code for an operation's parameter list.          -->
<!-- ========================================================== -->

<xsl:template match="UML:Operation" mode="parameter_list">
 <xsl:for-each select="UML:Element.ownedElement/UML:Parameter[not(@direction='result')]">
  <xsl:if test="position() != 1">, </xsl:if>
  <xsl:value-of select="id(uriHelper:RetrieveRawID(string(@type)))/@name"/><xsl:text> </xsl:text>
  <xsl:value-of select="@name"/>
  <xsl:if test="UML:TypedElement.multiplicity/UML:Multiplicity/UML:Multiplicity.range">
   <xsl:for-each select="UML:TypedElement.multiplicity/UML:Multiplicity/UML:Multiplicity.range/UML:MultiplicityRange">
    <xsl:text>[]</xsl:text>
   </xsl:for-each>
  </xsl:if>
 </xsl:for-each>
</xsl:template>


<!-- ========================================================== -->
<!-- Generates code for an operation's head (i.e. return type,  -->
<!-- operation name, parameters, and throws clause.             -->
<!-- ========================================================== -->

<xsl:template name="java_operation_head" match="//UML:Operation">
<xsl:call-template name="operation_comment"/>
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
<xsl:if test="@isAbstract='true'">
 <xsl:text>abstract </xsl:text>
</xsl:if>
<!-- Return Type (optional) -->
<xsl:if test="UML:Element.ownedElement/UML:Parameter[@direction='result']">
 <xsl:value-of select="id(uriHelper:RetrieveRawID(string(UML:Element.ownedElement/UML:Parameter[@direction='result']/@type)))/@name"/>
 <xsl:if test="UML:Element.ownedElement/UML:Parameter[@direction='result']/UML:TypedElement.multiplicity/UML:Multiplicity/UML:Multiplicity.range">
  <xsl:for-each select="UML:Element.ownedElement/UML:Parameter[@direction='result']/UML:TypedElement.multiplicity/UML:Multiplicity/UML:Multiplicity.range/UML:MultiplicityRange">
   <xsl:text>[]</xsl:text>
  </xsl:for-each>
 </xsl:if>
 <xsl:text> </xsl:text>
</xsl:if>
<xsl:value-of select="@name"/>( <xsl:apply-templates select="." mode="parameter_list"/> )</xsl:template>


<!-- ========================================================== -->
<!-- Generates code for an operation's comment using javadoc    -->
<!-- syntax.                                                    -->
<!-- ========================================================== -->

<xsl:template name="operation_comment">

 <!-- if there is a comment on the operation or if the operation has any parameters (other than the return value parameter) -->
 <xsl:if test="string(UML:Element.ownedElement/UML:TaggedValue[@name='documentation']/UML:TaggedValue.dataValue) or UML:Element.ownedElement/UML:Parameter[not(@direction='result')]">

  <!-- beginning of the comment -->
  <xsl:text>/**&#xA;</xsl:text>

  <!-- operation comment -->
  <xsl:if test="string(UML:Element.ownedElement/UML:TaggedValue[@name='documentation']/UML:TaggedValue.dataValue)">
   <xsl:text> * </xsl:text>
   <xsl:value-of select="UML:Element.ownedElement/UML:TaggedValue[@name='documentation']/UML:TaggedValue.dataValue"/>
   <!-- new line after the operation comment -->
   <xsl:text>&#xA; *&#xA;</xsl:text>
  </xsl:if>

  <!-- for each parameter... -->
  <xsl:for-each select="UML:Element.ownedElement/UML:Parameter[not(@direction='result')]">
   <xsl:text> * @param </xsl:text>
   
   <!-- name of the parameter -->
   <xsl:value-of select="@name"/>

   <!-- is the parameter documented -->
   <xsl:if test="UML:Element.ownedElement/UML:TaggedValue[@name='documentation']">
    <xsl:text> </xsl:text>
    <xsl:value-of select="UML:Element.ownedElement/UML:TaggedValue[@name='documentation']"/>
   </xsl:if>

   <xsl:text>&#xA;</xsl:text>
  </xsl:for-each>
  <xsl:text> */&#xA;</xsl:text>
 </xsl:if>
</xsl:template>

</xsl:stylesheet>