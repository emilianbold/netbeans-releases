<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:param name="cluster.name"/>
    <xsl:param name="filename"/>

    <xsl:template match="filesystem/folder[@name='Templates']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Templates</xsl:attribute>
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Services']/folder[@name='MIMEResolver']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Services</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">MIMEResolver</xsl:attribute>
                <xsl:apply-templates mode="mime-resolvers"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Debugger']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Debugger</xsl:attribute>
                <xsl:apply-templates mode="attach-types"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='J2EE']/folder[@name='DeploymentPlugins']
        /folder/file[attr/@stringvalue='org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Servers</xsl:attribute>
                <xsl:apply-templates mode="j2ee-server-types" select="."/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Servers']/folder[@name='Actions']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Servers</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">Actions</xsl:attribute>
                <xsl:apply-templates mode="actions"/>
            </xsl:element>
        </xsl:element>
        <xsl:for-each select="file/attr[@name='originalFile']">
            <xsl:call-template name="actions-definition">
                <xsl:with-param name="originalFile" select="."/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Menu']/folder[@name='Profile']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Menu</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">Profile</xsl:attribute>
                <xsl:apply-templates mode="actions" select="attr"/>
                <xsl:apply-templates mode="actions" select="file[attr[@name='ergonomics' and @boolvalue='true']]"/>
            </xsl:element>
        </xsl:element>
        <xsl:for-each select="file/attr[@name='originalFile' and ../attr[@name='ergonomics' and @boolvalue='true']]">
            <xsl:call-template name="actions-definition">
                <xsl:with-param name="originalFile" select="."/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Menu']/folder[@name='File']/folder[@name='Import']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Menu</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">File</xsl:attribute>
                <xsl:element name="folder">
                    <xsl:attribute name="name">Import</xsl:attribute>
                    <xsl:apply-templates mode="actions" select="attr"/>
                    <xsl:apply-templates mode="actions" select="file[attr[@name='ergonomics' and @boolvalue='true']]"/>
                </xsl:element>
            </xsl:element>
        </xsl:element>
        <xsl:for-each select="file/attr[@name='originalFile' and ../attr[@name='ergonomics' and @boolvalue='true']]">
            <xsl:call-template name="actions-definition">
                <xsl:with-param name="originalFile" select="."/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Services']/folder[@name='AntBasedProjectTypes']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Ergonomics</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">AntBasedProjectTypes</xsl:attribute>
                <xsl:apply-templates mode="project-types"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Loaders']/folder/folder/folder[@name='Factories']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Loaders</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name"><xsl:value-of select="../../@name"/></xsl:attribute>
                <xsl:element name="folder">
                    <xsl:attribute name="name"><xsl:value-of select="../@name"/></xsl:attribute>
                    <xsl:element name="folder">
                        <xsl:attribute name="name">Factories</xsl:attribute>
                        <xsl:element name="file">
                            <xsl:attribute name="name">Ergonomics.instance</xsl:attribute>
                            <xsl:element name="attr">
                                <xsl:attribute name="name">instanceCreate</xsl:attribute>
                                <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.fod.FodDataObjectFactory.create</xsl:attribute>
                            </xsl:element>
                            <xsl:element name="attr">
                                <xsl:attribute name="name">position</xsl:attribute>
                                <xsl:attribute name="intvalue">999999</xsl:attribute>
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <!-- project wizard -->
    <xsl:template match="file" mode="project-wizard">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="folder" mode="project-wizard">
        <xsl:element name="folder">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='instantiatingIterator']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name">instantiatingIterator</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.api.Factory.newProject</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='templateWizardIterator']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name">instantiatingIterator</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.api.Factory.newProject</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='urlvalue']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:call-template name="urlvalue">
                <xsl:with-param name="url" select="@urlvalue"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr" mode="project-wizard">
        <xsl:copy-of select="."/>
    </xsl:template>

    <!-- mime-resolvers -->
    <xsl:template match="file" mode="mime-resolvers">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:call-template name="url">
                <xsl:with-param name="url" select="@url"/>
            </xsl:call-template>
            <xsl:apply-templates mode="mime-resolvers"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr" mode="mime-resolvers">
        <xsl:copy-of select="."/>
    </xsl:template>

    <!-- attach type -->
    <xsl:template match="file" mode="attach-types">
        <xsl:if test="attr[@stringvalue='org.netbeans.spi.debugger.ui.AttachType']">
            <xsl:element name="file">
                <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:call-template name="url">
                    <xsl:with-param name="url" select="@url"/>
                </xsl:call-template>
                <xsl:apply-templates mode="attach-types"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="attr[@name='instanceCreate']" mode="attach-types">
        <xsl:element name="attr">
            <xsl:attribute name="name">instanceCreate</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.debugger.AttachTypeProxy.create</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr" mode="attach-types">
        <xsl:copy-of select="."/>
    </xsl:template>

    <!-- j2ee server type -->
    <xsl:template match="file" mode="j2ee-server-types">
        <xsl:if test="attr[@name='displayName']">
            <xsl:element name="file">
                <xsl:attribute name="name">WizardProvider-<xsl:value-of select="../@name"/>.instance</xsl:attribute>
                <attr name="instanceCreate" methodvalue="org.netbeans.modules.ide.ergonomics.ServerWizardProviderProxy.create"/>
                <attr name="instanceClass" stringvalue="org.netbeans.modules.ide.ergonomics.ServerWizardProviderProxy"/>
                <attr name="instanceOf" stringvalue="org.netbeans.spi.server.ServerWizardProvider"/>
                <attr name="originalDefinition">
                    <xsl:attribute name="stringvalue">
                        <xsl:call-template name="fullpath">
                            <xsl:with-param name="file" select="."/>
                        </xsl:call-template>
                    </xsl:attribute>
                </attr>
                <xsl:apply-templates select="attr[@name='displayName']" mode="j2ee-server-types"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="attr" mode="j2ee-server-types">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template name="fullpath" mode="j2ee-server-types">
        <xsl:param name="file"/>
        <xsl:if test="$file/../@name">
            <xsl:call-template name="fullpath">
                <xsl:with-param name="file" select="$file/.."/>
            </xsl:call-template>
            <xsl:text>/</xsl:text>
        </xsl:if>
        <xsl:value-of select="$file/@name"/>
    </xsl:template>

    <!-- actions -->
    <xsl:template match="file" mode="actions">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:call-template name="url">
                <xsl:with-param name="url" select="@url"/>
            </xsl:call-template>
            <xsl:apply-templates mode="actions"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr[@name='delegate']" mode="actions">
        <xsl:element name="attr">
            <xsl:attribute name="name">delegate</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.fod.FeatureAction.create</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr" mode="actions">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template name="actions-definition">
        <xsl:param name="originalFile"/>
        <xsl:for-each select=".">
            <xsl:call-template name="actions-definition-impl">
                <xsl:with-param name="path" select="$originalFile/@stringvalue"/>
                <xsl:with-param name="query" select="'filesystem'"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="actions-definition-impl">
        <xsl:param name="path"/>
        <xsl:param name="query"/>
        <xsl:variable name="category" select="substring-before($path,'/')"/>
        <xsl:choose>
            <xsl:when test="$category">
                <xsl:element name="folder">
                    <xsl:attribute name="name">
                        <xsl:value-of select="$category"/>
                    </xsl:attribute>
                    <xsl:call-template name="actions-definition-impl">
                        <xsl:with-param name="path" select="substring-after($path,'/')"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates
                    select="//filesystem/folder[@name='Actions']/descendant::file[@name=$path]"
                    mode="actions"
                />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- project type -->
    <xsl:template match="file" mode="project-types">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:call-template name="url">
                <xsl:with-param name="url" select="@url"/>
            </xsl:call-template>
            <xsl:apply-templates mode="project-types"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr[@name='instanceCreate']" mode="project-types">
        <xsl:element name="attr">
            <xsl:attribute name="name">instanceCreate</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.fod.FeatureProjectFactory.create</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr[@name='instanceClass']" mode="project-types">
        <xsl:element name="attr">
            <xsl:attribute name="name">instanceClass</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.spi.project.ProjectFactory</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr" mode="project-types">
        <xsl:copy-of select="."/>
    </xsl:template>

    <!-- convert relative URLs to absolute -->
    <xsl:template name="url">
        <xsl:param name="url"/>

        <xsl:choose>
            <xsl:when test="not($url)"/>
            <xsl:when test="contains($url,':')">
                <xsl:attribute name="url">
                    <xsl:value-of select="$url"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="starts-with($url,'/')">
                <xsl:attribute name="url">
                    <xsl:value-of select="$url"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="prefix" select="substring-before($filename, '.xml')"/>
                <xsl:attribute name="url">
                    <xsl:text>nbresloc:/</xsl:text>
                    <xsl:value-of select="translate($prefix, '.', '/')"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="$url"/>
                </xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="urlvalue">
        <xsl:param name="url"/>

        <xsl:choose>
            <xsl:when test="not($url)"/>
            <xsl:when test="contains($url,'/')">
                <xsl:attribute name="urlvalue">
                    <xsl:value-of select="$url"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="prefix" select="substring-before($filename, '.xml')"/>
                <xsl:attribute name="urlvalue">
                    <xsl:text>nbresloc:/</xsl:text>
                    <xsl:value-of select="translate($prefix, '.', '/')"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="$url"/>
                </xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
