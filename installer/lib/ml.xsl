<!--
                Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
Microsystems, Inc. All Rights Reserved.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>    
  <xsl:template match="*|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>      
  </xsl:template>

  <!-- Enable LanguageSupport for ja,zh. Used to build ML installer. -->
  <xsl:template match="section[@name='Build']/buildConfiguration[@name='Default']/property[@name='support']/arrayItem[@type='com.installshield.isje.build.LanguageSupport']/property[@name='locales']">
            <property array="True" length="3" name="locales" type="string">
                <arrayItem>en</arrayItem>
                <arrayItem>ja</arrayItem>
                <arrayItem>zh</arrayItem>
            </property>
  </xsl:template>

</xsl:stylesheet>
