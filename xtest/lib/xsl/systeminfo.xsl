<?xml version="1.0"?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

 The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="library.xsl"/>

<xsl:param name="mappedHostname"/>

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">Summary</xsl:with-param>
	</xsl:call-template>
</xsl:template>


<xsl:template match="/XTestResultsReport/SystemInfo">
	<H2>System Info</H2>
	<P>
	<TABLE>
		<TR><TD>Host</TD><TD>:</TD><TD>
			<xsl:choose>
				<xsl:when test="not($mappedHostname)">
					<xsl:value-of select="@host"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$mappedHostname"/>
				</xsl:otherwise>
			</xsl:choose>
		</TD></TR>
		<TR><TD>Operating System Name</TD><TD>:</TD><TD><xsl:value-of select="@osName"/></TD></TR>
		<TR><TD>Operating System Version</TD><TD>:</TD><TD><xsl:value-of select="@osVersion"/></TD></TR>
		<TR><TD>System Architecture</TD><TD>:</TD><TD><xsl:value-of select="@osArch"/></TD></TR>
		<TR><TD>Java Version</TD><TD>:</TD><TD><xsl:value-of select="@javaVersion"/></TD></TR>
		<TR><TD>Java Vendor</TD><TD>:</TD><TD><xsl:value-of select="@javaVendor"/></TD></TR>
		<TR><TD>User Language</TD><TD>:</TD><TD><xsl:value-of select="@userLanguage"/></TD></TR>
	</TABLE>
	</P>
	<!-- this needs to be redone !!!! -->
	<xsl:if test="false()">
		<P>
		<H3>Additional Information</H3>
		<TABLE>
			<xsl:apply-templates select="SystemInfoExtra"/>
		</TABLE>
		</P>
	</xsl:if>
</xsl:template>


<xsl:template match="SystemInfoExtra">
<TR><TD><xsl:value-of select="@name"/></TD><TD>:</TD><TD><xsl:value-of select="@value"/></TD></TR>		
</xsl:template>

</xsl:stylesheet>