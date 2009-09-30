<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="cluster.name"/>
    <xsl:key name="unique" match="folder|file|attr" use="@path"/>

<!-- iterates through hierarchy taking only those with unique path -->

    <xsl:template match="/filesystem">
        <xsl:element name="filesystem">
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="folder|file|attr">
        <xsl:variable name="myid" select="generate-id()"/>
        <xsl:variable name="pathid" select="generate-id(key('unique', @path))"/>

        <xsl:if test="$myid = $pathid">
            <xsl:element name="{name()}">
                <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:variable name="orig" select="."/>
                <xsl:for-each select="/descendant::folder[@path=$orig/@path]">
                    <xsl:apply-templates mode="project-wizard"/>
                    <xsl:apply-templates select="folder"/>
                </xsl:for-each>
            </xsl:element>
        </xsl:if>
    </xsl:template>

<!-- apply the mappings -->

    <!-- ignore is iterated already -->
    <xsl:template match="folder" mode="project-wizard"/>
    <xsl:template match="@path" mode="project-wizard"/>
    <xsl:template match="file" mode="project-wizard">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:if test="@url">
                <xsl:attribute name="url">
                    <xsl:call-template name="filename">
                        <xsl:with-param name="text" select="@url"/>
                    </xsl:call-template>
                </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='SystemFileSystem.localizingBundle']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name">SystemFileSystem.localizingBundle</xsl:attribute>
            <xsl:attribute name="stringvalue">org.netbeans.modules.ide.ergonomics.<xsl:value-of select="$cluster.name"/>.Bundle</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@bundlevalue]" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="bundlevalue">org.netbeans.modules.ide.ergonomics.<xsl:value-of select="$cluster.name"/>.Bundle#<xsl:value-of select="substring-after(@bundlevalue, '#')"/></xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@urlvalue]" mode="project-wizard">
        <xsl:choose>
            <xsl:when test="contains(@urlvalue,'javax/swing/beaninfo/')">
                <xsl:copy-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="attr">
                    <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                    <xsl:attribute name="urlvalue">
                        <xsl:text>nbresloc:/org/netbeans/modules/ide/ergonomics/</xsl:text>
                        <xsl:value-of select="$cluster.name"/>
                        <xsl:text>/</xsl:text>
                        <xsl:call-template name="filename">
                            <xsl:with-param name="text" select="@urlvalue"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="attr[@name = 'iconBase' or @name='iconResource']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="stringvalue">
                <xsl:text>org/netbeans/modules/ide/ergonomics/</xsl:text>
                <xsl:value-of select="$cluster.name"/>
                <xsl:text>/</xsl:text>
                <xsl:call-template name="filename">
                    <xsl:with-param name="text" select="@stringvalue"/>
                </xsl:call-template>
            </xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr" mode="project-wizard">
        <xsl:copy-of select="."/>
    </xsl:template>


    <xsl:template name="filename">
        <xsl:param name="text"/>
        <xsl:variable name="after">
            <xsl:choose>
                <xsl:when test="contains($text,':/')">
                    <xsl:value-of select="substring-after($text,':/')"/>
                </xsl:when>
                <xsl:when test="contains($text,'nbresloc:')">
                    <xsl:value-of select="substring-after($text,'nbresloc:')"/>
                </xsl:when>
                <xsl:when test="contains($text,'/')">
                    <xsl:value-of select="$text"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="''"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$after">
                <xsl:value-of select="translate($after,'/','-')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="translate($text,'/','-')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
