<?xml version="1.0"?>
<!--
                 Sun Public License Notice
 
 The contents of this file are subject to the Sun Public License
 Version 1.0 (the "License"). You may not use this file except in
 compliance with the License. A copy of the License is available at
 http://www.sun.com/
 
 The Original Code is NetBeans. The Initial Developer of the Original
 Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 Microsystems, Inc. All Rights Reserved.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="library.xsl"/>

<xsl:template match="/XTestResultsReport">
	
	<HTML>		
	<xsl:call-template name="copyright"/>
	<HEAD>
	
	<TITLE>XTest Results Report from <xsl:value-of select="SystemInfo/@host"/>, run at <xsl:value-of select="@timeStamp"/></TITLE>
	</HEAD>

	<FRAMESET cols="20%,80%">
		<!--
		<FRAMESET rows="30%,70%">
		-->
			<FRAME src="htmlresults/main-navigator.html" name="mainNavigator"/>
		<!--			
			<FRAME src="" name="contextNavigator"/>
		</FRAMESET>
		-->
		<FRAME src="htmlresults/testreport.html" name="report"/>
	</FRAMESET>
	<NOFRAMES>
	<H2>Frame Alert</H2>
	<P>
		This document is designed to be viewed using the frames feature. If you see this message, you are using a non-frame-capable web client.
	</P>
	</NOFRAMES>
	</HTML>
</xsl:template>

</xsl:stylesheet>