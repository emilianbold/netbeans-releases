<!--
The contents of this file are subject to the terms of the Common Development
and Distribution License (the License). You may not use this file except in
compliance with the License.

You can obtain a copy of the License at http://www.netbeans.org/cddl.html
or http://www.netbeans.org/cddl.txt.

When distributing Covered Code, include this CDDL Header Notice in each file
and include the License file at http://www.netbeans.org/cddl.txt.
If applicable, add the following below the CDDL Header, with the fields
enclosed by brackets [] replaced by your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            <property array="True" length="4" name="locales" type="string">
                <arrayItem>en</arrayItem>
                <arrayItem>ja</arrayItem>
                <arrayItem>pt_BR</arrayItem>
                <arrayItem>zh</arrayItem>
            </property>
  </xsl:template>

</xsl:stylesheet>
