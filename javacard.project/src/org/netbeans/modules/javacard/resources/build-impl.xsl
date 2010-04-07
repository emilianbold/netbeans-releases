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

            <property environment="env"/>
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


            <target name="-init-environment" depends="init-platform-properties,init-ri-properties,init-device-properties,init-keystore">
                <condition property="device.properties.found">
                    <or>
                        <available file="${{platform.device.file.path}}"/>
                        <istrue value="${{javacard.build.no.device.file}}"/>
                    </or>
                </condition>
                <available file="${{user.properties.file}}" property="user.properties.file.set"/>
            </target>

            <target name="init-platform-properties">
                <property name="platform.properties.file.key" value="jcplatform.${{platform.active}}"/>
                <resolveProperty property="computed.file.path" value="${{platform.properties.file.key}}"/>
                <echo>Using JavaCard Platform Definition at ${computed.file.path}</echo>
                <available file="${{computed.file.path}}" property="platform.properties.found"/>
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
                <property file="${{computed.file.path}}"/>
                <fail unless="javacard.home"><![CDATA[
javacard.home not set.  This property should be set to a valid path on disk
to a Java Card Runtime install.
]]>
                </fail>
                <available file="${{javacard.home}}" property="javacard.home.found"/>
                <fail unless="javacard.home.found"><![CDATA[
javacard.home set to ${javacard.home} in ${computed.file.path},
but ${javacard.home} does not exist on disk.]]>
                </fail>

                <echo>Java Card Home is ${javacard.home} (${javacard.name})</echo>
            </target>

            <target name="init-keystore" if="sign.bundle">
                <property name="keystore.unresolved" value="${{sign.keystore}}"/>
                <!-- We need to resolve javacard.home, which is referred to from
                project.properties as a path to the default keystore;  however,
                the default properties is loaded first, so javacard.home will
                be unresolved in properties loaded from there.  This way we
                solve the chicken-and-egg problem -->
                <resolvePropertyWithoutBrackets property="keystore.resolved"
                    value="${{keystore.unresolved}}"/>
                <echo>Keystore is ${keystore.resolved}</echo>
            </target>

            <target name="init-ri-properties" if="javacard.wrap.ri">
                <echo>Loading RI Properties from ${javacard.ri.properties.path}</echo>
                <property file="${{javacard.ri.properties.path}}"/>
                <available property="rifound" file="${{javacard.ri.home}}"/>
                <!--
                XXX should not, but does, fail - need to diagnose
                <fail unless="${{rifound}}">
The Java Card SDK this project is using requires the Java Card Reference
implementation along with the vendor SDK.  The javacard.ri.home property
is not set, or is set to a non-existent directory.  Currently it is set to
${javacard.ri.home}, most likely in definition file ${javacard.ri.properties.path}
                </fail>
                -->
            </target>

            <target name="init-device-properties" unless="javacard.build.no.device.file">
                <property name="platform.device.folder.path" value="jcplatform.${{platform.active}}.devicespath"/>
                <echo>Platform device property name is ${platform.device.folder.path}</echo>
                <resolveProperty property="computed.device.folder.path" value="${{platform.device.folder.path}}"/>
                <echo>Computed device folder path is ${computed.device.folder.path}</echo>

                <property name="platform.device.file.path" value="${{computed.device.folder.path}}${{file.separator}}${{active.device}}.${{javacard.device.file.extension}}"/>
                <echo>Platform device file path property name is ${platform.device.file.path}</echo>
                <property file="${{platform.device.file.path}}"/>
                <echo>Deploying to device ${javacard.device.name} http port ${javacard.device.httpPort}</echo>
            </target>

            <target name="__set_for_debug__">
                <property name="_fordebug_" value="true"/>
            </target>

            <property name="emulator.executable" value="${{javacard.emulator}}"/>
            <available file="${{emulator.executable}}" property="emulator.found"/>

            <target name="--init-jcdevkit-home-from-private-properties" depends="-init-environment">
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

<!--                <condition property="emulator.ok">
                    <or>
                       <not>
                           <xsl:comment>platform may not have an emulator</xsl:comment>
                           <isset property="javacard.emulator"/>
                       </not>
                       <available file="${{emulator.executable}}"/>
                    </or>
                </condition>

                <fail unless="emulator.ok"><![CDATA[
No emulator found at ${emulator.executable}]]>
                </fail>
                <fail unless="device.properties.found">
                    No device definition (properties-format) file found at
                    ${platform.device.file.path}
                </fail>
                -->
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
                    <pathelement path="${{javacard.toolClassPath}}"/>
                    <xsl:comment>Incorrect but appears to have been used at some point:</xsl:comment>
                    <pathelement path="${{javacard.toolClasspath}}"/>
                </path>
                <taskdef name="jc-pack" classname="${{javacard.tasks.packTaskClass}}">
                    <classpath>
                        <path refid="javacard.tasks.path"/>
                    </classpath>
                </taskdef>
                <taskdef name="jc-sign" classname="${{javacard.tasks.signTaskClass}}">
                    <classpath>
                        <path refid="javacard.tasks.path"/>
                    </classpath>
                </taskdef>
                <taskdef name="jc-proxy" classname="${{javacard.tasks.proxyTaskClass}}">
                    <classpath>
                        <path refid="javacard.tasks.path"/>
                    </classpath>
                </taskdef>
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

            <xsl:choose>
                <xsl:when test="$classiclibraryproject">
                    <target name="pack" depends="unpack-dependencies,compile,compile-proxies,create-descriptors,do-pack"/>
                </xsl:when>

                <xsl:when test="$classicappletproject">
                    <target name="pack" depends="unpack-dependencies,compile,compile-proxies,create-descriptors,create-static-pages,do-pack"/>
                </xsl:when>

                <xsl:when test="$extensionlibraryproject">
                    <target name="pack" depends="unpack-dependencies,compile,create-descriptors,do-pack"/>
                </xsl:when>

                <xsl:otherwise>
                    <target name="pack" depends="unpack-dependencies,compile,create-descriptors,create-static-pages,do-pack"/>
                </xsl:otherwise>
            </xsl:choose>

            <target name="do-pack">
                <jc-pack failonerror="true"/>
            </target>

            <target name="sign" depends="pack,do-sign"/>
            
            <target name="do-sign" if="sign.bundle" depends="pack">
                <jc-sign failonerror="true"/>
            </target>

            <xsl:if test="$classicappletproject or $classiclibraryproject">
                <target name="-print-message-for-use-my-proxies">
                    <echo>
    ${proxies.count} proxy source(s) were generated to './${proxy.generation.dir}'
    ${new.count} new sources were copied to './${src.proxies.dir}'
    
    Note: existing proxy sources at './${src.proxies.dir}' weren't replaced or removed.
    Only new ones were added. To replace any of existing proxy sources with generated ones
    or remove unnecessary sources you need to delete corresponding source files from 
    './${src.proxies.dir}'. You can see *all* generated sources at './${proxy.generation.dir}'.
                    </echo>
                    <echo></echo>
                </target>

                <target name="generate-sio-proxies" depends="-init,compile,create-descriptors">
                    <delete dir="${{proxy.generation.dir}}"/>
                    <mkdir dir="${{proxy.generation.dir}}"/>
                    <jc-proxy failonerror="true"/>
                    <fileset dir="${{proxy.generation.dir}}" includes="**/proxy/*.java" id="proxies.new">
                        <present present="srconly" targetdir="${{src.proxies.dir}}"/>
                    </fileset>
                    <resourcecount property="new.count" refid="proxies.new"/>
                    <copy todir="${{src.proxies.dir}}" overwrite="false">
                        <fileset refid="proxies.new"/>
                    </copy>
                    <resourcecount property="proxies.count">
                        <fileset dir="${{proxy.generation.dir}}" includes="**/proxy/*.java" />
                    </resourcecount>
                    <antcall target="-print-message-for-use-my-proxies"/>
                </target>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="$classiclibraryproject or $extensionlibraryproject">
                    <target name="run" depends="build, load-bundle"/>
                </xsl:when>

                <xsl:otherwise>
                    <target name="run" depends="__reset_for_debug__, build, load-bundle, create-instance, run-client"/>
                    <target name="run-for-debug" depends="__set_for_debug__, build, load-bundle, create-instance, run-client"/>
                </xsl:otherwise>
            </xsl:choose>

            <target name="build" depends="pack,sign"/>

            <target name="load-bundle">
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
                <antcall target="unload-dependencies"/>
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
                        <antcall target="run-script" inheritall="true" inheritrefs="true"/>
                    </target>

                    <target name="run-script" if="run.script">
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
                <javac destdir="${{build.classes.dir}}" source="${{javac.source}}" target="${{javac.target}}" nowarn="${{javac.deprecation}}" debug="${{javac.debug}}" optimize="no" bootclasspathref="javacard.classpath" includeAntRuntime="no">
                    <xsl:for-each select="/project:project/project:configuration/jcproj:data/jcproj:source-roots/jcproj:root">
                        <xsl:if test="@id != 'src.proxies.dir'">
                        <xsl:element name="src">
                            <xsl:attribute name="path">
                                <xsl:text>${</xsl:text>
                                <xsl:value-of select="@id"/>
                                <xsl:text>}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        </xsl:if>
                    </xsl:for-each>
                    <classpath id="compile.path">
                    <xsl:for-each select="/project:project/project:configuration/jcproj:data/jcproj:dependencies/jcproj:dependency">
                        <xsl:element name="pathelement">
                            <xsl:attribute name="path">
                                <xsl:choose>
                                    <xsl:when test="@kind = 'CLASSIC_LIB' or @kind = 'EXTENSION_LIB' or @kind = 'JAVA_PROJECT'">
                                        <xsl:text>${dependency.</xsl:text>
                                        <xsl:value-of select="@id"/>
                                        <xsl:text>.origin}/dist/</xsl:text>
                                        <xsl:value-of select="@id"/>
                                        <xsl:choose>
                                            <xsl:when test="@kind = 'CLASSIC_LIB'">
                                                <xsl:text>.cap</xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:text>.jar</xsl:text>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:text>${dependency.</xsl:text>
                                        <xsl:value-of select="@id"/>
                                        <xsl:text>.origin}</xsl:text>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:for-each>
                    </classpath>
                </javac>
            <xsl:if test="$classicappletproject or $classiclibraryproject">
                <condition property="compile.proxies">
                    <and>
                        <isset property="use.my.proxies"/>
                        <equals arg1="${{use.my.proxies}}" arg2="true"/>
                        <available file="${{src.proxies.dir}}" type="dir"/>
                    </and>
                </condition>
            </xsl:if>
                <copy todir="${{build.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/project:project/project:configuration/jcproj:data/jcproj:source-roots"/>
<!--                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>-->
                    </xsl:call-template>
                </copy>
            </target>

        <xsl:if test="$classicappletproject or $classiclibraryproject">
            <target name="compile-proxies" if="compile.proxies">
                <javac destdir="${{build.classes.dir}}" source="${{javac.source}}"
                        target="${{javac.target}}" nowarn="${{javac.deprecation}}"
                        debug="${{javac.debug}}" optimize="no" includeAntRuntime="no"
                        includes="**/proxy/*.java">
                    <bootclasspath>
                        <pathelement location="${{javacard.bootclasspath}}"/>
                    </bootclasspath>
                    <classpath refid="compile.path"/>
                    <src path="${{src.proxies.dir}}"/>
                </javac>
            </target>
        </xsl:if>

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
                <copy todir="${{build.dir}}" failonerror="false">
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

            <target name="load-dependencies" depends="build-dependencies,pack">
                <xsl:call-template name="load-dependencies"/>
            </target>

            <target name="unload-dependencies">
                <xsl:call-template name="unload-dependencies"/>
            </target>

            <target name="unpack-dependencies" depends="-init">
                <mkdir dir="${{build.classes.dir}}"/>
                <xsl:call-template name="unpack-dependencies"/>
            </target>

            <target name="build-dependencies" depends="-init" unless="dont.build.dependencies">
                <xsl:call-template name="build-dependencies"/>
            </target>

            <target name="clean-dependencies" depends="-init" unless="dont.build.dependencies">
                <xsl:call-template name="clean-dependencies"/>
            </target>

            <target name="clean-with-dependencies" description="Cleans this project and any projects it depends on" depends="clean,clean-dependencies"/>
            <target name="build-with-dependencies" description="Builds any projects this project depends on, then builds this project" depends="build-dependencies,pack"/>
        </project>
    </xsl:template>

    <xsl:template name="load-dependencies">
        <xsl:for-each select="/project:project/project:configuration/jcproj:data/jcproj:dependencies/jcproj:dependency">
            <xsl:if test="@deployment = 'DEPLOY_TO_CARD'">
                <xsl:element name="echo">
                    <xsl:attribute name="message">
                        <xsl:text>Loading dependency</xsl:text>
                        <xsl:value-of select="@id"/>
                        <xsl:text> (type:</xsl:text>
                        <xsl:value-of select="@kind"/>
                        <xsl:text>, deployment strategy </xsl:text>
                        <xsl:value-of select="@deployment"/>
                        <xsl:text>)</xsl:text>
                    </xsl:attribute>
                </xsl:element>
                <!-- Project root dependencies -->
                <xsl:if test="@kind = 'CLASSIC_LIB' or @kind = 'EXTENSION_LIB'">
                    <xsl:element name="ant">
                        <xsl:attribute name="target">
                            <xsl:text>load-bundle</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="antfile">
                            <xsl:text>${dependency.</xsl:text>
                            <xsl:value-of select="@id"/>
                            <xsl:text>.origin}/build.xml</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="dir">
                            <xsl:text>${dependency.</xsl:text>
                            <xsl:value-of select="@id"/>
                            <xsl:text>.origin}</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="inheritAll">
                            <xsl:text>false</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="inheritRefs">
                            <xsl:text>false</xsl:text>
                        </xsl:attribute>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>user.properties.file</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${user.properties.file}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>active.device</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${active.device}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>platform.active</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${platform.active}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>platform.properties.file.key</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${platform.properties.file.key}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>platform.device.folder.path</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${platform.device.folder.path}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:element>
                </xsl:if>
                <!-- deploying JAR files dependencies -->
                <xsl:if test="@kind = 'EXTENSION_LIB_JAR' or @kind = 'CLASSIC_LIB_JAR'">
                    <fail>Classic and Extension Lib JAR deployement w/o project not implemented yet</fail>
                </xsl:if>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="build-dependencies">
        <xsl:for-each select="/project:project/project:configuration/jcproj:data/jcproj:dependencies/jcproj:dependency">
                <!-- Project root dependencies -->
                <xsl:if test="@kind = 'CLASSIC_LIB' or @kind = 'EXTENSION_LIB' or @kind = 'JAVA_PROJECT'">
                    <xsl:element name="echo">
                        <xsl:attribute name="message">
                            <xsl:text>Building dependency</xsl:text>
                            <xsl:value-of select="@id"/>
                            <xsl:text> (type:</xsl:text>
                            <xsl:value-of select="@kind"/>
                            <xsl:text>, deployment strategy </xsl:text>
                            <xsl:value-of select="@deployment"/>
                            <xsl:text>)</xsl:text>
                        </xsl:attribute>
                    </xsl:element>
                    <xsl:element name="ant">
                        <xsl:attribute name="target">
                            <xsl:choose>
                                <xsl:when test="@kind = 'CLASSIC_LIB' or @kind = 'EXTENSION_LIB' or @kind = 'JAVA_PROJECT'">
                                    <xsl:text>pack</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>jar</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:attribute name="antfile">
                            <xsl:text>${dependency.</xsl:text>
                            <xsl:value-of select="@id"/>
                            <xsl:text>.origin}/build.xml</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="dir">
                            <xsl:text>${dependency.</xsl:text>
                            <xsl:value-of select="@id"/>
                            <xsl:text>.origin}</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="inheritAll">
                            <xsl:text>false</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="inheritRefs">
                            <xsl:text>false</xsl:text>
                        </xsl:attribute>
                        <xsl:if test="@kind = 'CLASSIC_LIB' or @kind = 'EXTENSION_LIB'">
                            <xsl:element name="property">
                                <xsl:attribute name="name">
                                    <xsl:text>user.properties.file</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="value">
                                    <xsl:text>${user.properties.file}</xsl:text>
                                </xsl:attribute>
                            </xsl:element>
                            <xsl:element name="property">
                                <xsl:attribute name="name">
                                    <xsl:text>active.device</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="value">
                                    <xsl:text>${active.device}</xsl:text>
                                </xsl:attribute>
                            </xsl:element>
                            <xsl:element name="property">
                                <xsl:attribute name="name">
                                    <xsl:text>platform.active</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="value">
                                    <xsl:text>${platform.active}</xsl:text>
                                </xsl:attribute>
                            </xsl:element>
                            <xsl:element name="property">
                                <xsl:attribute name="name">
                                    <xsl:text>platform.properties.file.key</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="value">
                                    <xsl:text>${platform.properties.file.key}</xsl:text>
                                </xsl:attribute>
                            </xsl:element>
                            <xsl:element name="property">
                                <xsl:attribute name="name">
                                    <xsl:text>platform.device.folder.path</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="value">
                                    <xsl:text>${platform.device.folder.path}</xsl:text>
                                </xsl:attribute>
                            </xsl:element>
                        </xsl:if>
                    </xsl:element>
                </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="clean-dependencies">
        <xsl:for-each select="/project:project/project:configuration/jcproj:data/jcproj:dependencies/jcproj:dependency">
            <!-- Project root dependencies -->
            <xsl:if test="@kind = 'CLASSIC_LIB' or @kind = 'EXTENSION_LIB' or @kind = 'JAVA_PROJECT'">
                <xsl:element name="echo">
                    <xsl:attribute name="message">
                        <xsl:text>Cleaning dependency </xsl:text>
                        <xsl:value-of select="@id"/>
                        <xsl:text> (type:</xsl:text>
                        <xsl:value-of select="@kind"/>
                        <xsl:text>, deployment strategy </xsl:text>
                        <xsl:value-of select="@deployment"/>
                        <xsl:text>)</xsl:text>
                    </xsl:attribute>
                </xsl:element>
                <xsl:element name="ant">
                    <xsl:attribute name="target">
                        <xsl:text>clean</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="antfile">
                        <xsl:text>${dependency.</xsl:text>
                        <xsl:value-of select="@id"/>
                        <xsl:text>.origin}/build.xml</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="dir">
                        <xsl:text>${dependency.</xsl:text>
                        <xsl:value-of select="@id"/>
                        <xsl:text>.origin}</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="inheritAll">
                        <xsl:text>false</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="inheritRefs">
                        <xsl:text>false</xsl:text>
                    </xsl:attribute>
                    <xsl:if test="@kind = 'CLASSIC_LIB' or @kind = 'EXTENSION_LIB'">
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>user.properties.file</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${user.properties.file}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>active.device</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${active.device}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>platform.active</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${platform.active}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>platform.properties.file.key</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${platform.properties.file.key}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>platform.device.folder.path</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${platform.device.folder.path}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:if>
                </xsl:element>
            </xsl:if>
            <!-- deploying JAR files dependencies -->
            <xsl:if test="@kind = 'EXTENSION_LIB_JAR' or @kind = 'CLASSIC_LIB_JAR'">
                <fail>Classic and Extension Lib JAR undeployement w/o project not implemented</fail>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="unload-dependencies">
        <xsl:for-each select="/project:project/project:configuration/jcproj:data/jcproj:dependencies/jcproj:dependency">
            <xsl:if test="@deployment = 'DEPLOY_TO_CARD'">
                <!-- Project root dependencies -->
                <xsl:if test="@kind = 'CLASSIC_LIB' or @kind = 'EXTENSION_LIB'">
                    <xsl:element name="echo">
                        <xsl:attribute name="message">
                            <xsl:text>Unloading dependency </xsl:text>
                            <xsl:value-of select="@id"/>
                            <xsl:text> (type:</xsl:text>
                            <xsl:value-of select="@kind"/>
                            <xsl:text>, deployment strategy </xsl:text>
                            <xsl:value-of select="@deployment"/>
                            <xsl:text>)</xsl:text>
                        </xsl:attribute>
                    </xsl:element>
                    <xsl:element name="ant">
                        <xsl:attribute name="target">
                            <xsl:text>unload-bundle</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="antfile">
                            <xsl:text>${dependency.</xsl:text>
                            <xsl:value-of select="@id"/>
                            <xsl:text>.origin}/build.xml</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="dir">
                            <xsl:text>${dependency.</xsl:text>
                            <xsl:value-of select="@id"/>
                            <xsl:text>.origin}</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="inheritAll">
                            <xsl:text>false</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="inheritRefs">
                            <xsl:text>false</xsl:text>
                        </xsl:attribute>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>user.properties.file</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${user.properties.file}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>active.device</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${active.device}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>platform.active</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${platform.active}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>platform.properties.file.key</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${platform.properties.file.key}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:element name="property">
                            <xsl:attribute name="name">
                                <xsl:text>platform.device.folder.path</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:text>${platform.device.folder.path}</xsl:text>
                            </xsl:attribute>
                        </xsl:element>

                    </xsl:element>
                </xsl:if>
                <!-- deploying JAR files dependencies -->
                <xsl:if test="@kind = 'EXTENSION_LIB_JAR' or @kind = 'CLASSIC_LIB_JAR'">
                    <fail>Classic and Extension Lib JAR undeployement w/o project not implemented yet</fail>
                </xsl:if>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="unpack-dependencies">
        <xsl:for-each select="/project:project/project:configuration/jcproj:data/jcproj:dependencies/jcproj:dependency">
            <xsl:if test="@deployment = 'INCLUDE_IN_PROJECT_CLASSES'">
                <xsl:element name="echo">
                    <xsl:attribute name="message">
                        <xsl:text>Un-JAR-ing dependency </xsl:text>
                        <xsl:value-of select="@id"/>
                        <xsl:text> (type:</xsl:text>
                        <xsl:value-of select="@kind"/>
                        <xsl:text>, deployment strategy </xsl:text>
                        <xsl:value-of select="@deployment"/>
                        <xsl:text>)</xsl:text>
                    </xsl:attribute>
                </xsl:element>
                <xsl:element name="unjar">
                    <xsl:attribute name="dest">
                        <xsl:text>${build.classes.dir}</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="src">
                        <xsl:text>${dependency.</xsl:text>
                        <xsl:value-of select="@id"/>
                        <xsl:text>.origin}</xsl:text>
                    </xsl:attribute>
                </xsl:element>
            </xsl:if>
        </xsl:for-each>
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
        <xsl:for-each select="$roots/jcproj:root[@id != 'src.proxies.dir']">
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
