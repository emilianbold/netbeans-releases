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

  <!-- Turn off system (GTK) LookAndFeel on Unix because the wizard looks really bad -->
  <xsl:template match="section[@name='Installer']/wizard[@name='Install']/interface[@name='swing']/property[@name='useSystemLookAndFeel']">
    <property name="useSystemLookAndFeel">False</property>  
  </xsl:template>
  <xsl:template match="section[@name='Uninstaller']/wizard[@name='Uninstall']/interface[@name='swing']/property[@name='useSystemLookAndFeel']">
    <property name="useSystemLookAndFeel">False</property>  
  </xsl:template>
  
  <!-- turn off generation of Win32 installer -->
  <xsl:template match="section/buildConfiguration/property/arrayItem/property/arrayItem[@type='com.installshield.wizard.platform.win32.Win32LauncherDistribution']/property[@name='enabled']">
    <property name="enabled">False</property>  
  </xsl:template>
  
</xsl:stylesheet>
