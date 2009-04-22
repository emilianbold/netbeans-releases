<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:archiveproject="http://www.netbeans.org/ns/archive-project/1"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                xmlns:projdeps2="http://www.netbeans.org/ns/ant-project-references/1"
                exclude-result-prefixes="xalan p projdeps projdeps2">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

        <xsl:comment><![CDATA[
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***

For the purpose of easier reading the script
is divided into following sections:
  - initialization
  - compilation
  - dist
  - execution
  - debugging
  - javadoc
  - junit compilation
  - junit execution
  - junit debugging
  - cleanup

]]></xsl:comment>

        <xsl:variable name="name" select="/p:project/p:configuration/archiveproject:data/archiveproject:name"/>
        <!-- Synch with build-impl.xsl: -->
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-impl">
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>
            <import file="ant-deploy.xml" />

            <fail message="Please build using Ant 1.7.1 or higher.">
                <condition>
                    <not>
                        <antversion atleast="1.7.1"/>
                    </not>
                </condition>
            </fail>

            <target name="default">
                <xsl:attribute name="depends">dist</xsl:attribute>
                <xsl:attribute name="description">Build whole project.</xsl:attribute>
            </target>

            <xsl:comment>
    ======================
    INITIALIZATION SECTION
    ======================
    </xsl:comment>

            <target name="-pre-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-init-private">
                <xsl:attribute name="depends">-pre-init</xsl:attribute>
                <property file="nbproject/private/private.properties"/>
            </target>

            <target name="-init-user">
                <xsl:attribute name="depends">-pre-init,-init-private</xsl:attribute>
                <property file="${{user.properties.file}}"/>
                <xsl:comment> The two properties below are usually overridden </xsl:comment>
                <xsl:comment> by the active platform. Just a fallback. </xsl:comment>
                <property name="default.javac.source" value="1.4"/>
                <property name="default.javac.target" value="1.4"/>
            </target>

            <target name="-init-project">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user</xsl:attribute>
                <property file="nbproject/project.properties"/>
            </target>

            <target name="-do-init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-init-macrodef-property</xsl:attribute>
                <xsl:if test="/p:project/p:configuration/archiveproject:data/archiveproject:explicit-platform">
                    <archiveproject:property name="platform.home" value="platforms.${{platform.active}}.home"/>
                    <archiveproject:property name="platform.bootcp" value="platforms.${{platform.active}}.bootclasspath"/>
                    <archiveproject:property name="platform.compiler" value="platforms.${{platform.active}}.compile"/>
                    <archiveproject:property name="platform.javac.tmp" value="platforms.${{platform.active}}.javac"/>
                    <condition property="platform.javac" value="${{platform.home}}/bin/javac">
                        <equals arg1="${{platform.javac.tmp}}" arg2="$${{platforms.${{platform.active}}.javac}}"/>
                    </condition>
                    <property name="platform.javac" value="${{platform.javac.tmp}}"/>
                    <archiveproject:property name="platform.java.tmp" value="platforms.${{platform.active}}.java"/>
                    <condition property="platform.java" value="${{platform.home}}/bin/java">
                        <equals arg1="${{platform.java.tmp}}" arg2="$${{platforms.${{platform.active}}.java}}"/>
                    </condition>
                    <property name="platform.java" value="${{platform.java.tmp}}"/>
                    <xsl:comment>
                    <fail unless="platform.home">Must set platform.home</fail>
                    <fail unless="platform.bootcp">Must set platform.bootcp</fail>
                    <fail unless="platform.java">Must set platform.java</fail>
                    <fail unless="platform.javac">Must set platform.javac</fail>
                    <fail if="platform.invalid">Platform is not correctly set up</fail>
                    </xsl:comment>
                </xsl:if>
                <xsl:if test="/p:project/p:configuration/archiveproject:data/archiveproject:use-manifest">
                    <xsl:comment>
                    <fail unless="manifest.file">Must set manifest.file</fail>
                    </xsl:comment>
                </xsl:if>
                <available file="${{conf.dir}}/MANIFEST.MF" property="has.custom.manifest"/>
                <available file="${{conf.dir}}/persistence.xml" property="has.persistence.xml"/>
                <available file="subarchives" property="has.subarchives"/>
                
            </target>

            <target name="-post-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-init-check">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-do-init</xsl:attribute>
                <!-- XXX XSLT 2.0 would make it possible to use a for-each here -->
                <!-- Note that if the properties were defined in project.xml that would be easy -->
                <!-- But required props should be defined by the AntBasedProjectType, not stored in each project -->
                <!--
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/archiveproject:data/archiveproject:source-roots"/>
                </xsl:call-template>
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/archiveproject:data/archiveproject:test-roots"/>
                </xsl:call-template>
                -->
                <xsl:comment>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="build.web.dir">Must set build.web.dir</fail>
                <fail unless="build.generated.dir">Must set build.generated.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.dir">Must set build.classes.dir</fail>
                <fail unless="build.test.classes.dir">Must set build.test.classes.dir</fail>
                <fail unless="build.test.results.dir">Must set build.test.results.dir</fail>
                <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>
                <fail unless="dist.war">Must set dist.war</fail>
                </xsl:comment>
            </target>

            <target name="-init-macrodef-property">
                <macrodef>
                    <xsl:attribute name="name">property</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">name</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">value</xsl:attribute>
                    </attribute>
                    <sequential>
                        <property name="@{{name}}" value="${{@{{value}}}}"/>
                    </sequential>
                  </macrodef>
            </target>

            <target name="init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-do-init,-post-init,-init-check</xsl:attribute>
            </target>


            <xsl:comment>
    ======================
    DIST BUILDING SECTION
    ======================
    </xsl:comment>

            <target name="-pre-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>


            <target name="-dist-subarchives">
                <xsl:attribute name="if">has.subarchives</xsl:attribute>                
                <subant target="dist">
                    <fileset dir="subarchives" includes="subtmpproj*/build.xml"/>
                </subant>
            </target>
            
            <target name="-do-dist-with-manifest">
                <xsl:attribute name="if">has.custom.manifest</xsl:attribute>                
                <mkdir dir="${{dist.dir}}"/>
                <mkdir dir="${{content.dir}}/META-INF"/>
                <copy todir="${{content.dir}}/META-INF">
                    <fileset dir="${{conf.dir}}"/>
                </copy>
                <jar jarfile="${{dist.archive}}" manifest="${{conf.dir}}/MANIFEST.MF">
                    <fileset dir="${{content.dir}}"/>
                </jar>
            </target>
            
            <target name="-do-dist-without-manifest">
                <xsl:attribute name="unless">has.custom.manifest</xsl:attribute>                
                <mkdir dir="${{dist.dir}}"/>
                <mkdir dir="${{content.dir}}/META-INF"/>
                <copy todir="${{content.dir}}/META-INF">
                    <fileset dir="${{conf.dir}}"/>
                </copy>
                <jar jarfile="${{dist.archive}}">
                    <fileset dir="${{content.dir}}"/>
                </jar>
            </target>

            <target name="-post-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="dist">
                <xsl:attribute name="depends">init,-pre-dist,-dist-subarchives,-do-dist-with-manifest,-do-dist-without-manifest,-post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution (WAR).</xsl:attribute>
            </target>

            <xsl:comment>
    ======================
    EXECUTION SECTION
    ======================
    </xsl:comment>

            <target name="run-deploy">
                <xsl:attribute name="depends">init,dist,-run-deploy-nb,-init-deploy-ant,-deploy-ant</xsl:attribute>
            </target>
            
            <target name="-run-deploy-nb" if="netbeans.home">
                <delete file="${{dist.archive}}.txt"/>
                <delete file="${{dist.archive}}.xml"/>
                <nbdeploy debugmode="false" clientUrlPart="${{client.urlPart}}" forceRedeploy="${{forceRedeploy}}"/>
            </target>

            <target name="-init-deploy-ant" unless="netbeans.home">
                <property name="deploy.ant.archive" value="${{dist.archive}}"/>
                <property name="deploy.ant.docbase.dir" value="${{web.docbase.dir}}"/>
                <property name="deploy.ant.resource.dir" value="${{resource.dir}}"/>
                <property name="deploy.ant.enabled" value="true"/>
            </target>
            
            <target name="run-undeploy">
                <xsl:attribute name="depends">dist,-run-undeploy-nb,-init-deploy-ant,-undeploy-ant</xsl:attribute>
            </target>
            
            <target name="-run-undeploy-nb" if="netbeans.home">
                <fail message="Undeploy is not supported from within the IDE"/>
            </target>

            <target name="verify">
                <xsl:attribute name="depends">init,dist</xsl:attribute>
                <nbverify file="${{dist.archive}}"/>
            </target>
            
        </project>
    </xsl:template>

</xsl:stylesheet>
