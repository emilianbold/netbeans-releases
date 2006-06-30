<?xml version="1.0"?>
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