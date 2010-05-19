<?xml version="1.0" encoding="UTF-8"?>
<!-- edited with XMLSpy v2006 sp1 U (http://www.altova.com) by Mei Wu (SeeBeyond Technology Corp.) -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xforms="http://www.w3.org/2002/xforms" xmlns:html="http://www.w3.org/1999/xhtml"  xmlns="http://www.w3.org/1999/xhtml">
	<xsl:template match="html:body">
		<inputXForm>
			<xsl:apply-templates select="xforms:group"/>
		</inputXForm>
	</xsl:template>
	<xsl:template match="xforms:group">
		<xsl:copy>
						<xsl:apply-templates select="@*"/>
			<p>
				<b>
					<i><xsl:value-of select="xforms:label"/></i>
				</b>
			</p>
			<p>
	    </p>
			<table border="1">
				<xsl:apply-templates select="xforms:output"/>
				<xsl:apply-templates select="xforms:group"/>
				<xsl:apply-templates select="xforms:repeat"/>
			</table>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="xforms:repeat">
			<p>
		    </p>
			<xsl:copy>
						<xsl:apply-templates select="@*"/>
			<table border="1">
				<xsl:apply-templates select="xforms:output"/>
				<xsl:apply-templates select="xforms:group"/>
				<xsl:apply-templates select="xforms:repeat"/>
			</table>						
		     </xsl:copy>
	</xsl:template>	
	<xsl:template match="xforms:output">
		<tr>
			<td>
				<xsl:apply-templates select="xforms:label"/>
			</td>
			<td>
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
