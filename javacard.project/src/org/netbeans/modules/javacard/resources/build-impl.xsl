<?xml version="1.0" encoding="UTF-8"?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.

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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

<xsl:stylesheet version="1.0" exclude-result-prefixes="xalan project jcproj"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:project="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:jcproj="http://www.netbeans.org/ns/javacard-project/3"
>
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
        <xsl:comment>
<![CDATA[*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***
]]>
        </xsl:comment>
        <xsl:variable name="name" select="/project:project/project:configuration/jcproj:data/jcproj:name"/>
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>

        <project name="{$codename}-impl" basedir="..">
            <property file="nbproject/project.properties"/>
            <xsl:comment>nbproject/private.properties contains a pointer to the IDE's global build.properties</xsl:comment>
            <property file="nbproject/private/private.properties"/>
            <xsl:comment>Load that, and it in turn will contain a pointer to the properties file representing the active platform, named jcproject.$PLATFORM_ACTIVE</xsl:comment>
            <property file="${{user.properties.file}}"/>
            <xsl:comment>Load *that* file and we've got keys and values for the bootclasspath and everything else we need to know about the javacard platform</xsl:comment>
            <property name="platform.properties.file.key" value="jcplatform.${{platform.active}}"/>

            <xsl:comment>Resolve a nested property so we can read the card platform definition</xsl:comment>
            <macrodef name="resolveProperty">
                <attribute name="property"/>
                <attribute name="value"/>
                <sequential>
                    <property name="tmp1.@{{property}}" value="@{{value}}"/>
                    <doResolveProperty property="@{{property}}" value="${{tmp1.@{{property}}}}"/>
                </sequential>
            </macrodef>
            <macrodef name="doResolveProperty">
                <attribute name="property"/>
                <attribute name="value"/>
                <sequential>
                    <property name="@{{property}}" value="${{@{{value}}}}"/>
                </sequential>
            </macrodef>
            <macrodef name="resolvePropertyWithoutBrackets">
                <attribute name="property"/>
                <attribute name="value"/>
                <sequential>
                    <property name="tmp1.@{{property}}" value="@{{value}}"/>
                    <doResolvePropertyWithoutBrackets property="@{{property}}" value="tmp1.@{{property}}"/>
                </sequential>
            </macrodef>
            <macrodef name="doResolvePropertyWithoutBrackets">
                <attribute name="property"/>
                <attribute name="value"/>
                <sequential>
                    <property name="@{{property}}" value="${{@{{value}}}}"/>
                </sequential>
            </macrodef>
            <resolveProperty property="computed.file.path" value="${{platform.properties.file.key}}"/>
            <echo>Using JavaCard Platform Definition at ${computed.file.path}</echo>
            <property file="${{computed.file.path}}"/>
            <echo>Java Card Home is ${javacard.home} (${javacard.name})</echo>

            <property name="platform.device.folder.path" value="jcplatform.${{platform.active}}.devicespath"/>
            <echo>Platform device property name is ${platform.device.folder.path}</echo>
            <resolveProperty property="computed.device.folder.path" value="${{platform.device.folder.path}}"/>
            <echo>Computed device folder path is ${computed.device.folder.path}</echo>

            <property name="platform.device.file.path" value="${{computed.device.folder.path}}${{file.separator}}${{active.device}}.${{javacard.device.file.extension}}"/>
            <echo>Platform device file path property name is ${platform.device.file.path}</echo>

            <property file="${{platform.device.file.path}}"/>

            <echo>Deploying to device ${javacard.device.name} http port ${javacard.device.httpPort}</echo>

            <property environment="env"/>
            <available file="${{user.properties.file}}" property="user.properties.file.set"/>
            <available file="${{computed.file.path}}" property="platform.properties.found"/>
            <available file="${{platform.device.file.path}}" property="device.properties.found"/>
            <available file="${{javacard.home}}" property="javacard.home.found"/>
            <xsl:variable name="kind">
                <xsl:value-of select="/project:project/project:configuration/jcproj:data/jcproj:properties/jcproj:property">
                    <xsl:with-param name="name">javacard.project.subtype</xsl:with-param>
                </xsl:value-of>
            </xsl:variable>
            <xsl:variable name="classicappletproject" select="$kind = 'org.netbeans.modules.javacard.capproject'"/>
            <xsl:variable name="webproject" select="$kind = 'org.netbeans.modules.javacard.webproject'"/>
            <xsl:variable name="classiclibraryproject" select="$kind = 'org.netbeans.modules.javacard.clslibproject'"/>
            <xsl:variable name="extendedappletproject" select="$kind = 'org.netbeans.modules.javacard.eapproject'"/>
            <xsl:variable name="extensionlibraryproject" select="$kind = 'org.netbeans.modules.javacard.extlibproject'"/>

            <xsl:choose>
                <xsl:when test="$extendedappletproject">
                    <property name="deployment.type.arg" value="extended-applet"/>
                </xsl:when>
                <xsl:when test="$classicappletproject">
                    <property name="deployment.type.arg" value="classic-applet"/>
                </xsl:when>
                <xsl:when test="$webproject">
                    <property name="deployment.type.arg" value="web"/>
                </xsl:when>
                <xsl:when test="$classiclibraryproject">
                    <property name="deployment.type.arg" value="classic-lib"/>
                </xsl:when>
                <xsl:when test="$extensionlibraryproject">
                    <property name="deployment.type.arg" value="extension-lib"/>
                </xsl:when>
                <xsl:otherwise>
                    <property name="deployment.type.arg" value="unknown"/>
                </xsl:otherwise>
            </xsl:choose>

            <property name="keystore.unresolved" value="${{sign.keystore}}"/>
            <!-- We need to resolve javacard.home, which is referred to from
            project.properties as a path to the default keystore;  however,
            the default properties is loaded first, so javacard.home will
            be unresolved in properties loaded from there.  This way we
            solve the chicken-and-egg problem -->
            <resolvePropertyWithoutBrackets property="keystore.resolved"
                value="${{keystore.unresolved}}"/>
                
            <echo>Keystore is ${keystore.resolved}</echo>

            <target name="__set_for_debug__">
                <property name="_fordebug_" value="true"/>
            </target>

            <property name="emulator.executable" value="${{javacard.emulator}}"/>
            <available file="${{emulator.executable}}" property="emulator.found"/>

            <target name="--init-jcdevkit-home-from-private-properties">
                <property file="nbproject/private/platform-private.properties"/>
                <fail unless="user.properties.file.set">
<![CDATA[
user.properties.file not set in nbproject/private/private.properties.
This should be set to the path on disk to the global NetBeans build.properties
(usually ${user.home}/.netbeans/7.0/build.properties or similar).

This file in turn contains a property jcplatform.${platform.active} which is a 
path to a properties file which contains properties of the Java Card
platform this project will be run on, such as the path to the emulator
and other data.

To fix this problem, open this project in the NetBeans IDE.  In 
Tools | Java Platforms, click Add Platform.  Select Java Card Platform from
the choices of platform kinds.  Locate your copy of a Java Card runtime on
disk.  Then right click this project in the Project tab, and make sure 
the project is set up to use the Java Card Platform you have just defined.

Data about the path to the emulator is not stored in version control by
default, as it will differ between user machines.
]]>
                </fail>
                <fail unless="platform.properties.found">
<![CDATA[
Java Card platform properties file not found.  Was expecting to find an
entry named jcplatform.${platform.active} in ${user.properties.file} which
would point to a properties file containing the path to the Java Card
runtime and other information such as memory settings.

To fix this problem, open this project in the NetBeans IDE.  In
Tools | Java Platforms, click Add Platform.  Select Java Card Platform from
the choices of platform kinds.  Locate your copy of a Java Card runtime on
disk.  Then right click this project in the Project tab, and make sure
the project is set up to use the Java Card Platform you have just defined.

Data about the path to the emulator is not stored in version control by
default, as it will differ between user machines.
]]>
                </fail>

                <fail unless="javacard.home"><![CDATA[
javacard.home not set.  This property should be set to a valid path on disk
to a Java Card Runtime install.
]]>
                </fail>
                <fail unless="javacard.home.found"><![CDATA[
javacard.home set to ${javacard.home} in ${computed.file.path},
but ${javacard.home} does not exist on disk.]]>
                </fail>
                <fail unless="emulator.found"><![CDATA[
No emulator found at ${emulator.executable}]]>
                </fail>
                <fail unless="device.properties.found">
                    No device definition (properties-format) file found at
                    ${platform.device.file.path}
                </fail>
            </target>

            <target name="-init" depends="--init-jcdevkit-home-from-private-properties">
                <property name="jcdk.lib" location="${{javacard.home}}/lib"/>
                <xsl:choose>
                    <xsl:when test="$classicappletproject or $classiclibraryproject">
                        <path id="javacard.classpath">
                            <pathelement location="${{javacard.classic.bootclasspath}}"/>
                        </path>
                    </xsl:when>
                    <xsl:when test="$webproject or $extendedappletproject or $extensionlibraryproject">
                        <path id="javacard.classpath">
                            <pathelement location="${{javacard.bootclasspath}}"/>
                        </path>
                    </xsl:when>
                </xsl:choose>
                <path id="javacard.tasks.path">
                    <pathelement path="${{javacard.nbtasksClassPath}}"/>
                    <pathelement path="${{javacard.toolClasspath}}"/>
                </path>
                <taskdef name="jc-load" classname="${{javacard.tasks.loadTaskClass}}">
                    <classpath>
                        <path refid="javacard.tasks.path"/>
                    </classpath>
                </taskdef>
                <taskdef name="jc-create" classname="${{javacard.tasks.createTaskClass}}">
                    <classpath>
                        <path refid="javacard.tasks.path"/>
                    </classpath>
                </taskdef>
                <taskdef name="jc-delete" classname="${{javacard.tasks.deleteTaskClass}}">
                    <classpath>
                        <path refid="javacard.tasks.path"/>
                    </classpath>
                </taskdef>
                <taskdef name="jc-unload" classname="${{javacard.tasks.unloadTaskClass}}">
                    <classpath>
                        <path refid="javacard.tasks.path"/>
                    </classpath>
                </taskdef>
                <xsl:if test="$webproject">
                <taskdef name="jc-browse" classname="${{javacard.tasks.browseTaskClass}}">
                    <classpath>
                        <path refid="javacard.tasks.path"/>
                    </classpath>
                </taskdef>
                </xsl:if>
                <mkdir dir="${{build.dir}}"/>
                <mkdir dir="${{build.meta.inf.dir}}"/>
                <mkdir dir="${{build.classes.dir}}"/>
                <mkdir dir="${{dist.dir}}"/>
                <xsl:choose>
                    <xsl:when test="$webproject">
                        <mkdir dir="${{build.web.inf.dir}}"/>
                    </xsl:when>
                    <xsl:when test="$classicappletproject or $extendedappletproject">
                        <mkdir dir="${{build.applet.inf.dir}}"/>
                    </xsl:when>
                </xsl:choose>
            </target>

            <target name="__reset_for_debug__">
                <property name="_fordebug_" value="false"/>
            </target>

            <xsl:choose>
                <xsl:when test="$webproject">
                    <target name="create-descriptors">
                        <copy todir="${{build.web.inf.dir}}">
                            <fileset dir="${{web.inf.dir}}"/>
                        </copy>
                        <copy todir="${{build.meta.inf.dir}}">
                            <fileset dir="${{meta.inf.dir}}"/>
                        </copy>
                    </target>

                </xsl:when>
                <xsl:when test="$classiclibraryproject or $extensionlibraryproject">
                    <target name="create-descriptors">
                        <copy todir="${{build.meta.inf.dir}}">
                            <fileset dir="${{meta.inf.dir}}"/>
                        </copy>
                    </target>
                </xsl:when>

                <xsl:otherwise>
                    <target name="create-descriptors">
                        <copy todir="${{build.applet.inf.dir}}">
                            <fileset dir="${{applet.inf.dir}}"/>
                        </copy>
                        <copy todir="${{build.meta.inf.dir}}">
                            <fileset dir="${{meta.inf.dir}}"/>
                        </copy>
                    </target>
                </xsl:otherwise>

            </xsl:choose>

            <target name="pack" depends="pack-unsigned,pack-signed"/>

            <target name="pack-unsigned" unless="sign.bundle">
                <java classname="${{javacard.packagerClass}}" dir="${{javacard.home}}/bin" classpath="${{javacard.toolClassPath}}" fork="true" failonerror="true">
                    <arg value="create"/>
                    <arg value="--type"/>
                    <arg value="${{deployment.type.arg}}"/>
                    <arg value="--out"/>
                    <arg file="${{dist.bundle}}"/>
                    <xsl:choose>
                        <xsl:when test="$classiclibraryproject">
                            <arg value="--packageaid"/>
                            <arg value="${{package.aid}}"/>
                        </xsl:when>
                    </xsl:choose>
                    <arg file="${{build.dir}}"/>
                    <arg value="--force"/>
                    <sysproperty key="jc.home" value="${{javacard.home}}"/>
                </java>
            </target>

            <target name="pack-signed" if="sign.bundle">
                <java classname="${{javacard.packagerClass}}" dir="${{javacard.home}}/bin" classpath="${{javacard.toolClassPath}}" fork="true" failonerror="true">
                    <arg value="create"/>
                    <arg value="--type"/>
                    <arg value="${{deployment.type.arg}}"/>
                    <arg value="--out"/>
                    <arg file="${{dist.bundle}}"/>
                    <xsl:choose>
                        <xsl:when test="$classiclibraryproject">
                            <arg value="--packageaid"/>
                            <arg value="${{package.aid}}"/>
                        </xsl:when>
                    </xsl:choose>
                    <arg value="--sign"/>
                    <arg value="-K"/>
                    <arg file="${{keystore.resolved}}"/>
                    <arg value="-P"/>
                    <arg value="password"/>
                    <arg value="-S"/>
                    <arg value="password"/>
                    <arg value="-A"/>
                    <arg value="ri"/>
                    <arg file="${{build.dir}}"/>
                    <arg value="--force"/>
                    <sysproperty key="jc.home" value="${{javacard.home}}"/>
                </java>
            </target>

            <xsl:choose>
                <xsl:when test="$classiclibraryproject or $extensionlibraryproject">
                    <target name="run" depends="build, load-bundle"/>
                </xsl:when>

                <xsl:otherwise>
                    <target name="run" depends="__reset_for_debug__, build, load-bundle, create-instance, run-client"/>
                    <target name="run-for-debug" depends="__set_for_debug__, build, load-bundle, create-instance, run-client"/>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="$classiclibraryproject or $extensionlibraryproject">
                    <target name="build" depends="compile, create-descriptors, pack"/>
                </xsl:when>

                <xsl:otherwise>
                    <target name="build" depends="compile, create-descriptors, create-static-pages, pack"/>
                </xsl:otherwise>
            </xsl:choose>

            <target name="load-bundle" depends="-init">
                <waitfor>
                    <http url="${{javacard.device.cardmanagerurl}}"/>
                </waitfor>
                <xsl:if test="$classicappletproject or $extendedappletproject or $webproject">
                    <jc-delete failonerror="no"/>
                </xsl:if>
                <jc-unload failonerror="no"/>
                <jc-load failonerror="yes"/>
            </target>

            <xsl:if test="$classicappletproject or $extendedappletproject or $webproject">
                <target name="create-instance" depends="-init">
                    <waitfor>
                        <http url="${{javacard.device.cardmanagerurl}}"/>
                    </waitfor>
                    <jc-create failonerror="yes"/>
                </target>
            </xsl:if>

            <xsl:if test="$classicappletproject or $extendedappletproject or $webproject">
                <target name="delete-instance" depends="-init">
                    <waitfor>
                        <http url="${{javacard.device.cardmanagerurl}}"/>
                    </waitfor>
                    <jc-delete failonerror="yes"/>
                </target>
            </xsl:if>

            <target name="unload-bundle" depends="-init">
                <waitfor>
                    <http url="${{javacard.device.cardmanagerurl}}"/>
                </waitfor>
                <jc-unload  failonerror="yes"/>
            </target>

            <xsl:choose>
                <xsl:when test="$webproject">
                    <target name="run-client" if="run.browser" depends="-browse-servlet, -browse-page, -browse-explicit"/>

                    <target name="-browse-servlet" if="use.servlet">
                        <xsl:comment>
                            <nb-jcServerInfo serverId="${{jcserverid}}" serverUrlProperty="javacard.device.serverurl"/>
                        </xsl:comment>
                        <jc-browse url="${{javacard.device.serverurl}}${{webcontextpath}}${{run.servlet.url}}"/>
                    </target>

                    <target name="-browse-page" if="use.page">
                        <xsl:comment>
                            <nb-jcServerInfo serverId="${{jcserverid}}" serverUrlProperty="javacard.device.serverurl"/>
                        </xsl:comment>
                        <jc-browse url="${{javacard.device.serverurl}}${{webcontextpath}}${{run.page.url}}"/>
                    </target>

                    <target name="-browse-explicit" if="use.url">
                        <xsl:comment>
                            <nb-jcServerInfo serverId="${{jcserverid}}" serverUrlProperty="javacard.device.serverurl"/>
                        </xsl:comment>
                        <jc-browse url="${{run.explicit.url}}"/>
                    </target>
                </xsl:when>

                <xsl:when test="$classicappletproject or $extendedappletproject">
                    <target name="run-client" if="run.apdutool">
                        <xsl:comment>
                            <nb-jcServerInfo serverId="${{jcserverid}}" contactedPortProperty="javacard.device.contactedPort" toolsClassPathProperty="card.tools.jars"/>
                        </xsl:comment>
                        <property name="script.target" value="${{basedir}}/${{run.script}}"/>
                        <available file="${{script.target}}" property="script.target.found"/>
                        <fail unless="script.target.found">No file found at ${script.target}</fail>
                        <echo><![CDATA[Invoking apdutool on ${script.target}]]></echo>
                        
                        <java classname="${{javacard.apdutoolClass}}" dir="${{javacard.home}}/bin" classpath="${{javacard.toolClassPath}}" fork="true" failonerror="${{param_failonerror}}">
                            <arg value="${{javacard.device.apdutool.contactedProtocol}}"/>
                            <arg value="-p"/>
                            <arg value="${{javacard.device.contactedPort}}"/>
                            <arg value="${{script.target}}"/>
                        </java>
                    </target>
                </xsl:when>
            </xsl:choose>

            <target name="help">
                <echo>
<![CDATA[help  - Displays this help message
clean - Cleans the project
build - Creates the deployable bundle (This is default target)
run   - Builds and deploys the application and starts the browser.
]]>
                </echo>
            </target>

            <target name="clean">
                <delete dir="${{build.dir}}"/>
                <delete dir="${{dist.dir}}"/>
            </target>

            <target name="compile" depends="-init">
                <javac destdir="${{build.classes.dir}}" source="${{javac.source}}" target="${{javac.target}}" nowarn="${{javac.deprecation}}" debug="${{javac.debug}}" optimize="no" classpath="${{class.path}}" bootclasspathref="javacard.classpath" includeAntRuntime="no">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/project:project/project:configuration/jcproj:data/jcproj:source-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </javac>

                <copy todir="${{build.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/project:project/project:configuration/jcproj:data/jcproj:source-roots"/>
<!--                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>-->
                    </xsl:call-template>
                </copy>
            </target>

            <target name="all" depends="build"/>

            <!-- this is the popup menu to send any apdu script file -->
            <xsl:if test="$classicappletproject or $extendedappletproject">
                <target name="--run-apdutool--" depends="-init">
                    <xsl:comment>
                        <nb-jcServerInfo serverId="${{jcserverid}}" contactedPortProperty="javacard.device.contactedPort" toolsClassPathProperty="card.tools.jars"/>
                    </xsl:comment>
                    <property name="script.target" value="${{apdu.script.file}}"/>
                    <echo><![CDATA[Sending apdu script file ${script.target}.]]></echo>
                    <java classname="${{javacard.apdutoolClass}}" dir="${{javacard.home}}/bin" classpath="${{javacard.toolClassPath}}" fork="true" failonerror="${{param_failonerror}}">
                        <arg value="${{javacard.device.apdutool.contactedProtocol}}"/>
                        <arg value="-p"/>
                        <arg value="${{javacard.device.contactedPort}}"/>
                        <arg value="${{script.target}}"/>
                    </java>
                </target>
            </xsl:if>

            <target name="create-static-pages" depends="-init">
                <copy todir="${{build.dir}}">
                    <xsl:choose>
                        <xsl:when test="$webproject">
                            <fileset dir="${{staticpages.dir}}"/>
                        </xsl:when>
                        <xsl:when test="$classicappletproject or $extendedappletproject">
                            <fileset dir="${{scripts.dir}}"/>
                        </xsl:when>
                    </xsl:choose>
                </copy>
            </target>
        </project>
    </xsl:template>

    <xsl:template name="createPath">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/jcproj:root">
            <xsl:if test="position() != 1">
                <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:text>${</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>}</xsl:text>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="createFilesets">
        <xsl:param name="roots"/>
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="includes2"/>
        <xsl:param name="excludes"/>
        <xsl:for-each select="$roots/jcproj:root">
            <xsl:element name="fileset">
                <xsl:attribute name="dir">
                    <xsl:text>${</xsl:text>
                    <xsl:value-of select="@id"/>
                    <xsl:text>}</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="includes">
                    <xsl:value-of select="$includes"/>
                </xsl:attribute>
                <xsl:choose>
                    <xsl:when test="$excludes">
                        <xsl:attribute name="excludes">
                            <xsl:value-of select="$excludes"/>,${excludes}
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="excludes">${excludes}</xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="$includes2">
                    <filename name="{$includes2}"/>
                </xsl:if>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
