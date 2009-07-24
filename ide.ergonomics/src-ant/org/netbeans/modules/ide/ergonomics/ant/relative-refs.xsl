<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:param name="cluster.name"/>

    <xsl:template match="/">
        <xsl:element name="filesystem">
            <xsl:element name="folder">
                <xsl:attribute name="name">Templates</xsl:attribute>
                <xsl:apply-templates
                    select="//*/folder[@name='Templates']/*"
                    mode="project-wizard"
                />
            </xsl:element>
            <xsl:if test="//*/folder[@name='Services']/folder[@name='MIMEResolver']/*">
                <xsl:element name="folder">
                    <xsl:attribute name="name">Services</xsl:attribute>
                    <xsl:element name="folder">
                        <xsl:attribute name="name">MIMEResolver</xsl:attribute>
                        <xsl:apply-templates
                            select="//*/folder[@name='Services']/folder[@name='MIMEResolver']/*"
                            mode="project-wizard"
                        />
                    </xsl:element>
                </xsl:element>
            </xsl:if>
            <xsl:if test="//*/folder[@name='Ergonomics']/*">
                <xsl:element name="folder">
                    <xsl:attribute name="name">Ergonomics</xsl:attribute>
                    <xsl:apply-templates
                        select="//*/folder[@name='Ergonomics']/*"
                        mode="project-wizard"
                    />
                </xsl:element>
            </xsl:if>
            <xsl:if test="//*/folder[@name='Loaders']">
                <xsl:element name="folder">
                    <xsl:attribute name="name">Loaders</xsl:attribute>
                    <xsl:apply-templates
                        select="//*/folder[@name='Loaders']/*"
                        mode="project-wizard"
                    />
                </xsl:element>
            </xsl:if>
            <xsl:if test="//*/folder[@name='Debugger']">
                <xsl:element name="folder">
                    <xsl:attribute name="name">Debugger</xsl:attribute>
                    <xsl:apply-templates
                        select="//*/folder[@name='Debugger']/*"
                        mode="project-wizard"
                    />
                </xsl:element>
            </xsl:if>
            <xsl:if test="//*/folder[@name='Servers']">
                <xsl:element name="folder">
                    <xsl:attribute name="name">Servers</xsl:attribute>
                        <xsl:apply-templates
                            select="//*/folder[@name='Servers']/*"
                            mode="project-wizard"
                        />
                </xsl:element>
            </xsl:if>
            <xsl:if test="//*/folder[@name='Menu']/folder[@name='Profile']">
                <xsl:element name="folder">
                    <xsl:attribute name="name">Menu</xsl:attribute>
                    <xsl:element name="folder">
                        <xsl:attribute name="name">Profile</xsl:attribute>
                        <xsl:apply-templates
                            select="//*/folder[@name='Menu']/folder[@name='Profile']/*"
                            mode="project-wizard"
                        />
                    </xsl:element>
                </xsl:element>
            </xsl:if>
            <xsl:if test="//*/folder[@name='Menu']/folder[@name='File']/folder[@name='Import']">
                <xsl:element name="folder">
                    <xsl:attribute name="name">Menu</xsl:attribute>
                    <xsl:element name="folder">
                        <xsl:attribute name="name">File</xsl:attribute>
                        <xsl:element name="folder">
                            <xsl:attribute name="name">Import</xsl:attribute>
                            <xsl:apply-templates
                                select="//*/folder[@name='Menu']/folder[@name='File']/folder[@name='Import']/*"
                                mode="project-wizard"
                            />
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:if>
            <xsl:if test="//filesystem/folder[@name='Actions']">
                <xsl:element name="folder">
                    <xsl:attribute name="name">Actions</xsl:attribute>
                    <xsl:apply-templates
                        select="//filesystem/folder[@name='Actions']/*"
                        mode="project-wizard"
                    />
                </xsl:element>
            </xsl:if>
        </xsl:element>
    </xsl:template>

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
    <xsl:template match="folder" mode="project-wizard">
        <xsl:element name="folder">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
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
