<?xml version="1.0" encoding="UTF-8"?>
<!-- edited with XMLSpy v2006 sp1 U (http://www.altova.com) by Mei Wu (SeeBeyond Technology Corp.) -->
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.

The contents of this file are subject to the terms of either the GNU3
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

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

Contributor(s):

Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xforms="http://www.w3.org/2002/xforms" xmlns:html="http://www.w3.org/1999/xhtml"  xmlns="http://www.w3.org/1999/xhtml">
	<xsl:template match="html:body">
		<inputXForm>
		   <table border="1" bordercolor="#b4b6b8" cellpadding="5" style="border-collapse:separate;border-spacing:0px;">
			<xsl:apply-templates select="xforms:group"/>
		   </table>						
		</inputXForm>
	</xsl:template>
	<xsl:template match="xforms:group">
            	<tr>
			<td  border="1" bordercolor="#b4b6b8" align="left">
			<p>
				<b>
					<i><xsl:value-of select="xforms:label"/></i>
				</b>
			</p>
			</td>
			<td  border="1" bordercolor="#b4b6b8" align="left">
                            <xsl:copy>
                                <xsl:apply-templates select="@*"/>
                                <table border="1" bordercolor="#b4b6b8" cellpadding="5" style="border-collapse:separate;border-spacing:0px;">
                                            <xsl:apply-templates select="xforms:output"/>
                                            <xsl:apply-templates select="xforms:group"/>
                                            <xsl:apply-templates select="xforms:repeat"/>
                                 </table>
                            </xsl:copy>
			</td>
		</tr>
	</xsl:template>        
	<xsl:template match="xforms:repeat">
           	<tr>
			<td  border="1" bordercolor="#b4b6b8" align="left" colspan="2">

				<xsl:copy>
					<xsl:apply-templates select="@*"/>
					<table  border="1" bordercolor="#b4b6b8" cellpadding="5" style="border-collapse:separate;border-spacing:0px;">
						<xsl:apply-templates select="xforms:output"/>
						<xsl:apply-templates select="xforms:group"/>
						<xsl:apply-templates select="xforms:repeat"/>
					</table>						
				 </xsl:copy>
			</td>
		</tr>		     
	</xsl:template>	
	<xsl:template match="xforms:output">
		<tr>
			<td  border="1" bordercolor="#b4b6b8" align="left">
				<xsl:apply-templates select="xforms:label"/>
			</td>
    			<td  border="1" bordercolor="#b4b6b8" align="left">
				  <xsl:copy>
						<xsl:apply-templates select="@*"/>
				  </xsl:copy>
			</td>
		</tr>
	</xsl:template>
	
	<xsl:template match="@*">
		 <xsl:copy>
		 </xsl:copy>
	</xsl:template>	
</xsl:stylesheet>
