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

  <!-- Turn on System LookAndFeel -->
  <xsl:template match="section[@name='Installer']/wizard[@name='Install']/interface[@name='swing']/property[@name='useSystemLookAndFeel']">
    <property name="useSystemLookAndFeel">True</property>
  </xsl:template>
  <xsl:template match="section[@name='Uninstaller']/wizard[@name='Uninstall']/interface[@name='swing']/property[@name='useSystemLookAndFeel']">
    <property name="useSystemLookAndFeel">True</property>
  </xsl:template>

  <!-- Turn off generation of Linux/Solaris, keep Win32 and Mac installers -->
  <xsl:template match="section/buildConfiguration/property/arrayItem/property/arrayItem[@type='com.ibm.wizard.platform.linux.LinuxLauncherDistribution']/property[@name='enabled']">
    <property name="enabled">False</property>  
  </xsl:template>
  <xsl:template match="section/buildConfiguration/property/arrayItem/property/arrayItem[@type='com.installshield.wizard.platform.solaris.distribution.SolarisLauncherDistributionSparc']/property[@name='enabled']">
    <property name="enabled">False</property>
  </xsl:template>
  <xsl:template match="section/buildConfiguration/property/arrayItem/property/arrayItem[@type='com.installshield.wizard.platform.solaris.distribution.SolarisLauncherDistributionX86']/property[@name='enabled']">
    <property name="enabled">False</property>
  </xsl:template>
  
</xsl:stylesheet>
