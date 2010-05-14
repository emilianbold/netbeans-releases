<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:xslt="http://www.netbeans.org/ns/j2ee-xsltpro/1"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                exclude-result-prefixes="xalan p xslt projdeps">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

        <xsl:comment>
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***

For the purpose of easier reading the script
is divided into following sections:

  - initialization
  - dist
  - cleanup
        </xsl:comment>

        <xsl:variable name="name" select="/p:project/p:configuration/xslt:data/xslt:name"/>
        <project name="{$name}-impl">
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>

            <target name="default">
                <xsl:attribute name="depends">dist_se</xsl:attribute>
                <xsl:attribute name="description">Build whole project.</xsl:attribute>
            </target>

            <xsl:comment>
                ======================
                INITIALIZATION SECTION
                ======================
            </xsl:comment>

            <target name="pre-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-init-private">
                <xsl:attribute name="depends">pre-init</xsl:attribute>
                <property file="nbproject/private/private.properties"/>
            </target>

            <target name="-init-userdir">
                <xsl:attribute name="depends">pre-init,-init-private</xsl:attribute>
                <property name="user.properties.file" location="${{netbeans.user}}/build.properties"/>
            </target>

            <target name="-init-user">
                <xsl:attribute name="depends">pre-init,-init-private,-init-userdir</xsl:attribute>
                <property file="${{user.properties.file}}"/>
            </target>

            <target name="-init-project">
                <xsl:attribute name="depends">pre-init,-init-private,-init-userdir,-init-user</xsl:attribute>
                <property file="nbproject/project.properties"/>
            </target>

            <target name="-do-init">
                <xsl:attribute name="depends">pre-init,-init-private,-init-userdir,-init-user,-init-project</xsl:attribute>
                <available file="${{src.dir}}/../retrieved" property="retrieved.exists"/>
            </target>

            <target name="-post-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-init-check">
                <xsl:attribute name="depends">pre-init,-init-private,-init-userdir,-init-user,-init-project,-do-init</xsl:attribute>
                <fail unless="src.dir">Must set src.dir</fail>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="dist.jar">Must set dist.jar</fail>
            </target>

            <target name="-init-taskdefs" if="from.commandline">
        <!-- XXX: we need to seperate xslt model into standalone jars so that
             we can use them in ant tasks. for now we need to add the jars of
             this modules -->
                <path id="ant.task.classpath">
                    <pathelement location="${{esb.netbeans.platform}}/../ide/modules/org-netbeans-modules-xml-xam.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../ide/modules/org-netbeans-modules-xml-retriever.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../ide/modules/org-netbeans-modules-projectapi.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../ide/modules/org-netbeans-modules-xml-schema-model.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../ide/modules/org-netbeans-modules-xml-wsdl-model.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../ide/modules/org-apache-xml-resolver.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../ide/modules/ext/resolver-1_2_nb.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../platform/modules/org-openide-dialogs.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../platform/modules/org-openide-loaders.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../platform/modules/org-openide-nodes.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../platform/modules/org-openide-text.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../platform/core/core.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../platform/core/org-openide-filesystems.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../platform/lib/boot.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../platform/lib/org-openide-modules.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../platform/lib/org-openide-util.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../platform/lib/org-openide-util-lookup.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../soa/modules/org-netbeans-modules-soa-ui.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../soa/modules/org-netbeans-modules-xslt-model.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../soa/modules/org-netbeans-modules-xslt-project.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../soa/ant/nblib/org-netbeans-modules-xslt-project.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../soa/modules/org-netbeans-modules-xslt-core.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../soa/modules/org-netbeans-modules-xslt-tmap.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../soa/modules/org-netbeans-modules-xslt-validation.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../xml/modules/org-netbeans-modules-xml-misc.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../xml/modules/org-netbeans-modules-xml-wsdl-extensions.jar"/>
                    <pathelement location="${{esb.netbeans.platform}}/../xml/modules/org-netbeans-modules-xml-xpath-ext.jar"/>
                </path>

                <taskdef name="validate-xslt-project" classname="org.netbeans.modules.xslt.project.anttasks.CliValidateProjectTask">
                    <classpath refid="ant.task.classpath"/>
                </taskdef>

                <taskdef name="generate-xsltsu-catalog-xml" classname="org.netbeans.modules.xslt.project.anttasks.CliGenerateCatalogTask">
                    <classpath refid="ant.task.classpath"/>
                </taskdef>

                <taskdef name="generate-xsltsu-jbi-xml" classname="org.netbeans.modules.xslt.project.anttasks.GenerateJBIDescriptorTask">
                    <classpath refid="ant.task.classpath"/>
                </taskdef>
            </target>

            <target name="init">
        <xsl:attribute name="depends">pre-init,-init-private,-init-userdir,-init-user,-init-project,-do-init,-post-init,-init-check,-init-taskdefs</xsl:attribute>
            </target>

            <xsl:comment>
        =====================
        DIST BUILDING SECTION
        =====================
            </xsl:comment>

            <target name="-pre-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'-deps-jar-dist'"/>
                <xsl:with-param name="type" select="'jar'"/>
            </xsl:call-template>

            <target name="-do-dist">
                <xsl:attribute name="depends">init,-pre-dist</xsl:attribute>
                <mkdir dir="${{build.dir}}"/>

                <xsl:comment>validation</xsl:comment>
                <validate-xslt-project buildDirectory="${{basedir}}/${{build.dir}}" sourceDirectory="${{basedir}}/${{src.dir}}" projectClassPath="${{javac.classpath}}" buildDependentProjectDir="${{basedir}}/${{build.dir}}/dependentProjectFiles" classpathRef="ant.task.classpath" allowBuildWithError="${{allow.build.with.error}}"/>

                <xsl:comment> copy all files from project source directory to build directory. </xsl:comment>
                <copy todir="${{build.dir}}" preservelastmodified="true" >
                    <fileset includes="**/*" dir="${{src.dir}}"/>
                </copy>

                <generate-xsltsu-catalog-xml buildDirectory="${{basedir}}/${{build.dir}}" sourceDirectory="${{basedir}}/${{src.dir}}" projectClassPath="${{javac.classpath}}" classpathRef="ant.task.classpath"/>
                <generate-xsltsu-jbi-xml buildDirectory="${{basedir}}/${{build.dir}}" sourceDirectory="${{basedir}}/${{src.dir}}" projectClassPath="${{javac.classpath}}" classpathRef="ant.task.classpath"/>

                <jar compress="${{jar.compress}}" jarfile="${{build.dir}}/SEDeployment.jar">
                    <fileset includes="**/*.xml, **/*.wsdl,**/*.xsd, **/*.xsl, **/*.xslt, **/*.jar" excludes="SEDeployment.jar" dir="${{basedir}}/${{build.dir}}"/>
                </jar>
            </target>

            <target name="-post-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="dist_se">
                <xsl:attribute name="depends">init,-pre-dist,-deps-jar-dist,-do-dist,-post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution.</xsl:attribute>
            </target>

            <xsl:comment>
                ===============
                CLEANUP SECTION
                ===============
            </xsl:comment>

    <target name="-pre-clean">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>

            <xsl:call-template name="deps.target">
        <xsl:with-param name="targetname" select="'-deps-clean'"/>
            </xsl:call-template>

    <target name="-do-clean">
        <xsl:attribute name="depends">init,-pre-clean</xsl:attribute>

                <delete dir="${{build.dir}}"/>
                <delete dir="${{dist.dir}}"/>
            </target>

    <target name="-post-clean">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="clean">
        <xsl:attribute name="depends">init,-pre-clean,-deps-clean,-do-clean,-post-clean</xsl:attribute>
                <xsl:attribute name="description">Clean build products.</xsl:attribute>
            </target>
        </project>

    </xsl:template>

    <!---
    Generic template to build subdependencies of a certain type.
    Feel free to copy into other modules.
    @param targetname required name of target to generate
    @param type artifact-type from project.xml to filter on; optional, if not
    specified, uses all references, and looks for clean targets rather than
    build targets
    @return an Ant target which builds (or cleans) all known subprojects
    -->
    <xsl:template name="deps.target">
        <xsl:param name="targetname"/>
        <xsl:param name="type"/>

        <target name="{$targetname}">
            <xsl:attribute name="depends">init</xsl:attribute>
            <xsl:attribute name="unless">${no.dependencies}</xsl:attribute>
            <xsl:variable name="references" select="/p:project/p:configuration/projdeps:references"/>
            <xsl:for-each select="$references/projdeps:reference[not($type) or projdeps:artifact-type = $type]">
                <xsl:variable name="subproj" select="projdeps:foreign-project"/>
                <xsl:variable name="subtarget">
                    <xsl:choose>
                        <xsl:when test="$type">
                            <xsl:value-of select="projdeps:target"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="projdeps:clean-target"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="script" select="projdeps:script"/>
                <xsl:variable name="scriptdir" select="substring-before($script, '/')"/>
                <xsl:variable name="scriptdirslash">
                    <xsl:choose>
                        <xsl:when test="$scriptdir = ''"/>
                        <xsl:otherwise>
                            <xsl:text>/</xsl:text>
                            <xsl:value-of select="$scriptdir"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="scriptfileorblank" select="substring-after($script, '/')"/>
                <xsl:variable name="scriptfile">
                    <xsl:choose>
                        <xsl:when test="$scriptfileorblank != ''">
                            <xsl:value-of select="$scriptfileorblank"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$script"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <ant target="{$subtarget}" inheritall="false">
                    <xsl:attribute name="antfile">${project.<xsl:value-of select="$subproj"/>}<xsl:value-of select="$scriptdirslash"/>/<xsl:value-of select="$scriptfile"/></xsl:attribute>
                </ant>
            </xsl:for-each>
        </target>
    </xsl:template>
</xsl:stylesheet>
