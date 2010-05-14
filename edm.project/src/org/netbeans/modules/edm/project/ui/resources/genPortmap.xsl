<!--
Translate from 
    <root>
        <dtel partnerLink="pl0" partnerLinkType="plt0" roleName="r0" file="f0"/> 
        ...
        <dtel partnerLink="pl3" partnerLinkType="plt3" roleName="r3" file="f3"/> 
    </root>
to
    <portmaps>
        <portmap partnerLink="pl0" partnerLinkType="plt0" role="myRole" roleName="r0"/>
        ...
        <portmap partnerLink="pl3" partnerLinkType="plt3" role="myRole" roleName="r3"/>
    </portmaps>
-->
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
<xsl:template match="edmmap">
<portmaps>
    <xsl:apply-templates/>
</portmaps>
</xsl:template>

<xsl:template match="edm">
    <portmap>
        <xsl:copy-of select="@partnerLink"/>
        <xsl:copy-of select="@partnerLinkType"/>
        <xsl:copy-of select="@roleName"/>
        <xsl:attribute name="role">myRole</xsl:attribute>
    </portmap>
    <xsl:if test="@type!='requestReplyService'">
    <portmap>
        <xsl:attribute name="partnerLink">
        <xsl:value-of select="@outPartnerLink"/>
        </xsl:attribute>
        <xsl:attribute name="partnerLinkType">
        <xsl:value-of select="@outPartnerLinkType"/>
        </xsl:attribute>
        <xsl:attribute name="roleName">
        <xsl:value-of select="@outRoleName"/>
        </xsl:attribute>
        <xsl:attribute name="role">partnerRole</xsl:attribute>
    </portmap>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>

