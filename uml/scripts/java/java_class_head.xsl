<?xml version='1.0' encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:UML="omg.org/UML/1.4" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:uriHelper="urn:uriHelper" version="1.0">
 <xsl:output method="text"/>

<!-- ========================================================== -->
<!-- Generates code for a class head (including extends,        -->
<!-- implements)                                                -->
<!-- ========================================================== -->

<xsl:template name="class_head">
 <xsl:call-template name="class_comment"/>
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
 <xsl:text>class </xsl:text>
 <xsl:value-of select="@name"/>
 <xsl:call-template name="extends_statement"/>
 <xsl:call-template name="implements_statement"/>
</xsl:template>

<!-- ========================================================== -->
<!-- Generates code for a extends statement                     -->
<!-- ========================================================== -->

<xsl:template name="extends_statement">
 <xsl:if test="UML:Classifier.generalization/UML:Generalization and not(UML:Classifier.generalization/UML:Generalization/@isDeleted)">
  <xsl:text> extends </xsl:text>
  <xsl:value-of select="id(uriHelper:RetrieveRawID(string(UML:Classifier.generalization/UML:Generalization/@general)))/@name"/>
 </xsl:if>
</xsl:template>

<!-- ========================================================== -->
<!-- Generates code for an implements statement                 -->
<!-- ========================================================== -->

<xsl:template name="implements_statement">
 <xsl:if test="@clientDependency">
   <xsl:call-template name="process_dependency_list">
    <xsl:with-param name="dependency_list" select="normalize-space(@clientDependency)"/>
   </xsl:call-template>
 </xsl:if>
</xsl:template>

<!-- ========================================================== -->
<!-- Sidebar on XSLT programming.                               -->
<!-- ========================================================== -->
<!-- XSLT programming is closer to functional programming than  -->
<!-- to procedural programming.  In functional programming,     -->
<!-- side effects are frowned upon.                             -->
<!--                                                            -->
<!-- In XSLT, you cannot change a variable's value.  Because of -->
<!-- this, you'll see xsl:variable tags, with xsl:choose tags   -->
<!-- inside of them.  This is a way of conditionally initial-   -->
<!-- izing a variable.                                          -->
<!--                                                            -->
<!-- The input of this function is a list of comma-delimited    -->
<!-- strings.  In the typical style of functional programming,  -->
<!-- the head of the list is processed and then the function is -->
<!-- called recursively passing it the head of the remaining    -->
<!-- list and the tail of the remaining list where the tail of  -->
<!-- the list is all elements of the list (possibly empty)      -->
<!-- except for the head of the list.  See also LISP's car and  -->
<!-- cdr functions.                                             -->
<!--                                                            -->
<!-- Armed with this knowledge, you are prepared to make sense  -->
<!-- of the process_dependency_list function                    -->
<!-- ========================================================== -->


<!-- ========================================================== -->
<!-- Generates code for each interface that is implemented      -->
<!-- ========================================================== -->

<xsl:template name="process_dependency_list">
 <xsl:param name="dependency_list"/>                      <!-- list of all of the class' client dependencies -->
 <xsl:param name="dependency_item"    select="''"/>
 <xsl:param name="written_implements" select="0"/>        <!-- "1" if we've written " implements " already.  "0" otherwise  -->

 <!--trim leading and trailing whitespace off of the implements list -->
 <xsl:variable name="trimmed_list" select="normalize-space($dependency_list)"/>
 <xsl:variable name="trimmed_item" select="normalize-space($dependency_item)"/>

 <!-- 
    determine if we should write the text " implements "                                    
    It should only be written if $trimmed_item is the XMI Id of the first UML:Implementation 
    element that is not deleted.
 -->
 <xsl:variable name="should_write_implements">
  <xsl:choose>
   <xsl:when test="$written_implements='1'">0</xsl:when>
   <xsl:when test="$written_implements='0' and string-length($trimmed_item) and name(id(uriHelper:RetrieveRawID(string($trimmed_item))))='UML:Implementation' and not(id(uriHelper:RetrieveRawID(string($trimmed_item)))/@isDeleted)">1</xsl:when>
   <xsl:otherwise>0</xsl:otherwise>
  </xsl:choose>
 </xsl:variable>
 
 <!-- is $trimmed_item an Implementation XMI ID? -->
 <xsl:if test="string-length($trimmed_item) and name(id(uriHelper:RetrieveRawID(string($trimmed_item))))='UML:Implementation' and not(id(uriHelper:RetrieveRawID(string($trimmed_item)))/@isDeleted)">

  <!-- yes, it is an implementation XMI ID -->

  <!-- either write implements or a comma before the implementation supplier's name -->
  <xsl:choose>
   <xsl:when test="$should_write_implements='1'">
    <xsl:text> implements </xsl:text>
   </xsl:when>
   <xsl:otherwise>
    <xsl:text>, </xsl:text>
   </xsl:otherwise>
  </xsl:choose>

  <!-- write the name of what we're implementing -->
  <xsl:value-of select="id(uriHelper:RetrieveRawID(string(id(uriHelper:RetrieveRawID(string($trimmed_item)))/@supplier)))/@name"/>

 </xsl:if>

 <!-- set $first to either the next item in the list or the only item in the list -->
 <xsl:variable name="first">
  <xsl:choose>
   <xsl:when test="contains($trimmed_list,' ')">
    <xsl:value-of select="substring-before($trimmed_list, ' ')"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="$trimmed_list"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:variable>

 <!-- set $rest to either all items in the list except for the next item or to nothing -->
 <xsl:variable name="rest">
  <xsl:choose>
   <xsl:when test="contains($trimmed_list,' ')">
    <xsl:value-of select="substring-after($trimmed_list, ' ')"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="''"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:variable>
 
 <!-- more elements in the list to process -->
 <xsl:if test="string-length($first)">

  <!-- determine if we have already written " implements " -->
  <xsl:variable name="written">
   <xsl:choose>
    <xsl:when test="$written_implements='1' or $should_write_implements='1'">1</xsl:when>
    <xsl:otherwise>0</xsl:otherwise>
   </xsl:choose>
  </xsl:variable>

  <!-- now call this function again recursively with the new item and the new list -->
  <xsl:call-template name="process_dependency_list">
   <xsl:with-param name="dependency_list"    select="$rest"/>
   <xsl:with-param name="dependency_item"    select="$first"/>
   <xsl:with-param name="written_implements" select="$written"/>
  </xsl:call-template>

 </xsl:if>

</xsl:template>

<!-- ========================================================== -->
<!-- Generates code for a class comment                         -->
<!-- ========================================================== -->

<xsl:template name="class_comment">
 <xsl:if test="string(UML:Element.ownedElement/UML:TaggedValue[@name='documentation']/UML:TaggedValue.dataValue)">
  <xsl:text>/**&#xA; * </xsl:text>
  <xsl:value-of select="UML:Element.ownedElement/UML:TaggedValue[@name='documentation']/UML:TaggedValue.dataValue"/>
  <xsl:text>&#xA; */&#xA;</xsl:text>
 </xsl:if>
</xsl:template>

<!-- ========================================================== -->
<!-- Generates code for a interface head (including extends)    -->
<!-- ========================================================== -->

<xsl:template name="interface_head">
 <xsl:call-template name="class_comment"/>
 <xsl:text>interface </xsl:text>
 <xsl:value-of select="@name"/>
 <xsl:call-template name="extends_statement"/>
</xsl:template>

</xsl:stylesheet>

