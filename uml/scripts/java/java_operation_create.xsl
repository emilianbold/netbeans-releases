<?xml version='1.0' encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:UML="omg.org/UML/1.4" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:uriHelper="urn:uriHelper" version="1.0">
 <xsl:import href="java_operation_head.xsl"/>
 <xsl:output method="text"/>




<!-- ========================================================== -->
<!-- Generates code for an operation including curly braces.    -->
<!-- ========================================================== -->

<xsl:template match="//UML:Operation">
 <xsl:text>&#xA;</xsl:text>
 <xsl:call-template name="java_operation_head"/>
 <xsl:choose>
  <xsl:when test="@isAbstract='true'">
   <xsl:text>;</xsl:text>
  </xsl:when>
  <xsl:otherwise>
   <xsl:text>&#xA;{</xsl:text>
   <xsl:call-template name="operation_body"/>
   <xsl:text>&#xA;}</xsl:text>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<!-- ========================================================== -->
<!-- Generates code for getter and setter methods.  For getter  -->
<!-- methods, the attribute associated with the getter method   -->
<!-- is returned.  For setter methods, the attribute            -->
<!-- associated withe the setter is set.                        -->
<!-- ========================================================== -->
<xsl:template name="operation_body">
 <xsl:choose>

  <!-- Get Method -->
  <xsl:when test="@clientDependency">
   <xsl:variable name="client_dependency_id" select="uriHelper:RetrieveRawID(string(@clientDependency))"/> 
   <xsl:text>&#xA;   return </xsl:text><xsl:value-of select="id(uriHelper:RetrieveRawID(string(id($client_dependency_id)/@supplier)))/@name"/><xsl:text>;</xsl:text>
  </xsl:when>
  
   <!-- Set Method -->
  <xsl:when test="@supplierDependency">
   <xsl:variable name="supplier_dependency_id" select="uriHelper:RetrieveRawID(string(@supplierDependency))"/>
   <xsl:text>&#xA;   </xsl:text><xsl:value-of select="id(uriHelper:RetrieveRawID(string(id($supplier_dependency_id)/@client)))/@name"/> = <xsl:value-of select="UML:Element.ownedElement/UML:Parameter[not(@direction='result')][1]/@name"/><xsl:text>;</xsl:text>
  </xsl:when>

  <!-- This operation is not a getter or a setter.                                 -->
  <!-- If the operation has a return type, return the default value for that type. -->
  <xsl:otherwise>
   <xsl:if test="UML:Element.ownedElement/UML:Parameter[@direction='result']/@type">

    <!-- the name of the operation's return type -->
    <xsl:variable name="type_name" 
                  select="id(uriHelper:RetrieveRawID(string(UML:Element.ownedElement/UML:Parameter[@direction='result']/@type)))/@name"/>

    <xsl:variable name="default_value">
      <xsl:apply-templates select="document('../../Config/Languages.etc')/*">
       <xsl:with-param name="type_name" select="$type_name"/>
      </xsl:apply-templates>
    </xsl:variable>

    <xsl:if test="string-length($default_value) > 0">
     <xsl:text>&#xA;</xsl:text>
     <xsl:value-of select="$type_name"/> retVal = <xsl:value-of select="$default_value"/>
     <xsl:text>;</xsl:text>
     <xsl:text>&#xA;</xsl:text>
     <xsl:text>return retVal;</xsl:text>
    </xsl:if>

   </xsl:if>
  </xsl:otherwise>

 </xsl:choose>

</xsl:template>


<xsl:template match="//LanguageConfig">
 <xsl:param name="type_name" select="'float'"/>
 <xsl:value-of select="Language[@type='java']/DataTypes/DataType[@name=$type_name]/@default_value"/>
</xsl:template>


</xsl:stylesheet>


