<!--
                Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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

  <!-- Set explicitely background color for Unix/Metal LookAndFell -->
  <xsl:template match="section[@name='Installer']/wizard[@name='Install']/interface[@name='swing']/property[@name='backgroundColor']">
    <property name="backgroundColor">230,230,230</property>
  </xsl:template>
  <xsl:template match="section[@name='Uninstaller']/wizard[@name='Uninstall']/interface[@name='swing']/property[@name='backgroundColor']">
    <property name="backgroundColor">230,230,230</property>
  </xsl:template>
                                                                                                                                                                               
  <!-- turn off generation of Win32 installer -->
  <xsl:template match="section/buildConfiguration/property/arrayItem/property/arrayItem[@type='com.installshield.wizard.platform.win32.Win32LauncherDistribution']/property[@name='enabled']">
    <property name="enabled">False</property>  
  </xsl:template>
  
</xsl:stylesheet>
