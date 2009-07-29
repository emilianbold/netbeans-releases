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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
                xmlns:carproject="http://www.netbeans.org/ns/car-project/1"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                xmlns:projdeps2="http://www.netbeans.org/ns/ant-project-references/2"
                xmlns:libs="http://www.netbeans.org/ns/ant-project-libraries/1"
                exclude-result-prefixes="xalan p projdeps projdeps2 libs">
    <!-- XXX should use namespaces for NB in-VM tasks from ant/browsetask and debuggerjpda/ant (Ant 1.6.1 and higher only) -->
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    
    <xsl:template match="/">
        
        <xsl:comment><![CDATA[
        *** GENERATED FROM project.xml - DO NOT EDIT  ***
        ***         EDIT ../build.xml INSTEAD         ***

        For the purpose of easier reading the script
        is divided into following sections:

        - initialization
        - compilation
        - jar
        - execution
        - debugging
        - javadoc
        - junit compilation
        - junit execution
        - junit debugging
        - applet
        - cleanup

        ]]></xsl:comment>
        
        <xsl:variable name="name" select="/p:project/p:configuration/carproject:data/carproject:name"/>
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
                <xsl:attribute name="depends">dist,javadoc</xsl:attribute>
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
            
            <target name="-pre-init-am">
                <xsl:comment> Access Manager pre-initialization </xsl:comment>
            </target>
            
            <target name="-init-private">
                <xsl:attribute name="depends">-pre-init</xsl:attribute>
                <property file="nbproject/private/private.properties"/>
            </target>
            
            <xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">
                <target name="-init-libraries" depends="-pre-init,-init-private">
                    <xsl:for-each select="/p:project/p:configuration/libs:libraries/libs:definitions">
                        <property name="libraries.{position()}.path" location="{.}"/>
                        <dirname property="libraries.{position()}.dir.nativedirsep" file="${{libraries.{position()}.path}}"/>
                        <!-- Do not want \ on Windows, since it would act as an escape char: -->
                        <pathconvert property="libraries.{position()}.dir" dirsep="/">
                            <path path="${{libraries.{position()}.dir.nativedirsep}}"/>
                        </pathconvert>
                        <basename property="libraries.{position()}.basename" file="${{libraries.{position()}.path}}" suffix=".properties"/>
                        <touch file="${{libraries.{position()}.dir}}/${{libraries.{position()}.basename}}-private.properties"/> <!-- has to exist, yuck -->
                        <loadproperties srcfile="${{libraries.{position()}.dir}}/${{libraries.{position()}.basename}}-private.properties" encoding="ISO-8859-1">
                            <filterchain>
                                <replacestring from="$${{base}}" to="${{libraries.{position()}.dir}}"/>
                                <escapeunicode/>
                            </filterchain>
                        </loadproperties>
                        <loadproperties srcfile="${{libraries.{position()}.path}}" encoding="ISO-8859-1">
                            <filterchain>
                                <replacestring from="$${{base}}" to="${{libraries.{position()}.dir}}"/>
                                <escapeunicode/>
                            </filterchain>
                        </loadproperties>
                    </xsl:for-each>
                </target>
            </xsl:if>
            
            <target name="-init-user">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if></xsl:attribute>                
                <property file="${{user.properties.file}}"/>
                <xsl:comment> The two properties below are usually overridden </xsl:comment>
                <xsl:comment> by the active platform. Just a fallback. </xsl:comment>
                <property name="default.javac.source" value="1.4"/>
                <property name="default.javac.target" value="1.4"/>
            </target>
            
            <target name="-init-project">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user</xsl:attribute>
                <property file="nbproject/project.properties"/>
            </target>
            
            <target name="-do-init">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-init-macrodef-property</xsl:attribute>
                <xsl:if test="/p:project/p:configuration/carproject:data/carproject:explicit-platform">
                    <carproject:property name="platform.home" value="platforms.${{platform.active}}.home"/>
                    <carproject:property name="platform.bootcp" value="platforms.${{platform.active}}.bootclasspath"/>
                    <carproject:property name="platform.compiler" value="platforms.${{platform.active}}.compile"/>
                    <carproject:property name="platform.javac.tmp" value="platforms.${{platform.active}}.javac"/>
                    <condition property="platform.javac" value="${{platform.home}}/bin/javac">
                        <equals arg1="${{platform.javac.tmp}}" arg2="$${{platforms.${{platform.active}}.javac}}"/>
                    </condition>
                    <property name="platform.javac" value="${{platform.javac.tmp}}"/>
                    <carproject:property name="platform.java.tmp" value="platforms.${{platform.active}}.java"/>
                    <condition property="platform.java" value="${{platform.home}}/bin/java">
                        <equals arg1="${{platform.java.tmp}}" arg2="$${{platforms.${{platform.active}}.java}}"/>
                    </condition>
                    <property name="platform.java" value="${{platform.java.tmp}}"/>
                    <carproject:property name="platform.javadoc.tmp" value="platforms.${{platform.active}}.javadoc"/>
                    <condition property="platform.javadoc" value="${{platform.home}}/bin/javadoc">
                        <equals arg1="${{platform.javadoc.tmp}}" arg2="$${{platforms.${{platform.active}}.javadoc}}"/>
                    </condition>
                    <property name="platform.javadoc" value="${{platform.javadoc.tmp}}"/>
                    <condition property="platform.invalid" value="true">
                        <or>
                            <contains string="${{platform.javac}}" substring="$${{platforms."/>
                            <contains string="${{platform.java}}" substring="$${{platforms."/>
                            <contains string="${{platform.javadoc}}" substring="$${{platforms."/>
                        </or>
                    </condition>
                    <fail unless="platform.home">Must set platform.home</fail>
                    <fail unless="platform.bootcp">Must set platform.bootcp</fail>
                    <fail unless="platform.java">Must set platform.java</fail>
                    <fail unless="platform.javac">Must set platform.javac</fail>
  <fail if="platform.invalid">
 The J2SE Platform is not correctly set up.
 Your active platform is: ${platform.active}, but the corresponding property "platforms.${platform.active}.home" is not found in the project's properties files. 
 Either open the project in the IDE and setup the Platform with the same name or add it manually.
 For example like this:
     ant -Duser.properties.file=&lt;path_to_property_file&gt; jar (where you put the property "platforms.${platform.active}.home" in a .properties file)
  or ant -Dplatforms.${platform.active}.home=&lt;path_to_JDK_home&gt; jar (where no properties file is used) 
  </fail>
                </xsl:if>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:test-roots"/>
                    <xsl:with-param name="propName">have.tests</xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:source-roots"/>
                    <xsl:with-param name="propName">have.sources</xsl:with-param>
                </xsl:call-template>
                <condition property="netbeans.home+have.tests">
                    <and>
                        <isset property="netbeans.home"/>
                        <isset property="have.tests"/>
                    </and>
                </condition>
                <property name="javadoc.preview" value="true"/>
                <condition property="no.javadoc.preview">
                    <isfalse value="${{javadoc.preview}}"/>
                </condition>
                <property name="javac.compilerargs" value=""/>
                <property name="work.dir" value="${{basedir}}"/>
                <condition property="no.deps">
                    <and>
                        <istrue value="${{no.dependencies}}"/>
                    </and>
                </condition>
                <property name="javac.debug" value="true"/>
                <available file="${{meta.inf}}/MANIFEST.MF" property="has.custom.manifest"/>
                <condition property="classes.dir" value="${{build.ear.classes.dir}}">
                    <isset property="dist.ear.dir"/>
                </condition>
                <property name="classes.dir" value="${{build.classes.dir}}"/>
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
                <condition property="do.display.browser">
                    <istrue value="${{display.browser}}"/>
                </condition>
                <condition property="application.args.param" value="${{application.args}}" else="">
                    <and>
                        <isset property="application.args"/>
                        <not>
                            <equals arg1="${{application.args}}" arg2="" trim="true"/>
                        </not>
                    </and>
                </condition>
                <condition property="run.jvmargs.param" value="${{run.jvmargs}}" else="">
                    <and>
                        <isset property="run.jvmargs"/>
                        <not>
                            <equals arg1="${{run.jvmargs}}" arg2="" trim="true"/>
                        </not>
                    </and>
                </condition>
                <property name="source.encoding" value="${{file.encoding}}"/>
                <condition property="javadoc.encoding.used" value="${{javadoc.encoding}}">
                    <and>
                        <isset property="javadoc.encoding"/>
                        <not>
                            <equals arg1="${{javadoc.encoding}}" arg2=""/>
                        </not>
                    </and>
                </condition> 
                <property name="javadoc.encoding.used" value="${{source.encoding}}"/>
                <property name="includes" value="**"/>
                <property name="excludes" value=""/>
                <condition property="javac.compilerargs.jaxws" value="-Djava.endorsed.dirs='${{jaxws.endorsed.dir}}'" else="">
                    <and>
                        <isset property="jaxws.endorsed.dir"/>
                        <available file="nbproject/jaxws-build.xml"/>
                    </and>
                </condition>
            </target>
            
            <target name="-post-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-init-check">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-do-init</xsl:attribute>
                <!-- XXX XSLT 2.0 would make it possible to use a for-each here -->
                <!-- Note that if the properties were defined in project.xml that would be easy -->
                <!-- But required props should be defined by the AntBasedProjectType, not stored in each project -->
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:source-roots"/>
                </xsl:call-template>
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:test-roots"/>
                </xsl:call-template>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.dir">Must set build.classes.dir</fail>
                <fail unless="build.generated.dir">Must set build.generated.dir</fail>
                <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail>
                <fail unless="build.test.classes.dir">Must set build.test.classes.dir</fail>
                <fail unless="build.test.results.dir">Must set build.test.results.dir</fail>
                <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>
                <fail unless="dist.jar">Must set dist.jar</fail>
                <fail unless="j2ee.platform.classpath">
The Java EE server classpath is not correctly set up. Your active server type is ${j2ee.server.type}.
Either open the project in the IDE and assign the server or setup the server classpath manually.
For example like this:
   ant -Duser.properties.file=&lt;path_to_property_file&gt; (where you put the property "j2ee.platform.classpath" in a .properties file)
or ant -Dj2ee.platform.classpath=&lt;server_classpath&gt; (where no properties file is used)
                </fail>                
            </target>
            
            <target name="-init-macrodef-property">
                <macrodef>
                    <xsl:attribute name="name">property</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/car-project/1</xsl:attribute>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/car-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.classpath}:${j2ee.platform.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">debug</xsl:attribute>
                        <xsl:attribute name="default">${javac.debug}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">gensrcdir</xsl:attribute>
                        <xsl:attribute name="default">${empty.dir}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property name="empty.dir" location="${{build.dir}}/empty"/><!-- #157692 -->
                        <mkdir dir="${{empty.dir}}"/>
                        <javac>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <!-- XXX #137060 likely needs to be fixed here -->
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                            <xsl:if test ="not(/p:project/p:configuration/carproject:data/carproject:explicit-platform/@explicit-source-supported ='false')">                            
                                <xsl:attribute name="source">${javac.source}</xsl:attribute>
                                <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            </xsl:if>                            
                            <xsl:attribute name="includes">@{includes}</xsl:attribute>
                            <xsl:attribute name="excludes">@{excludes}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/carproject:data/carproject:explicit-platform">
                                <xsl:attribute name="fork">yes</xsl:attribute>
                                <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                                <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
                            </xsl:if>
                            <xsl:attribute name="includeantruntime">false</xsl:attribute>
                            <src>
                                <dirset dir="@{{gensrcdir}}" erroronmissingdir="false">
                                    <include name="*"/>
                                </dirset>
                            </src>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <compilerarg line="${{javac.compilerargs}} ${{javac.compilerargs.jaxws}}"/>
                            <customize/>
                        </javac>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-junit">
                <macrodef>
                    <xsl:attribute name="name">junit</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/car-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <sequential>
                        <junit>
                            <xsl:attribute name="showoutput">true</xsl:attribute>
                            <xsl:attribute name="fork">true</xsl:attribute>
                            <xsl:attribute name="dir">${basedir}</xsl:attribute> <!-- #47474: match <java> --> 
                            <xsl:attribute name="failureproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="errorproperty">tests.failed</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/carproject:data/carproject:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <batchtest todir="${{build.test.results.dir}}">
                                <xsl:call-template name="createFilesets">
                                    <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:test-roots"/>
                                    <xsl:with-param name="includes">@{includes}</xsl:with-param>
                                    <xsl:with-param name="includes2">@{testincludes}</xsl:with-param>
                                    <xsl:with-param name="excludes">@{excludes}</xsl:with-param>
                                </xsl:call-template>
                            </batchtest>
                            <classpath>
                                <path path="${{run.test.classpath}}"/>
                                <path path="${{j2ee.platform.classpath}}"/>
                            </classpath>
                            <syspropertyset>
                                <propertyref prefix="test-sys-prop."/>
                                <mapper type="glob" from="test-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <formatter type="brief" usefile="false"/>
                            <formatter type="xml"/>
                            <jvmarg line="${{run.jvmargs.param}}"/>
                        </junit>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-java">
                <macrodef>
                    <xsl:attribute name="name">java</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/car-project/1</xsl:attribute>
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
                            <xsl:attribute name="dir">${work.dir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/carproject:data/carproject:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <jvmarg line="${{j2ee.appclient.tool.jvmoptions}}"/>
                            <jvmarg line="${{run.jvmargs.param}}"/>
                            <classpath>
                                <path path="${{dist.jar}}:${{javac.classpath}}:${{j2ee.platform.classpath}}:${{j2ee.appclient.tool.runtime}}"/>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/car-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">name</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">stopclassname</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <sequential>
                        <!--<nbjpdastart transport="dt_socket" addressproperty="jpda.address" name="@{{name}}">-->
                        <nbjpdastart transport="${{debug-transport}}" addressproperty="jpda.address" name="@{{name}}" stopclassname="@{{stopclassname}}">
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <xsl:if test="/p:project/p:configuration/carproject:data/carproject:explicit-platform">
                                <bootclasspath>
                                    <path path="${{platform.bootcp}}"/>
                                </bootclasspath>
                            </xsl:if>
                        </nbjpdastart>
                    </sequential>
                </macrodef>
                <macrodef>
                    <xsl:attribute name="name">nbjpdareload</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/car-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">dir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <nbjpdareload>
                            <fileset includes="${{fix.classes}}" dir="@{{dir}}" >
                                <include name="${{fix.includes}}*.class"/>
                            </fileset>
                        </nbjpdareload>
                    </sequential>
                </macrodef>
                <macrodef>
                    <xsl:attribute name="name">nbjpdaappreloaded</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <sequential>
                        <nbjpdaappreloaded />
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-debug-args">
                <xsl:choose>
                    <xsl:when test="/p:project/p:configuration/carproject:data/carproject:explicit-platform">
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/car-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <attribute>
                        <xsl:attribute name="name">args</xsl:attribute>
                        <xsl:attribute name="default">${application.args.param}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <java fork="true" classname="@{{classname}}">
                            <xsl:attribute name="dir">${work.dir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/carproject:data/carproject:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                                <bootclasspath>
                                    <path path="${{platform.bootcp}}"/>
                                </bootclasspath>
                            </xsl:if>
                            <jvmarg line="${{debug-args-line}}"/>
                            <jvmarg value="-Xrunjdwp:transport=${{debug-transport}},address=${{jpda.address}}"/>
                            <jvmarg line="${{run.jvmargs.param}}"/>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <syspropertyset>
                                <propertyref prefix="run-sys-prop."/>
                                <mapper type="glob" from="run-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <arg line="@{{args}}"/>
                            <customize/>
                        </java>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-debug-appclient" depends="-init-debug-args">
                <macrodef>
                    <xsl:attribute name="name">debug-appclient</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/car-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">mainclass</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <attribute>
                        <xsl:attribute name="name">args</xsl:attribute>
                        <xsl:attribute name="default">${application.args.param}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <parallel>
                            <java fork="true" classname="@{{mainclass}}">
                                <xsl:attribute name="dir">${work.dir}</xsl:attribute>
                                <xsl:if test="/p:project/p:configuration/carproject:data/carproject:explicit-platform">
                                    <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                                    <bootclasspath>
                                        <path path="${{platform.bootcp}}"/>
                                    </bootclasspath>
                                </xsl:if>
                                <jvmarg line="${{j2ee.appclient.tool.jvmoptions}}"/>
                                <jvmarg line="${{debug-args-line}}"/>
                                <jvmarg value="-Xrunjdwp:transport=${{jpda.transport}},server=y,address=${{jpda.address}},suspend=y"/>
                                <jvmarg line="${{run.jvmargs.param}}"/>
                                <arg line="@{{args}}"/>
                                <classpath>
                                    <path path="${{javac.classpath}}:${{j2ee.platform.classpath}}:${{j2ee.appclient.tool.runtime}}"/>
                                    <path path="@{{classpath}}"/>
                                </classpath>
                                <syspropertyset>
                                    <propertyref prefix="run-sys-prop."/>
                                    <mapper type="glob" from="run-sys-prop.*" to="*"/>
                                </syspropertyset>
                                <customize/>
                            </java>
                            <nbjpdaconnect name="${{name}}" host="${{jpda.host}}" address="${{jpda.address}}" transport="${{jpda.transport}}">
                                <classpath>
                                    <path path="${{javac.classpath}}:${{j2ee.platform.classpath}}:${{j2ee.appclient.tool.runtime}}"/>
                                    <path path="@{{classpath}}"/>
                                </classpath>
                                <sourcepath>
                                    <path path="${{src.dir}}"/>
                                </sourcepath>
                            </nbjpdaconnect>
                        </parallel>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-taskdefs">
                <fail unless="libs.CopyLibs.classpath">
The libs.CopyLibs.classpath property is not set up.
This property must point to 
org-netbeans-modules-java-j2seproject-copylibstask.jar file which is part
of NetBeans IDE installation and is usually located at 
&lt;netbeans_installation&gt;/java&lt;version&gt;/ant/extra folder.
Either open the project in the IDE and make sure CopyLibs library
exists or setup the property manually. For example like this:
 ant -Dlibs.CopyLibs.classpath=a/path/to/org-netbeans-modules-java-j2seproject-copylibstask.jar
                </fail>
                <taskdef resource="org/netbeans/modules/java/j2seproject/copylibstask/antlib.xml" classpath="${{libs.CopyLibs.classpath}}"/>
            </target>

            <target name="init">
                <xsl:attribute name="depends">-pre-init,-pre-init-am,-init-private,-init-user,-init-project,-do-init,-post-init,-init-check,-init-macrodef-property,-init-macrodef-javac,-init-macrodef-junit,-init-macrodef-java,-init-macrodef-nbjpda,-init-macrodef-debug,-init-macrodef-debug-appclient,-init-taskdefs</xsl:attribute>
            </target>
            
            <xsl:comment>
                ===================
                COMPILATION SECTION
                ===================
            </xsl:comment>
            
            <target name="-check-main-class">
                <fail>
                    Main class must be set. Go to <xsl:value-of select="$name"/> project properties -> Run and set the main class there.
                    <condition>
                        <not>
                            <or>
                                <isset property="dist.ear.dir"/>
                                <isset property="main.class"/>
                            </or>
                        </not>
                    </condition>
                </fail>
            </target>
            
            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'-deps-module-jar'"/>
                <xsl:with-param name="type" select="'jar'"/>
            </xsl:call-template>
            
            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'-deps-ear-jar'"/>
                <xsl:with-param name="type" select="'jar'"/>
                <xsl:with-param name="ear" select="'true'"/>
            </xsl:call-template>
            
            <target name="deps-jar">
                <xsl:attribute name="depends">init, -check-main-class, -deps-module-jar, -deps-ear-jar</xsl:attribute>
            </target>
                     
            <xsl:if test="/p:project/p:configuration/carproject:data/carproject:web-service-clients/carproject:web-service-client">
                <target name="wscompile-init" depends="init">
                    <taskdef name="wscompile" classname="com.sun.xml.rpc.tools.ant.Wscompile"
                             classpath="${{wscompile.classpath}}"/>
                    <taskdef name="wsclientuptodate" classname="org.netbeans.modules.websvc.jaxrpc.ant.WsClientUpToDate"
                             classpath="${{wsclientuptodate.classpath}}"/>
                    
                    <mkdir dir="${{classes.dir}}"/>
                    <mkdir dir="${{build.generated.sources.dir}}/jax-rpc"/>
                    
                    <xsl:for-each select="/p:project/p:configuration/carproject:data/carproject:web-service-clients/carproject:web-service-client">
                        <xsl:variable name="wsclientname">
                            <xsl:value-of select="carproject:web-service-client-name"/>
                        </xsl:variable>
                        
                        <wsclientuptodate property="wscompile.client.{$wsclientname}.notrequired"
                                          sourcewsdl="${{meta.inf}}/wsdl/{$wsclientname}.wsdl"
                                          targetdir="${{build.generated.sources.dir}}/jax-rpc"/>
                    </xsl:for-each>
                </target>
            </xsl:if>
            
            <xsl:for-each select="/p:project/p:configuration/carproject:data/carproject:web-service-clients/carproject:web-service-client">
                <xsl:variable name="wsclientname">
                    <xsl:value-of select="carproject:web-service-client-name"/>
                </xsl:variable>
                <xsl:variable name="useimport">
                    <xsl:choose>
                        <xsl:when test="carproject:web-service-stub-type">
                            <xsl:value-of select="carproject:web-service-stub-type='jsr-109_client'"/>
                        </xsl:when>
                        <xsl:otherwise>true</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="useclient">
                    <xsl:choose>
                        <xsl:when test="carproject:web-service-stub-type">
                            <xsl:value-of select="carproject:web-service-stub-type='jaxrpc_static_client'"/>
                        </xsl:when>
                        <xsl:otherwise>false</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                
                <target name="{$wsclientname}-client-wscompile" depends="wscompile-init" unless="wscompile.client.{$wsclientname}.notrequired">
                    <property name="config_target" location="${{meta.inf}}/wsdl"/>
                    <copy file="${{meta.inf}}/wsdl/{$wsclientname}-config.xml"
                          tofile="${{build.generated.sources.dir}}/jax-rpc/wsdl/{$wsclientname}-config.xml" filtering="on" encoding="UTF-8">
                        <filterset>
                            <!-- replace token with reference to WSDL file in source tree, not build tree, since the
                            the file probably has not have been copied to the build tree yet. -->
                            <filter token="CONFIG_ABSOLUTE_PATH" value="${{config_target}}"/>
                        </filterset>
                    </copy>
                    <wscompile
                        verbose="${{wscompile.client.{$wsclientname}.verbose}}"
                        debug="${{wscompile.client.{$wsclientname}.debug}}"
                        xPrintStackTrace="${{wscompile.client.{$wsclientname}.xPrintStackTrace}}"
                        xSerializable="${{wscompile.client.{$wsclientname}.xSerializable}}"
                        optimize="${{wscompile.client.{$wsclientname}.optimize}}"
                        fork="true" keep="true"
                        client="{$useclient}" import="{$useimport}"
                        features="${{wscompile.client.{$wsclientname}.features}}"
                        base="${{build.generated.sources.dir}}/jax-rpc"
                        sourceBase="${{build.generated.sources.dir}}/jax-rpc"
                        classpath="${{wscompile.classpath}}:${{javac.classpath}}"
                        mapping="${{build.generated.sources.dir}}/jax-rpc/wsdl/{$wsclientname}-mapping.xml"
                        httpproxy="${{wscompile.client.{$wsclientname}.proxy}}"
                        config="${{build.generated.sources.dir}}/jax-rpc/wsdl/{$wsclientname}-config.xml">
                    </wscompile>
                </target>
            </xsl:for-each>
            
            <xsl:if test="/p:project/p:configuration/carproject:data/carproject:web-service-clients/carproject:web-service-client">
                <target name="web-service-client-generate">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/p:project/p:configuration/carproject:data/carproject:web-service-clients/carproject:web-service-client">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:variable name="wsname2">
                                <xsl:value-of select="carproject:web-service-client-name"/>
                            </xsl:variable>
                            <xsl:value-of select="carproject:web-service-client-name"/><xsl:text>-client-wscompile</xsl:text>
                        </xsl:for-each>
                    </xsl:attribute>
                    <xsl:for-each select="/p:project/p:configuration/carproject:data/carproject:web-service-clients/carproject:web-service-client">
                        <xsl:variable name="wsclientname">
                            <xsl:value-of select="carproject:web-service-client-name"/>
                        </xsl:variable>
                        <copy file="${{build.generated.sources.dir}}/jax-rpc/wsdl/{$wsclientname}-mapping.xml"
                              tofile="${{classes.dir}}/META-INF/{$wsclientname}-mapping.xml"/>
                    </xsl:for-each>
                </target>
            </xsl:if>
            
            <target name="-pre-pre-compile">
                <xsl:attribute name="depends">init,deps-jar<xsl:if test="/p:project/p:configuration/carproject:data/carproject:web-service-clients/carproject:web-service-client">,web-service-client-generate</xsl:if></xsl:attribute>
                <mkdir dir="${{classes.dir}}"/>
            </target>
            
            <target name="-pre-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="library-inclusion-in-archive" depends="compile">
                <xsl:if test="count(//carproject:included-library) &gt; 0">
                    <mkdir dir="${{build.classes.dir}}/META-INF/lib"/>
                </xsl:if>
                <xsl:for-each select="//carproject:included-library">
                    <xsl:variable name="included.prop.name">
                        <xsl:value-of select="."/>
                    </xsl:variable>
                    <copyfiles todir="${{build.classes.dir}}/META-INF/lib">
                       <xsl:attribute name="files"><xsl:value-of select="concat('${',$included.prop.name,'}')"/></xsl:attribute>
                    </copyfiles>
                </xsl:for-each>
            </target>
            
            <target name="library-inclusion-in-manifest" depends="compile">
                <xsl:attribute name="if">dist.ear.dir</xsl:attribute>
                <!-- copy libraries into ear  -->
                <xsl:for-each select="//carproject:included-library">
                    <xsl:variable name="included.prop.name">
                        <xsl:value-of select="."/>
                    </xsl:variable>
                    <copyfiles todir="${{dist.ear.dir}}">
                       <xsl:attribute name="files"><xsl:value-of select="concat('${',$included.prop.name,'}')"/></xsl:attribute>
                       <xsl:attribute name="manifestproperty">
                           <xsl:value-of select="concat('manifest.', $included.prop.name)"/>
                       </xsl:attribute>
                    </copyfiles>
                </xsl:for-each>
                
                <manifest file="${{build.ear.classes.dir}}/META-INF/MANIFEST.MF" mode="update">
                    <xsl:if test="//carproject:included-library">
                        <attribute>
                            <xsl:attribute name="name">Class-Path</xsl:attribute>
                            <xsl:attribute name="value">
                                <!-- cp elements for included libraries and files -->
                                <xsl:for-each select="//carproject:included-library">
                                    <xsl:variable name="included.prop.name">
                                        <xsl:value-of select="."/>
                                    </xsl:variable>
                                    <xsl:value-of select="concat('${manifest.', $included.prop.name, '} ')"/>
                                </xsl:for-each>  
                            </xsl:attribute>
                        </attribute>
                    </xsl:if>
                </manifest>
                
            </target>
            
            <target name="-copy-meta-inf">
                <copy todir="${{classes.dir}}">
                    <fileset dir="${{meta.inf}}" includes="**/*.dbschema"/>
                </copy>
                <copy todir="${{classes.dir}}/META-INF">
                    <fileset dir="${{meta.inf}}" excludes="**/*.dbschema **/xml-resources/** ${{meta.inf.excludes}}"/>
                </copy>
                <xsl:if test="/p:project/p:configuration/carproject:data/carproject:web-services/carproject:web-service">
                    <xsl:comment>For web services, refresh application-client.xml and sun-application-client.xml</xsl:comment>  
                    <copy todir="${{classes.dir}}" overwrite="true"> 
                        <fileset includes="META-INF/application-client.xml META-INF/sun-application-client.xml" dir="${{meta.inf}}"/>
                    </copy>
                </xsl:if>
            </target>
            
            <target name="-do-compile">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile,-copy-meta-inf</xsl:attribute>
                <xsl:attribute name="if">have.sources</xsl:attribute>
                <carproject:javac destdir="${{classes.dir}}" gensrcdir="${{build.generated.sources.dir}}"/>
                <copy todir="${{classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:source-roots"/>
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                </copy>
            </target>
            
            <target name="-post-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="compile">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile,-do-compile,-post-compile</xsl:attribute>
                <xsl:attribute name="description">Compile project.</xsl:attribute>
            </target>
            
            <target name="-pre-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-compile-single">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile</xsl:attribute>
                <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
                <carproject:javac includes="${{javac.includes}}" excludes="" gensrcdir="${{build.generated.sources.dir}}"/>
            </target>
            
            <target name="-post-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="compile-single">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile-single,-do-compile-single,-post-compile-single</xsl:attribute>
            </target>
            
            <xsl:comment>
                ====================
                DIST BUILDING SECTION
                ====================
            </xsl:comment>
            
            <target name="-pre-pre-dist">
                <xsl:attribute name="depends">init</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
            </target>
            
            <target name="-pre-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-dist-with-manifest">
                <xsl:attribute name="depends">init,compile,-pre-dist,library-inclusion-in-archive</xsl:attribute>
                <xsl:attribute name="if">has.custom.manifest</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <manifest file="${{build.classes.dir}}/META-INF/MANIFEST.MF" mode="update">
                    <attribute name="Main-Class" value="${{main.class}}"/>
                </manifest>
                <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}" manifest="${{build.classes.dir}}/META-INF/MANIFEST.MF">
                    <fileset dir="${{build.classes.dir}}"/>
                </jar>
            </target>
            
            <target name="-do-dist-without-manifest">
                <xsl:attribute name="depends">init,compile,-pre-dist,library-inclusion-in-archive</xsl:attribute>
                <xsl:attribute name="unless">has.custom.manifest</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}">
                    <fileset dir="${{build.classes.dir}}"/>
                </jar>
            </target>
            
            <target name="-do-ear-dist-with-manifest">
                <xsl:attribute name="depends">init,compile,-pre-dist</xsl:attribute>
                <xsl:attribute name="if">has.custom.manifest</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.ear.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <manifest file="${{build.ear.classes.dir}}/META-INF/MANIFEST.MF" mode="update">
                    <attribute name="Main-Class" value="${{main.class}}"/>
                </manifest>
                <jar jarfile="${{dist.ear.jar}}" compress="${{jar.compress}}" manifest="${{build.ear.classes.dir}}/META-INF/MANIFEST.MF">
                    <fileset dir="${{build.ear.classes.dir}}"/>
                </jar>
            </target>
            
            <target name="-do-ear-dist-without-manifest">
                <xsl:attribute name="depends">init,compile,-pre-dist</xsl:attribute>
                <xsl:attribute name="unless">has.custom.manifest</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.ear.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.ear.jar}}" compress="${{jar.compress}}">
                    <fileset dir="${{build.ear.classes.dir}}"/>
                </jar>
            </target>
            
            <target name="-do-dist" depends="init,compile,-pre-dist,library-inclusion-in-archive,-do-dist-without-manifest,-do-dist-with-manifest"/>
            
            <target name="-do-ear-dist">
                <xsl:attribute name="depends">init,compile,-pre-dist,library-inclusion-in-manifest,-do-ear-dist-without-manifest,-do-ear-dist-with-manifest</xsl:attribute>
            </target>
            
            <target name="-post-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="dist">
                <xsl:attribute name="depends">init,compile,-pre-dist,-do-dist,-post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution (JAR).</xsl:attribute>
            </target>
            
            <target name="dist-ear">
                <xsl:attribute name="depends">init,compile,-pre-dist,-do-ear-dist,-post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution (JAR) to be packaged into an EAR.</xsl:attribute>
            </target>
            
            <xsl:comment>
                =================
                EXECUTION SECTION
                =================
            </xsl:comment>
            
            <target name="run">
                <xsl:attribute name="depends">dist,run-deploy,run-tool2,old-run-jar,run-display-browser</xsl:attribute>
                <xsl:attribute name="description">Run a main class.</xsl:attribute>
            </target>
            
            <target name="old-run-jar">
                <xsl:attribute name="depends">dist,run-deploy</xsl:attribute>
                <xsl:attribute name="description">Run a main class.</xsl:attribute>
                <xsl:attribute name="if">j2ee.appclient.tool.args</xsl:attribute>
                <antcall target="run-tool"/>
                <antcall target="run-jar"/>
            </target>

            <target name="run-jar">
                <xsl:attribute name="depends">init</xsl:attribute>
                <xsl:attribute name="description">Run a main class.</xsl:attribute>
                <xsl:attribute name="unless">j2ee.appclient.mainclass.args</xsl:attribute>
                <xsl:attribute name="if">j2ee.clientName</xsl:attribute>
                <carproject:java classname="${{main.class}}">
                    <customize>
                        <arg line="${{application.args.param}}"/>
                    </customize>
                </carproject:java>
            </target>
            
            <target name="run-tool">
                <xsl:attribute name="depends">init,-as-retrieve-option-workaround</xsl:attribute>
                <xsl:attribute name="description">Run a main class.</xsl:attribute>
                <xsl:attribute name="if">j2ee.appclient.mainclass.args</xsl:attribute>
                <xsl:attribute name="unless">j2ee.clientName</xsl:attribute>
                <java fork="true" classname="${{j2ee.appclient.tool.mainclass}}">
                    <xsl:attribute name="dir">${work.dir}</xsl:attribute>
                    <xsl:if test="/p:project/p:configuration/carproject:data/carproject:explicit-platform">
                        <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                    </xsl:if>
                    <jvmarg line="${{j2ee.appclient.tool.jvmoptions}}"/>
                    <jvmarg line="${{run.jvmargs.param}}"/>
                    <!--<arg line="${{j2ee.appclient.mainclass.args}}"/>-->
                    <arg line="${{j2ee.appclient.tool.args}}"/>
                    <arg line="-client ${{client.jar}}"/>
                    <arg line="${{application.args.param}}"/>
                    <classpath>
                        <path path="${{javac.classpath}}:${{j2ee.platform.classpath}}:${{j2ee.appclient.tool.runtime}}"/>
                    </classpath>
                    <syspropertyset>
                        <propertyref prefix="run-sys-prop."/>
                        <mapper type="glob" from="run-sys-prop.*" to="*"/>
                    </syspropertyset>
                </java>
            </target>
            
            <target name="run-tool2">
                <xsl:attribute name="depends">init,run-deploy,-as-retrieve-option-workaround</xsl:attribute>
                <xsl:attribute name="description">Run a main class.</xsl:attribute>
                <xsl:attribute name="unless">j2ee.appclient.tool.args</xsl:attribute>
                <mkdir dir="${{dist.dir}}/{$name}Client"/>
                <copy file="${{dist.jar}}" todir="${{dist.dir}}/{$name}Client"/>
                <java fork="true" jar="${{client.jar}}">
                    <xsl:attribute name="dir">${work.dir}</xsl:attribute>
                    <xsl:if test="/p:project/p:configuration/carproject:data/carproject:explicit-platform">
                        <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                    </xsl:if>
                    <jvmarg line="${{j2ee.appclient.tool.jvmoptions}}${{client.jar}}"/>
                    <jvmarg line="${{run.jvmargs.param}}"/>
                    <!--<arg line="${{j2ee.appclient.mainclass.args}}"/>-->
                    <arg line="${{application.args.param}}"/>
                    <syspropertyset>
                        <propertyref prefix="run-sys-prop."/>
                        <mapper type="glob" from="run-sys-prop.*" to="*"/>
                    </syspropertyset>
                </java>
            </target>

            <target name="run-single">
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <carproject:java classname="${{run.class}}"/>
            </target>
            
            <!--
               Idea is to add new non-mandatory option to nbdeploy task. This
               option should be a replacement for asadmin deploy -retrieve local_dir
               command. See also http://www.netbeans.org/issues/show_bug.cgi?id=82929.
            -->
            <target name="-as-retrieve-option-workaround">
                <xsl:attribute name="if">j2ee.appclient.mainclass.args</xsl:attribute>
                <xsl:attribute name="unless">j2ee.clientName</xsl:attribute>
                <property name="client.jar" value="${{dist.dir}}/{$name}Client.jar"/>
                <sleep seconds="3"/>
                <copy file="${{wa.copy.client.jar.from}}/{$name}/{$name}Client.jar" todir="${{dist.dir}}"/>                
            </target>
            
            <target name="pre-run-deploy">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="post-run-deploy">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-pre-nbmodule-run-deploy">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> This target can be overriden by NetBeans modules. Don't override it directly, use -pre-run-deploy task instead. </xsl:comment>
            </target>
            
            <target name="-post-nbmodule-run-deploy">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> This target can be overriden by NetBeans modules. Don't override it directly, use -post-run-deploy task instead. </xsl:comment>
            </target>
            
            <target name="-run-deploy-am">
                <xsl:comment> Task to deploy to the Access Manager runtime. </xsl:comment>
            </target>
            
            <target name="run-deploy">
                <xsl:attribute name="depends">init,compile,dist,pre-run-deploy,-pre-nbmodule-run-deploy,-run-deploy-nb,-init-deploy-ant,-deploy-ant,-run-deploy-am,-post-nbmodule-run-deploy,post-run-deploy</xsl:attribute>
                <nbjpdaappreloaded />
            </target>
            
            <target name="-run-deploy-nb" if="netbeans.home">
                <nbdeploy debugmode="false" clientUrlPart="${{client.urlPart}}" forceRedeploy="${{forceRedeploy}}"/>
            </target>
            
            <target name="-init-deploy-ant" unless="netbeans.home">
                <property name="deploy.ant.archive" value="${{dist.jar}}"/>
                <property name="deploy.ant.resource.dir" value="${{resource.dir}}"/>
                <property name="deploy.ant.enabled" value="true"/>
            </target>
            
            <target name="run-undeploy">
                <xsl:attribute name="depends">init,-run-undeploy-nb,-init-deploy-ant,-undeploy-ant</xsl:attribute>
            </target>
            
            <target name="-run-undeploy-nb" if="netbeans.home">
                <fail message="Undeploy is not supported from within the IDE"/>
            </target>
            
            <target name="run-display-browser">
                <xsl:attribute name="depends">run-deploy,-init-display-browser,-display-browser-nb,-display-browser-cl</xsl:attribute>
            </target>
            
            <target name="-init-display-browser" if="do.display.browser">
                <condition property="do.display.browser.nb">
                    <isset property="netbeans.home"/>
                </condition>
                <condition property="do.display.browser.cl">
                    <and>
                        <isset property="deploy.ant.enabled"/>
                        <isset property="deploy.ant.client.url"/>
                    </and>
                </condition>
            </target>
            
            <target name="-display-browser-nb" if="do.display.browser.nb">
                <nbbrowse url="${{client.url}}"/>
            </target>
            
            <target name="-get-browser" if="do.display.browser.cl" unless="browser">
                <condition property="browser" value="rundll32">
                    <os family="windows"/>
                </condition>
                <condition property="browser.args" value="url.dll,FileProtocolHandler" else="">
                    <os family="windows"/>
                </condition>
                <condition property="browser" value="/usr/bin/open">
                    <os family="mac"/>
                </condition>
                <property environment="env"/>
                <condition property="browser" value="${{env.BROWSER}}">
                    <isset property="env.BROWSER"/>
                </condition>
                <condition property="browser" value="/usr/bin/firefox">
                    <available file="/usr/bin/firefox"/>
                </condition>
                <condition property="browser" value="/usr/local/firefox/firefox">
                    <available file="/usr/local/firefox/firefox"/>
                </condition>
                <condition property="browser" value="/usr/bin/mozilla">
                    <available file="/usr/bin/mozilla"/>
                </condition>
                <condition property="browser" value="/usr/local/mozilla/mozilla">
                    <available file="/usr/local/mozilla/mozilla"/>
                </condition>
                <condition property="browser" value="/usr/sfw/lib/firefox/firefox">
                    <available file="/usr/sfw/lib/firefox/firefox"/>
                </condition>
                <condition property="browser" value="/opt/csw/bin/firefox">
                    <available file="/opt/csw/bin/firefox"/>
                </condition>
                <condition property="browser" value="/usr/sfw/lib/mozilla/mozilla">
                    <available file="/usr/sfw/lib/mozilla/mozilla"/>
                </condition>
                <condition property="browser" value="/opt/csw/bin/mozilla">
                    <available file="/opt/csw/bin/mozilla"/>
                </condition>
            </target>
            
            <target name="-display-browser-cl" depends="-get-browser" if="do.display.browser.cl">
                <fail unless="browser">
                    Browser not found, cannot launch the deployed application. Try to set the BROWSER environment variable.
                </fail>
                <property name="browse.url" value="${{deploy.ant.client.url}}${{client.urlPart}}"/>
                <echo>Launching ${browse.url}</echo>
                <exec executable="${{browser}}" spawn="true">
                    <arg line="${{browser.args}} ${{browse.url}}"/>
                </exec>
            </target>
            
            <target name="verify">
                <xsl:attribute name="depends">dist</xsl:attribute>
                <nbverify file="${{dist.jar}}"/>
            </target>
            
            <target name="run-main">
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <carproject:java classname="${{run.class}}"/>
            </target>
            
            
            
            
            <xsl:comment>
                =================
                DEBUGGING SECTION
                =================
            </xsl:comment>
            
            <target name="-debug-start-debugger">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <carproject:nbjpdastart name="${{debug.class}}"/>
            </target>
            
            <target name="-debug-start-debuggee">
                <xsl:attribute name="depends">init,compile</xsl:attribute>
                <carproject:debug>
                    <customize>
                        <arg line="${{application.args.param}}"/>
                    </customize>
                </carproject:debug>
            </target>
            
            <target name="debug-jar">
                <xsl:attribute name="unless">j2ee.appclient.mainclass.args</xsl:attribute>
                <xsl:attribute name="if">j2ee.clientName</xsl:attribute>
                <carproject:debug-appclient mainclass="${{main.class}}" args="${{application.args.param}}"/>
            </target>
            
            <target name="debug-tool">
                <xsl:attribute name="depends">init,-as-retrieve-option-workaround</xsl:attribute>
                <xsl:attribute name="if">j2ee.appclient.mainclass.args</xsl:attribute>
                <xsl:attribute name="unless">j2ee.clientName</xsl:attribute>
                <carproject:debug-appclient mainclass="${{j2ee.appclient.tool.mainclass}}" args="${{j2ee.appclient.mainclass.args}} ${{application.args.param}}"/>
            </target>
            
            <target name="debug">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,run-deploy,debug-tool,debug-jar</xsl:attribute>
                <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
            </target>
            
            <target name="-debug-start-debugger-stepinto">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <carproject:nbjpdastart stopclassname="${{main.class}}"/>
            </target>
            
            <target name="debug-stepinto">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-debug-start-debugger-stepinto,-debug-start-debuggee</xsl:attribute>
            </target>
            
            <target name="-debug-start-debuggee-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="debug.class">Must select one file in the IDE or set debug.class</fail>
                <carproject:debug classname="${{debug.class}}"/>
            </target>
            
            <target name="debug-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single,-debug-start-debugger,-debug-start-debuggee-single</xsl:attribute>
            </target>
            
            <target name="-pre-debug-fix">
                <xsl:attribute name="depends">init</xsl:attribute>
                <fail unless="fix.includes">Must set fix.includes</fail>
                <property name="javac.includes" value="${{fix.includes}}.java"/>
            </target>
            
            <target name="-do-debug-fix">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,compile-single</xsl:attribute>
                <carproject:nbjpdareload/>
            </target>
            
            <target name="debug-fix">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,-do-debug-fix</xsl:attribute>
            </target>
            
            <xsl:comment>
                ===============
                JAVADOC SECTION
                ===============
            </xsl:comment>
            
            <target name="-javadoc-build">
                <xsl:attribute name="depends">init</xsl:attribute>
                <mkdir dir="${{dist.javadoc.dir}}"/>
                <!-- XXX do an up-to-date check first -->
                <javadoc>
                    <xsl:attribute name="destdir">${dist.javadoc.dir}</xsl:attribute>
                    <xsl:attribute name="source">${javac.source}</xsl:attribute>
                    <xsl:attribute name="notree">${javadoc.notree}</xsl:attribute>
                    <xsl:attribute name="use">${javadoc.use}</xsl:attribute>
                    <xsl:attribute name="nonavbar">${javadoc.nonavbar}</xsl:attribute>
                    <xsl:attribute name="noindex">${javadoc.noindex}</xsl:attribute>
                    <xsl:attribute name="splitindex">${javadoc.splitindex}</xsl:attribute>
                    <xsl:attribute name="author">${javadoc.author}</xsl:attribute>
                    <xsl:attribute name="version">${javadoc.version}</xsl:attribute>
                    <xsl:attribute name="windowtitle">${javadoc.windowtitle}</xsl:attribute>
                    <xsl:attribute name="private">${javadoc.private}</xsl:attribute>
                    <xsl:attribute name="additionalparam">${javadoc.additionalparam}</xsl:attribute>
                    <xsl:attribute name="failonerror">true</xsl:attribute> <!-- #47325 -->
                    <xsl:attribute name="useexternalfile">true</xsl:attribute> <!-- #57375, requires Ant >=1.6.5 -->
                    <xsl:attribute name="encoding">${javadoc.encoding.used}</xsl:attribute>
                    <xsl:attribute name="docencoding">UTF-8</xsl:attribute>
                    <xsl:attribute name="charset">UTF-8</xsl:attribute>
                    <xsl:if test="/p:project/p:configuration/carproject:data/carproject:explicit-platform">
                        <xsl:attribute name="executable">${platform.javadoc}</xsl:attribute>
                    </xsl:if>                                                        
                    <classpath>
                        <path path="${{javac.classpath}}:${{j2ee.platform.classpath}}:${{j2ee.appclient.tool.runtime}}"/>
                    </classpath>
                    <!-- Does not work with includes/excludes:
                    <sourcepath>
                        <xsl:call-template name="createPathElements">
                            <xsl:with-param name="locations" select="/p:project/p:configuration/carproject:data/carproject:source-roots"/>
                        </xsl:call-template>
                    </sourcepath>
                    <xsl:call-template name="createPackagesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:source-roots"/>
                    </xsl:call-template>
                    -->
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:source-roots"/>
                        <xsl:with-param name="includes2">**/*.java</xsl:with-param>
                    </xsl:call-template>
                    <fileset>
                        <xsl:attribute name="dir">${build.generated.sources.dir}</xsl:attribute>
                        <xsl:attribute name="erroronmissingdir">false</xsl:attribute>
                        <include name="**/*.java"/>
                    </fileset>
                </javadoc>
            </target>
            
            <target name="-javadoc-browse">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="unless">no.javadoc.preview</xsl:attribute>
                <xsl:attribute name="depends">init,-javadoc-build</xsl:attribute>
                <nbbrowse file="${{dist.javadoc.dir}}/index.html"/>
            </target>
            
            <target name="javadoc">
                <xsl:attribute name="depends">init,-javadoc-build,-javadoc-browse</xsl:attribute>
                <xsl:attribute name="description">Build Javadoc.</xsl:attribute>
            </target>
            
            <xsl:comment>
                =========================
                JUNIT COMPILATION SECTION
                =========================
            </xsl:comment>
            
            <target name="-pre-pre-compile-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile</xsl:attribute>
                <mkdir dir="${{build.test.classes.dir}}"/>
            </target>
            
            <target name="-pre-compile-test">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-compile-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-pre-pre-compile-test,-pre-compile-test</xsl:attribute>
                <xsl:element name="carproject:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="debug">true</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}</xsl:attribute>
                </xsl:element>
                <copy todir="${{build.test.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:test-roots"/>
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                </copy>
            </target>
            
            <target name="-post-compile-test">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="compile-test">
                <xsl:attribute name="depends">init,compile,-pre-pre-compile-test,-pre-compile-test,-do-compile-test,-post-compile-test</xsl:attribute>
            </target>
            
            <target name="-pre-compile-test-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-compile-test-single">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-pre-pre-compile-test,-pre-compile-test-single</xsl:attribute>
                <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
                <xsl:element name="carproject:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="debug">true</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}</xsl:attribute>
                    <xsl:attribute name="includes">${javac.includes}</xsl:attribute>
                    <xsl:attribute name="excludes"/>
                </xsl:element>
                <copy todir="${{build.test.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/carproject:data/carproject:test-roots"/>
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                </copy>
            </target>
            
            <target name="-post-compile-test-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="compile-test-single">
                <xsl:attribute name="depends">init,compile,-pre-pre-compile-test,-pre-compile-test-single,-do-compile-test-single,-post-compile-test-single</xsl:attribute>
            </target>
            
            <xsl:comment>
                =======================
                JUNIT EXECUTION SECTION
                =======================
            </xsl:comment>
            
            <target name="-pre-test-run">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <mkdir dir="${{build.test.results.dir}}"/>
            </target>
            
            <target name="-do-test-run">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test,-pre-test-run</xsl:attribute>
                <carproject:junit testincludes="**/*Test.java"/>
            </target>
            
            <target name="-post-test-run">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test,-pre-test-run,-do-test-run</xsl:attribute>
                <fail if="tests.failed" unless="ignore.failing.tests">Some tests failed; see details above.</fail>
            </target>
            
            <target name="test-report">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <!-- TBD
                <junitreport todir="${{build.test.results.dir}}">
                <fileset dir="${{build.test.results.dir}}">
                <include name="TEST-*.xml"/>
                </fileset>
                <report format="noframes" todir="${{build.test.results.dir}}"/>
                </junitreport>
                -->
            </target>
            
            <target name="-test-browse">
                <xsl:attribute name="if">netbeans.home+have.tests</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <!-- TBD
                <nbbrowse file="${{build.test.results.dir}}/junit-noframes.html"/>
                -->
            </target>
            
            <target name="test">
                <xsl:attribute name="depends">init,compile-test,-pre-test-run,-do-test-run,test-report,-post-test-run,-test-browse</xsl:attribute>
                <xsl:attribute name="description">Run unit tests.</xsl:attribute>
            </target>
            
            <target name="-pre-test-run-single">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <mkdir dir="${{build.test.results.dir}}"/>
            </target>
            
            <target name="-do-test-run-single">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single</xsl:attribute>
                <fail unless="test.includes">Must select some files in the IDE or set test.includes</fail>
                <carproject:junit includes="${{test.includes}}" excludes=""/>
            </target>
            
            <target name="-post-test-run-single">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single,-do-test-run-single</xsl:attribute>
                <fail if="tests.failed" unless="ignore.failing.tests">Some tests failed; see details above.</fail>
            </target>
            
            <target name="test-single">
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single,-do-test-run-single,-post-test-run-single</xsl:attribute>
                <xsl:attribute name="description">Run single unit test.</xsl:attribute>
            </target>
            
            <xsl:comment>
                =======================
                JUNIT DEBUGGING SECTION
                =======================
            </xsl:comment>
            
            <target name="-debug-start-debuggee-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test</xsl:attribute>
                <fail unless="test.class">Must select one file in the IDE or set test.class</fail>
                <property name="test.report.file" location="${{build.test.results.dir}}/TEST-${{test.class}}.xml"/>
                <delete file="${{test.report.file}}"/>
                <xsl:comment> the directory must exist, otherwise the XML formatter would fail </xsl:comment>
                <mkdir dir="${{build.test.results.dir}}"/>
                <carproject:debug classname="org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner"
                                  classpath="${{ant.home}}/lib/ant.jar:${{ant.home}}/lib/ant-junit.jar:${{debug.test.classpath}}">
                    <customize>
                        <arg value="${{test.class}}"/>
                        <arg value="formatter=org.apache.tools.ant.taskdefs.optional.junit.BriefJUnitResultFormatter"/>
                        <arg value="formatter=org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter,${{test.report.file}}"/>
                    </customize>
                </carproject:debug>
            </target>
            
            <target name="-debug-start-debugger-test">
                <xsl:attribute name="if">netbeans.home+have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test</xsl:attribute>
                <carproject:nbjpdastart name="${{test.class}}" classpath="${{debug.test.classpath}}"/>
            </target>
            
            <target name="debug-test">
                <xsl:attribute name="depends">init,compile-test,-debug-start-debugger-test,-debug-start-debuggee-test</xsl:attribute>
            </target>
            
            <target name="-do-debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,compile-test-single</xsl:attribute>
                <carproject:nbjpdareload dir="${{build.test.classes.dir}}"/>
            </target>
            
            <target name="debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,-do-debug-fix-test</xsl:attribute>
            </target>
            
            
            <xsl:comment>
                =========================
                APPLET EXECUTION SECTION
                =========================
            </xsl:comment>
            
            <target name="run-applet">
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="applet.url">Must select one file in the IDE or set applet.url</fail>
                <carproject:java classname="sun.applet.AppletViewer">
                    <customize>
                        <arg value="${{applet.url}}"/>
                    </customize>
                </carproject:java>
            </target>
            
            <xsl:comment>
                =========================
                APPLET DEBUGGING  SECTION
                =========================
            </xsl:comment>
            
            <target name="-debug-start-debuggee-applet">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="applet.url">Must select one file in the IDE or set applet.url</fail>
                <carproject:debug classname="sun.applet.AppletViewer">
                    <customize>
                        <arg value="${{applet.url}}"/>
                    </customize>
                </carproject:debug>
            </target>
            
            <target name="debug-applet">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single,-debug-start-debugger,-debug-start-debuggee-applet</xsl:attribute>
            </target>
            
            <xsl:comment>
                ===============
                CLEANUP SECTION
                ===============
            </xsl:comment>
            
            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-clean'"/>
            </xsl:call-template>
            
            <target name="-do-clean">
                <xsl:attribute name="depends">init</xsl:attribute>
                <delete dir="${{build.dir}}"/>
                <delete dir="${{dist.dir}}"/>
                <!-- XXX explicitly delete all build.* and dist.* dirs in case they are not subdirs -->
            </target>
            
            <target name="-post-clean">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="clean">
                <xsl:attribute name="depends">init,deps-clean,-do-clean,-post-clean</xsl:attribute>
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
            
            <xsl:variable name="references2" select="/p:project/p:configuration/projdeps2:references"/>
            <xsl:for-each select="$references2/projdeps2:reference[not($type) or projdeps2:artifact-type = $type]">
                <xsl:variable name="subproj" select="projdeps2:foreign-project"/>
                <xsl:variable name="subtarget">
                    <xsl:choose>
                        <xsl:when test="$type">
                            <xsl:value-of select="projdeps2:target"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="projdeps2:clean-target"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="script" select="projdeps2:script"/>
                <!-- Distinguish build of a dependent project as standalone module or as a part of an ear -->
                <xsl:choose>
                    <xsl:when test="$ear">
                        <xsl:choose>
                            <!-- call standart target if the artifact type is jar (java libraries) -->
                            <xsl:when test="$subtarget = 'jar'">
                                <xsl:choose>
                                    <xsl:when test="projdeps2:properties">
                                        <ant target="{$subtarget}" inheritall="false" antfile="{$script}">
                                            <xsl:for-each select="projdeps2:properties/projdeps2:property">
                                                <property name="{@name}" value="{.}"/>
                                            </xsl:for-each>
                                        </ant>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <ant target="{$subtarget}" inheritall="false" antfile="{$script}"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:choose>
                                    <xsl:when test="projdeps2:properties">
                                        <ant target="dist-ear" inheritall="false" antfile="{$script}">
                                            <xsl:for-each select="projdeps2:properties/projdeps2:property">
                                                <property name="{@name}" value="{.}"/>
                                            </xsl:for-each>
                                            <property name="dist.ear.dir" location="${{build.dir}}"/>
                                        </ant>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <ant target="dist-ear" inheritall="false" antfile="{$script}">
                                            <property name="dist.ear.dir" location="${{build.dir}}"/>
                                        </ant>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="projdeps2:properties">
                                <ant target="{$subtarget}" inheritall="false" antfile="{$script}">
                                    <xsl:for-each select="projdeps2:properties/projdeps2:property">
                                        <property name="{@name}" value="{.}"/>
                                    </xsl:for-each>
                                </ant>
                            </xsl:when>
                            <xsl:otherwise>
                                <ant target="{$subtarget}" inheritall="false" antfile="{$script}"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            
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
                <xsl:for-each select="$roots/carproject:root">
                    <xsl:element name="available">
                        <xsl:attribute name="file"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
                    </xsl:element>
                </xsl:for-each>
            </or>
        </xsl:element>
    </xsl:template>
    
    <xsl:template name="createSourcePathValidityTest">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/carproject:root">
            <xsl:element name="fail">
                <xsl:attribute name="unless"><xsl:value-of select="@id"/></xsl:attribute>
                <xsl:text>Must set </xsl:text><xsl:value-of select="@id"/>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="createFilesets">
        <xsl:param name="roots"/>
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="includes2"/>
        <xsl:param name="excludes"/>
        <xsl:for-each select="$roots/carproject:root">
            <xsl:element name="fileset">
                <xsl:attribute name="dir"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
                <xsl:attribute name="includes"><xsl:value-of select="$includes"/></xsl:attribute>
                <xsl:choose>
                    <xsl:when test="$excludes">
                        <xsl:attribute name="excludes"><xsl:value-of select="$excludes"/>,${excludes}</xsl:attribute>
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
    
    <xsl:template name="createPackagesets">
        <xsl:param name="roots"/>
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="excludes"/>
        <xsl:for-each select="$roots/carproject:root">
            <xsl:element name="packageset">
                <xsl:attribute name="dir"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
                <xsl:attribute name="includes"><xsl:value-of select="$includes"/></xsl:attribute>
                <xsl:choose>
                    <xsl:when test="$excludes">
                        <xsl:attribute name="excludes"><xsl:value-of select="$excludes"/>,${excludes}</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="excludes">${excludes}</xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>        
    <xsl:template name="createPathElements">
        <xsl:param name="locations"/>
        <xsl:for-each select="$locations/carproject:root">
            <xsl:element name="pathelement">
                <xsl:attribute name="location"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="createPath">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/carproject:root">
            <xsl:if test="position() != 1">
                <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:text>${</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>}</xsl:text>
        </xsl:for-each>						
    </xsl:template>
    
</xsl:stylesheet>
