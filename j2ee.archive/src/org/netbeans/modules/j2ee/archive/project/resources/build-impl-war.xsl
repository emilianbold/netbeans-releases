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
                exclude-result-prefixes="xalan p projdeps">
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
            <xsl:attribute name="default">build</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>
            <import file="ant-deploy.xml" />

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

            <target name="-do-ear-init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-init-macrodef-property</xsl:attribute>
                <xsl:attribute name="if">dist.ear.dir</xsl:attribute>
                <property value="${{build.ear.web.dir}}/META-INF" name="build.meta.inf.dir"/>
                <property name="build.classes.dir.real" value="${{build.ear.classes.dir}}"/>
                <property name="build.web.dir.real" value="${{build.ear.web.dir}}"/>
            </target>

            <target name="-do-init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-init-macrodef-property, -do-ear-init</xsl:attribute>
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
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/archiveproject:data/archiveproject:test-roots"/>
                    <xsl:with-param name="propName">have.tests</xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/archiveproject:data/archiveproject:source-roots"/>
                    <xsl:with-param name="propName">have.sources</xsl:with-param>
                </xsl:call-template>
                <condition property="netbeans.home+have.tests">
                    <and>
                        <isset property="netbeans.home"/>
                        <isset property="have.tests"/>
                    </and>
                </condition>
                <property name="javac.compilerargs" value=""/>
                <condition property="no.deps">
                    <and>
                        <istrue value="${{no.dependencies}}"/>
                    </and>
                </condition>
                <condition property="no.dist.ear.dir">
                    <not>
                        <isset property="dist.ear.dir"/>
                    </not>
                </condition>
                <property name="build.web.excludes" value="${{build.classes.excludes}}"/>
                <condition property="do.compile.jsps">
                    <istrue value="${{compile.jsps}}"/>
                </condition>
                <condition property="do.display.browser">
                    <istrue value="${{display.browser}}"/>
                </condition>
                <available file="${{conf.dir}}/MANIFEST.MF" property="has.custom.manifest"/>
                <available file="${{conf.dir}}/persistence.xml" property="has.persistence.xml"/>
                
                <condition property="do.war.package.with.custom.manifest">
                    <and>
                        <istrue value="${{war.package}}"/>
                        <isset property="has.custom.manifest"/>
                    </and>
                </condition>
                <condition property="do.war.package.without.custom.manifest">
                    <and>
                        <istrue value="${{war.package}}"/>
                        <not>
                            <isset property="has.custom.manifest"/>
                        </not>
                    </and>
                </condition>
                
                <property value="${{build.web.dir}}/META-INF" name="build.meta.inf.dir"/>
                <property name="build.classes.dir.real" value="${{build.classes.dir}}"/>
                <property name="build.web.dir.real" value="${{build.web.dir}}"/>
                <property name="dist.archive" value="${{dist.dir}}/${{war.name}}"/>
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
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/archiveproject:data/archiveproject:source-roots"/>
                </xsl:call-template>
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/archiveproject:data/archiveproject:test-roots"/>
                </xsl:call-template>
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

            <target name="-init-macrodef-javac">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/archiveproject:data/archiveproject:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir.real}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.classpath}:${j2ee.platform.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">debug</xsl:attribute>
                        <xsl:attribute name="default">${javac.debug}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <javac>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:if test ="not(/p:project/p:configuration/archiveproject:data/archiveproject:explicit-platform/@explicit-source-supported ='false')">
                                <xsl:attribute name="source">${javac.source}</xsl:attribute>
                                <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            </xsl:if>
                            <xsl:if test="/p:project/p:configuration/archiveproject:data/archiveproject:explicit-platform">
                                <xsl:attribute name="fork">yes</xsl:attribute>
                                <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                                <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
                            </xsl:if>
                            <xsl:attribute name="includeantruntime">false</xsl:attribute>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <compilerarg line="${{javac.compilerargs}}"/>
                            <customize/>
                        </javac>
                    </sequential>
                 </macrodef>
            </target>

            <target name="-init-macrodef-junit">
                <macrodef>
                    <xsl:attribute name="name">junit</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">**/*Test.java</xsl:attribute>
                    </attribute>
                    <sequential>
                        <junit>
                            <xsl:attribute name="showoutput">true</xsl:attribute>
                            <xsl:attribute name="fork">true</xsl:attribute>
                            <xsl:attribute name="dir">${basedir}</xsl:attribute> <!-- #47474: match <java> -->
                            <xsl:attribute name="failureproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="errorproperty">tests.failed</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/archiveproject:data/archiveproject:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <batchtest todir="${{build.test.results.dir}}">
                                <xsl:call-template name="createFilesets">
                                    <xsl:with-param name="roots" select="/p:project/p:configuration/archiveproject:data/archiveproject:test-roots"/>
                                    <xsl:with-param name="includes">@{includes}</xsl:with-param>
                                </xsl:call-template>
                            </batchtest>
                            <classpath>
                                <path path="${{run.test.classpath}}"/>
                            </classpath>
                            <syspropertyset>
                                <propertyref prefix="test-sys-prop."/>
                                <mapper type="glob" from="test-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <formatter type="brief" usefile="false"/>
                            <formatter type="xml"/>
                        </junit>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-java">
                <macrodef>
                    <xsl:attribute name="name">java</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <java fork="true" classname="@{{classname}}">
                            <xsl:if test="/p:project/p:configuration/archiveproject:data/archiveproject:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <jvmarg line="${{runmain.jvmargs}}"/>
                            <classpath>
                                <path path="${{build.classes.dir.real}}:${{javac.classpath}}:${{j2ee.platform.classpath}}"/>
                            </classpath>
                            <syspropertyset>
                                <propertyref prefix="run-sys-prop."/>
                                <mapper type="glob" from="run-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <customize/>
                        </java>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-nbjpda" depends="-init-debug-args">
                <macrodef>
                    <xsl:attribute name="name">nbjpdastart</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">name</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <nbjpdastart transport="${{debug-transport}}" addressproperty="jpda.address" name="@{{name}}">
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <xsl:if test="/p:project/p:configuration/archiveproject:data/archiveproject:explicit-platform">
                                <bootclasspath>
                                    <path path="${{platform.bootcp}}"/>
                                </bootclasspath>
                            </xsl:if>
                        </nbjpdastart>
                    </sequential>
                </macrodef>
                <macrodef>
                    <xsl:attribute name="name">nbjpdareload</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">dir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir.real}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <nbjpdareload>
                            <fileset includes="${{fix.classes}}" dir="@{{dir}}" >
                                <include name="${{fix.includes}}*.class"/>
                            </fileset>
                        </nbjpdareload>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-debug-args">
                <xsl:choose>
                    <xsl:when test="/p:project/p:configuration/archiveproject:data/archiveproject:explicit-platform">
                        <exec executable="${{platform.java}}" outputproperty="version-output">
                            <arg value="-version"/>
                        </exec>
                    </xsl:when>
                    <xsl:otherwise>
                        <property name="version-output" value="java version &quot;${{ant.java.version}}"/>
                    </xsl:otherwise>
                </xsl:choose>
                <condition property="have-jdk-older-than-1.4">
                    <!-- <matches pattern="^java version &quot;1\.[0-3]" string="${version-output}"/> (ANT 1.7) -->
                    <or>
                        <contains string="${{version-output}}" substring="java version &quot;1.0"/>
                        <contains string="${{version-output}}" substring="java version &quot;1.1"/>
                        <contains string="${{version-output}}" substring="java version &quot;1.2"/>
                        <contains string="${{version-output}}" substring="java version &quot;1.3"/>
                    </or>
                </condition>
                <condition property="debug-args-line" value="-Xdebug -Xnoagent -Djava.compiler=none" else="-Xdebug">
                    <istrue value="${{have-jdk-older-than-1.4}}"/>
                </condition>
                <condition property="debug-transport-by-os" value="dt_shmem" else="dt_socket">
                    <os family="windows"/>
                </condition>
                <condition property="debug-transport" value="${{debug.transport}}" else="${{debug-transport-by-os}}">
                    <isset property="debug.transport"/>
                </condition>
            </target>

            <target name="-init-macrodef-debug" depends="-init-debug-args">
                <macrodef>
                    <xsl:attribute name="name">debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">args</xsl:attribute>
                        <xsl:attribute name="default">${application.args}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <java fork="true" classname="@{{classname}}">
                            <xsl:if test="/p:project/p:configuration/archiveproject:data/archiveproject:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <jvmarg line="${{debug-args-line}}"/>
                            <jvmarg value="-Xrunjdwp:transport=${{debug-transport}},address=${{jpda.address}}"/>
                            <jvmarg line="${{runmain.jvmargs}}"/>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <syspropertyset>
                                <propertyref prefix="run-sys-prop."/>
                                <mapper type="glob" from="run-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <arg line="@{{args}}"/>
                        </java>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-copy-ear-war">
                <macrodef name="copy-ear-war">
                    <attribute name="file"/>
                    <attribute name="propname"/>
                    <sequential>
                        <property name="temp.dirname" value="${{dist.ear.dir}}/temp"/>
                        <mkdir dir="${{dist.ear.dir}}/temp"/>
                        <basename property="base_@{{propname}}" file="@{{file}}"/>
                        <unzip src="@{{file}}" dest="${{temp.dirname}}">
                            <patternset>
                                <include name="META-INF/tlds/*.tld"/>
                            </patternset>
                        </unzip>
                        <available file="${{temp.dirname}}/META-INF/tlds" type="dir" property="hastlds_@{{propname}}"/>
                        <condition value="${{build.web.dir.real}}/WEB-INF/lib" property="copy.to.dir_@{{propname}}">
                            <isset property="hastlds_@{{propname}}"/>
                        </condition>
                        <condition value="${{dist.ear.dir}}" property="copy.to.dir_@{{propname}}">
                            <not>
                                <isset property="hastlds_@{{propname}}"/>
                            </not>
                        </condition>
                        <copy file="@{{file}}" todir="${{copy.to.dir_@{{propname}}}}"/>
                        <!--manifest handling-->
                        <condition value="${{base_@{{propname}}}}" property="@{{propname}}">
                            <not>
                                <isset property="hastlds_@{{propname}}"/>
                            </not>
                        </condition>
                        <condition value="" property="@{{propname}}">
                            <isset property="hastlds_@{{propname}}"/>
                        </condition>
                        <delete dir="${{dist.ear.dir}}/temp"/>
                    </sequential>
                </macrodef>
            </target>

            <target name="init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-do-init,-post-init,-init-check,-init-macrodef-property,-init-macrodef-javac,-init-macrodef-junit,-init-macrodef-java,-init-macrodef-nbjpda,-init-macrodef-debug,-init-macrodef-copy-ear-war</xsl:attribute>
            </target>

            <xsl:comment>
    ======================
    COMPILATION SECTION
    ======================
    </xsl:comment>
            <target name="-pre-pre-compile">
                <xsl:attribute name="depends">init<xsl:if test="/p:project/p:configuration/archiveproject:data/archiveproject:web-service-clients/archiveproject:web-service-client">,web-service-client-generate</xsl:if></xsl:attribute>
                <mkdir dir="${{build.classes.dir.real}}"/>
            </target>

            <target name="-pre-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-copy-webdir">
                <copy todir="${{build.web.dir.real}}">
                  <fileset excludes="${{build.web.excludes}}" dir="${{web.docbase.dir}}">
                   <xsl:if test="/p:project/p:configuration/archiveproject:data/archiveproject:web-services/archiveproject:web-service">
                     <xsl:attribute name="excludes">WEB-INF/classes/** WEB-INF/web.xml WEB/sun-web.xml</xsl:attribute>
                   </xsl:if>
                  </fileset>
                </copy>

                <xsl:if test="/p:project/p:configuration/archiveproject:data/archiveproject:web-services/archiveproject:web-service">
                    <xsl:comment>For web services, refresh web.xml and sun-web.xml</xsl:comment>
                    <copy todir="${{build.web.dir.real}}" overwrite="true">
                      <fileset includes="WEB-INF/web.xml WEB-INF/sun-web.xml" dir="${{web.docbase.dir}}"/>
                    </copy>
                 </xsl:if>
            </target>

            <target name="-do-compile">
                <xsl:attribute name="depends">init,  -pre-pre-compile, -pre-compile, -copy-manifest, -copy-webdir</xsl:attribute>
                <xsl:attribute name="if">have.sources</xsl:attribute>

                <archiveproject:javac destdir="${{build.classes.dir.real}}"/>

                <copy todir="${{build.classes.dir.real}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/archiveproject:data/archiveproject:source-roots"/>
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                </copy>
            </target>

            <target name="-copy-manifest" if="has.custom.manifest">
                <mkdir dir="${{build.meta.inf.dir}}"/>
                <copy todir="${{build.meta.inf.dir}}">
                    <fileset dir="${{conf.dir}}" includes="MANIFEST.MF"/>
                </copy>
            </target>
            
            <target name="-post-compile">
                <xsl:if test="/p:project/p:configuration/archiveproject:data/archiveproject:web-services/archiveproject:web-service">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/p:project/p:configuration/archiveproject:data/archiveproject:web-services/archiveproject:web-service">
                                <xsl:if test="position()!=1"><xsl:text>, </xsl:text>
                                </xsl:if>
                                <xsl:choose>
                                  <xsl:when test="not(archiveproject:from-wsdl)">
                                    <xsl:value-of select="archiveproject:web-service-name"/><xsl:text>_wscompile</xsl:text>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <xsl:text>fromwsdl-noop</xsl:text>
                                  </xsl:otherwise>
                                </xsl:choose>
                         </xsl:for-each>
                    </xsl:attribute>
                </xsl:if>
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="compile">
                <xsl:attribute name="depends">init,-pre-pre-compile,-pre-compile,-do-compile,-post-compile</xsl:attribute>
                <xsl:attribute name="description">Compile project.</xsl:attribute>
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


            <target name="-do-dist">
                <mkdir dir="${{dist.dir}}"/>
                <jar jarfile="${{dist.archive}}">
                    <fileset dir="${{content.dir}}"/>
                </jar>
            </target>
            
            <target name="-post-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="dist">
                <xsl:attribute name="depends">init,-pre-dist,-do-dist,-post-dist</xsl:attribute>
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



    <xsl:comment>

    ======================
    CLEANUP SECTION
    ======================
    </xsl:comment>

            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-clean'"/>
            </xsl:call-template>

            <target name="do-clean">
                <xsl:attribute name="depends">init</xsl:attribute>

                <condition value="${{build.web.dir.real}}" property="build.dir.to.clean">
                    <isset property="dist.ear.dir"/>
                </condition>
                <property value="${{build.web.dir.real}}" name="build.dir.to.clean"/>

                <delete includeEmptyDirs="true" quiet="true">
                    <fileset dir="${{build.dir.to.clean}}/WEB-INF/lib"/>
                </delete>
                <delete includeEmptyDirs="true">
                    <fileset dir=".">
                        <include name="${{build.dir}}/**"/>
                        <exclude name="${{build.dir.to.clean}}/WEB-INF/lib/**"/>
                    </fileset>
                </delete>
                <available file="${{build.dir.to.clean}}/WEB-INF/lib" type="dir" property="status.clean-failed"/>
                <delete dir="${{dist.dir}}"/>
                <!-- XXX explicitly delete all build.* and dist.* dirs in case they are not subdirs -->
                <!--
                <delete dir="${{build.generated.dir}}"/>
                <delete dir="${{build.web.dir.real}}"/>
                -->
            </target>

            <target name="check-clean">
                <xsl:attribute name="depends">do-clean</xsl:attribute>
                <xsl:attribute name="if">status.clean-failed</xsl:attribute>
                <!--
                    When undeploy is implemented it should be optional:
                <xsl:attribute name="unless">clean.check.skip</xsl:attribute>
                -->
                <echo message="Warning: unable to delete some files in ${{build.web.dir.real}}/WEB-INF/lib - they are probably locked by the J2EE server. " />
                <echo level="info" message="To delete all files undeploy the module from Server Registry in Runtime tab and then use Clean again."/>
                <!--
                    Here comes the undeploy code when supported by nbdeploy task:
                <nbdeploy undeploy="true" clientUrlPart="${client.urlPart}"/>
                    And then another attempt to delete:
                <delete dir="${{build.web.dir.real}}/WEB-INF/lib"/>
                -->
            </target>

            <target name="-post-clean">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="clean">
                <xsl:attribute name="depends">init,deps-clean,do-clean,check-clean,-post-clean</xsl:attribute>
                <xsl:attribute name="description">Clean build products.</xsl:attribute>
            </target>

            <target name="clean-ear">
                <xsl:attribute name="depends">clean</xsl:attribute>
                <xsl:attribute name="description">Clean build products.</xsl:attribute>
            </target>

        </project>

<!-- TBD items:

Could pass <propertyset> to run, debug, etc. under Ant 1.6,
optionally, by doing e.g.

  <propertyset>
    <propertyref prefix="sysprop."/>
    <mapper type="glob" from="sysprop.*" to="*"/>
  </propertyset>

Now user can add to e.g. project.properties e.g.:
  sysprop.org.netbeans.modules.javahelp=0
to simulate
  -Dorg.netbeans.modules.javahelp=0

-->

    </xsl:template>

    <!---
    Generic template to build subdependencies of a certain type.
    Feel free to copy into other modules.
    @param targetname required name of target to generate
    @param type artifact-type from project.xml to filter on; optional, if not specified, uses
                all references, and looks for clean targets rather than build targets
    @return an Ant target which builds (or cleans) all known subprojects
    -->
    <xsl:template name="deps.target">
        <xsl:param name="targetname"/>
        <xsl:param name="type"/>
        <xsl:param name="ear"/>
        <target name="{$targetname}">
            <xsl:attribute name="depends">init</xsl:attribute>

            <xsl:choose>
                <xsl:when test="$ear">
                    <xsl:attribute name="if">dist.ear.dir</xsl:attribute>
                    <xsl:attribute name="unless">no.deps</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="if">no.dist.ear.dir</xsl:attribute>
                    <xsl:attribute name="unless">no.deps</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>

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
                <!-- Distinguish build of a dependent project as standalone module or as a part of an ear -->
                <xsl:choose>
                    <xsl:when test="$ear">
                        <xsl:choose>
                            <!-- call standart target if the artifact type is jar (java libraries) -->
                            <xsl:when test="$subtarget = 'jar'">
                                <ant target="{$subtarget}" inheritall="false" antfile="${{project.{$subproj}}}/{$script}"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <ant target="dist-ear" inheritall="false" antfile="${{project.{$subproj}}}/{$script}">
                                    <property name="dist.ear.dir" location="${{build.dir}}"/>
                                </ant>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <ant target="{$subtarget}" inheritall="false" antfile="${{project.{$subproj}}}/{$script}"/>
                    </xsl:otherwise>
                </xsl:choose>

            </xsl:for-each>
        </target>
    </xsl:template>

    <xsl:template name="createRootAvailableTest">
        <xsl:param name="roots"/>
        <xsl:param name="propName"/>
        <xsl:element name="condition">
            <xsl:attribute name="property"><xsl:value-of select="$propName"/></xsl:attribute>
            <or>
                <xsl:for-each select="$roots/archiveproject:root">
                    <xsl:element name="available">
			        <xsl:attribute name="file"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
		            </xsl:element>
                </xsl:for-each>
            </or>
        </xsl:element>
    </xsl:template>

    <xsl:template name="createSourcePathValidityTest">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/archiveproject:root">
            <xsl:element name="fail">
			    <xsl:attribute name="unless"><xsl:value-of select="@id"/></xsl:attribute>
                <xsl:text>Must set </xsl:text><xsl:value-of select="@id"/>
		    </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="createFilesets">
		<xsl:param name="roots"/>
        <xsl:param name="includes"/>
        <xsl:param name="excludes"/>
        <xsl:for-each select="$roots/archiveproject:root">
            <xsl:element name="fileset">
			    <xsl:attribute name="dir"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
                <xsl:if test="$includes">
                    <xsl:attribute name="includes"><xsl:value-of select="$includes"/></xsl:attribute>
                </xsl:if>
                <xsl:if test="$excludes">
			        <xsl:attribute name="excludes"><xsl:value-of select="$excludes"/></xsl:attribute>
                </xsl:if>
		    </xsl:element>
        </xsl:for-each>
    </xsl:template>
        
    <xsl:template name="createPackagesets">
        <xsl:param name="roots"/>
        <xsl:param name="includes"/>
        <xsl:param name="excludes"/>
        <xsl:for-each select="$roots/archiveproject:root">
            <xsl:element name="packageset">
                <xsl:attribute name="dir"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
                <xsl:if test="$includes">
                    <xsl:attribute name="includes"><xsl:value-of select="$includes"/></xsl:attribute>
                </xsl:if>
                <xsl:if test="$excludes">
                    <xsl:attribute name="excludes"><xsl:value-of select="$excludes"/></xsl:attribute>
                </xsl:if>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>        

    <xsl:template name="createPathElements">
        <xsl:param name="locations"/>
        <xsl:for-each select="$locations/archiveproject:root">
            <xsl:element name="pathelement">
			    <xsl:attribute name="location"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
		    </xsl:element>
        </xsl:for-each>
    </xsl:template>

	<xsl:template name="createPath">
		<xsl:param name="roots"/>
		<xsl:for-each select="$roots/archiveproject:root">
		    <xsl:if test="position() != 1">
			    <xsl:text>:</xsl:text>
		    </xsl:if>
		    <xsl:text>${</xsl:text>
		    <xsl:value-of select="@id"/>
		    <xsl:text>}</xsl:text>
		</xsl:for-each>
	</xsl:template>

        <xsl:template name="manifestBasenameIterateFiles" >
            <xsl:param name="property"/>
            <xsl:param name="files" /><!-- number of files in the libfile property -->
            <xsl:param name="libfile"/>
            <xsl:if test="$files &gt; 0">
                <xsl:variable name="fileNo" select="$files+(-1)"/>
                <xsl:variable name="lib" select="concat(substring-before($libfile,'}'),'.libfile.',$files,'}')"/>
                <xsl:variable name="propertyName" select="concat($property, '.', $fileNo+1)"/>
                <basename property="{$propertyName}" file="{$lib}"/>
                <xsl:call-template name="manifestBasenameIterateFiles">
                    <xsl:with-param name="files" select="$fileNo"/>
                    <xsl:with-param name="libfile" select="$libfile"/>
                    <xsl:with-param name="property" select="$property"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:template>

        <xsl:template name="manifestPrintEntriesIterateFiles" >
            <xsl:param name="property"/>
            <xsl:param name="files" /><!-- number of files in the libfile property -->
            <xsl:param name="libfile"/>
            <xsl:if test="$files &gt; 0">
                <xsl:call-template name="manifestPrintEntriesIterateFilesIncreasingOrder">
                    <xsl:with-param name="files" select="$files"/>
                    <xsl:with-param name="libfile" select="$libfile"/>
                    <xsl:with-param name="index" select="1"/>
                    <xsl:with-param name="property" select="$property"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:template>

        <xsl:template name="manifestPrintEntriesIterateFilesIncreasingOrder" >
            <xsl:param name="property"/>
            <xsl:param name="files" /><!-- number of files in the libfile property -->
            <xsl:param name="index" /><!-- index of file in libfile property -->
            <xsl:param name="libfile"/>
            <xsl:if test="$files &gt; 0">
                <xsl:variable name="propertyName" select="concat($property, '.', $index)"/>
                <xsl:text>${</xsl:text><xsl:value-of select="$propertyName"/><xsl:text>.X} </xsl:text>
                <xsl:call-template name="manifestPrintEntriesIterateFilesIncreasingOrder">
                    <xsl:with-param name="files" select="$files+(-1)"/>
                    <xsl:with-param name="index" select="$index+1"/>
                    <xsl:with-param name="libfile" select="$libfile"/>
                    <xsl:with-param name="property" select="$property"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:template>

        <xsl:template name="copyIterateFiles" >
            <xsl:param name="files" />
            <xsl:param name="target"/>
            <xsl:param name="libfile"/>
            <xsl:param name="ear"/>
            <xsl:param name="property"/>
            <xsl:if test="$files &gt; 0">
                <xsl:variable name="fileNo" select="$files+(-1)"/>
                <xsl:variable name="lib" select="concat(substring-before($libfile,'}'),'.libfile.',$files,'}')"/>
                
                <xsl:variable name="propertyName" select="concat($property, '.', $files, '.X')"/>
                <xsl:if test="$ear='true'">
                    <copy-ear-war file="{$lib}" propname="{$propertyName}"/>
                </xsl:if>
                
                <xsl:if test="$ear!='true'">
                    <copy file="{$lib}" todir="{$target}"/>
                </xsl:if>
                
                <xsl:call-template name="copyIterateFiles">
                    <xsl:with-param name="files" select="$fileNo"/>
                    <xsl:with-param name="target" select="$target"/>
                    <xsl:with-param name="libfile" select="$libfile"/>
                    <xsl:with-param name="ear" select="$ear"/>
                    <xsl:with-param name="property" select="$property"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:template>

        <xsl:template name="copyIterateDirs" >
            <xsl:param name="files" />
            <xsl:param name="target"/>
            <xsl:param name="libfile"/>
            <xsl:if test="$files &gt; 0">
                <xsl:variable name="fileNo" select="$files+(-1)"/>
                <xsl:variable name="lib" select="concat(substring-before($libfile,'}'),'.libdir.',$files,'}')"/>
                <copy todir="{$target}">
                    <fileset dir="{$lib}" includes="**/*"/>
                </copy>
                <xsl:call-template name="copyIterateDirs">
                    <xsl:with-param name="files" select="$fileNo"/>
                    <xsl:with-param name="target" select="$target"/>
                    <xsl:with-param name="libfile" select="$libfile"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:template>
</xsl:stylesheet>
