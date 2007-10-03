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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:ejbjarproject1="http://www.netbeans.org/ns/j2ee-ejbjarproject/1"
                xmlns:ejbjarproject2="http://www.netbeans.org/ns/j2ee-ejbjarproject/2"
                xmlns:ejbjarproject3="http://www.netbeans.org/ns/j2ee-ejbjarproject/3"
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
  - cleanup

        ]]></xsl:comment>
        
        <xsl:variable name="name" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:name"/>
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-impl">
            <xsl:attribute name="default">build</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>
            <import file="ant-deploy.xml" />
            <target name="default">
                <xsl:attribute name="depends">dist,javadoc</xsl:attribute>
                <xsl:attribute name="description">Build whole project.</xsl:attribute>
            </target>
            
            <xsl:comment> 
                INITIALIZATION SECTION 
            </xsl:comment>
            
            <target name="-pre-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-init-private">
                <xsl:attribute name="depends">-pre-init</xsl:attribute>
                <property file="nbproject/private/private.properties"/>
            </target>
            
            <target name="-init-userdir">
                <xsl:attribute name="depends">-pre-init,-init-private</xsl:attribute>
                <property name="user.properties.file" location="${{netbeans.user}}/build.properties"/>
            </target>
            
            <target name="-init-user">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-userdir</xsl:attribute>
                <property file="${{user.properties.file}}"/>
                <xsl:comment> The two properties below are usually overridden </xsl:comment>
                <xsl:comment> by the active platform. Just a fallback. </xsl:comment>
                <property name="default.javac.source" value="1.4"/>
                <property name="default.javac.target" value="1.4"/>
            </target>
            
            <target name="-init-project">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-userdir,-init-user</xsl:attribute>
                <property file="nbproject/project.properties"/>
            </target>
            
            <target name="-do-init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-userdir,-init-user,-init-project,-init-macrodef-property</xsl:attribute>
                <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:explicit-platform">
                    <ejbjarproject1:property name="platform.home" value="platforms.${{platform.active}}.home"/>
                    <ejbjarproject1:property name="platform.bootcp" value="platforms.${{platform.active}}.bootclasspath"/>
                    <ejbjarproject1:property name="platform.compiler" value="platforms.${{platform.active}}.compile"/>
                    <ejbjarproject1:property name="platform.javac.tmp" value="platforms.${{platform.active}}.javac"/>
                    <condition property="platform.javac" value="${{platform.home}}/bin/javac">
                        <equals arg1="${{platform.javac.tmp}}" arg2="$${{platforms.${{platform.active}}.javac}}"/>
                    </condition>
                    <property name="platform.javac" value="${{platform.javac.tmp}}"/>
                    <ejbjarproject1:property name="platform.java.tmp" value="platforms.${{platform.active}}.java"/>
                    <condition property="platform.java" value="${{platform.home}}/bin/java">
                        <equals arg1="${{platform.java.tmp}}" arg2="$${{platforms.${{platform.active}}.java}}"/>
                    </condition>
                    <property name="platform.java" value="${{platform.java.tmp}}"/>
                    <ejbjarproject1:property name="platform.javadoc.tmp" value="platforms.${{platform.active}}.javadoc"/>
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
                    <fail if="platform.invalid">Platform is not correctly set up</fail>
                </xsl:if>
                <xsl:comment> Ensure configuration directory exists. </xsl:comment>
                <mkdir dir="${{meta.inf}}"/>
                <property name="runmain.jvmargs" value=""/>
                <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:use-manifest">
                    <fail unless="manifest.file">Must set manifest.file</fail>
                </xsl:if>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:test-roots"/>
                    <xsl:with-param name="propName">have.tests</xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:source-roots"/>
                    <xsl:with-param name="propName">have.sources</xsl:with-param>
                </xsl:call-template>
                <condition property="netbeans.home+have.tests">
                    <and>
                        <isset property="netbeans.home"/>
                        <isset property="have.tests"/>
                    </and>
                </condition>
                <condition property="no.javadoc.preview">
                    <isfalse value="${{javadoc.preview}}"/>
                </condition>
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
            </target>
            
            <target name="-post-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-init-check">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-userdir,-init-user,-init-project,-do-init</xsl:attribute>
                <!-- XXX XSLT 2.0 would make it possible to use a for-each here -->
                <!-- Note that if the properties were defined in project.xml that would be easy -->
                <!-- But required props should be defined by the AntBasedProjectType, not stored in each project -->
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:source-roots"/>
                </xsl:call-template>
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:test-roots"/>
                </xsl:call-template>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="build.generated.dir">Must set build.generated.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.dir">Must set build.classes.dir</fail>
                <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail>
                <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>
                <fail unless="dist.jar">Must set dist.jar</fail>
            </target>
            
            <target name="-init-macrodef-property">
                <macrodef>
                    <xsl:attribute name="name">property</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-ejbjarproject/1</xsl:attribute>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-ejbjarproject/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:source-roots"/>
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
                        <xsl:attribute name="name">debug</xsl:attribute>
                        <xsl:attribute name="default">${javac.debug}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">javac.compilerargs.jaxws</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property name="javac.compilerargs" value=""/>
                        <javac>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                            <xsl:if test="not(/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:explicit-platform/@explicit-source-supported = 'false')">
                                <xsl:attribute name="source">${javac.source}</xsl:attribute>
                                <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            </xsl:if>
                            <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:explicit-platform">
                                <xsl:attribute name="fork">yes</xsl:attribute>
                                <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                            </xsl:if>
                            <xsl:attribute name="includeantruntime">false</xsl:attribute>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <compilerarg line="${{javac.compilerargs}} @{{javac.compilerargs.jaxws}}"/>
                            <customize/>
                        </javac>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-junit">
                <macrodef>
                    <xsl:attribute name="name">junit</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-ejbjarproject/2</xsl:attribute>
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
                            <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <batchtest todir="${{build.test.results.dir}}">
                                <xsl:call-template name="createFilesets">
                                    <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:test-roots"/>
                                    <xsl:with-param name="includes">@{includes}</xsl:with-param>
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
                        </junit>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-java">
                <macrodef>
                    <xsl:attribute name="name">java</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-ejbjarproject/3</xsl:attribute>
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
                            <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <jvmarg line="${{runmain.jvmargs}}"/>
                            <classpath>
                                <path path="${{build.classes.dir}}:${{javac.classpath}}:${{j2ee.platform.classpath}}"/>
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
            
            <target name="-init-macrodef-nbjpda">
                <macrodef>
                    <xsl:attribute name="name">nbjpdastart</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-ejbjarproject/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">name</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <nbjpdastart transport="dt_socket" addressproperty="jpda.address" name="@{{name}}">
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:explicit-platform">
                                <bootclasspath>
                                    <path path="${{platform.bootcp}}"/>
                                </bootclasspath>
                            </xsl:if>
                        </nbjpdastart>
                    </sequential>
                </macrodef>
                <macrodef>
                    <xsl:attribute name="name">nbjpdareload</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-ejbjarproject/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">dir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <nbjpdareload>
                            <fileset includes="${{fix.includes}}*.class" dir="@{{dir}}"/>
                        </nbjpdareload>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-debug-args">
                <xsl:choose>
                    <xsl:when test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:explicit-platform">
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
            </target>

            <target name="-init-macrodef-debug" depends="-init-debug-args">
                <macrodef>
                    <xsl:attribute name="name">debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-ejbjarproject/1</xsl:attribute>
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
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <java fork="true" classname="@{{classname}}">
                            <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                                <bootclasspath>
                                    <path path="${{platform.bootcp}}"/>
                                </bootclasspath>
                            </xsl:if>
                            <jvmarg line="${{debug-args-line}}"/>
                            <jvmarg value="-Xrunjdwp:transport=dt_socket,address=${{jpda.address}}"/>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <arg line="@{{args}}"/>
                            <customize/>
                        </java>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-userdir,-init-user,-init-project,-do-init,-post-init,-init-check,-init-macrodef-property,-init-macrodef-javac,-init-macrodef-junit,-init-macrodef-java,-init-macrodef-nbjpda,-init-macrodef-debug</xsl:attribute>
            </target>
            
            <xsl:comment>
                COMPILATION SECTION
            </xsl:comment>
            
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
                <xsl:attribute name="depends">init, -deps-module-jar, -deps-ear-jar</xsl:attribute>
            </target>
            
            <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-services/ejbjarproject3:web-service|/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-service-clients/ejbjarproject3:web-service-client">
                <target name="wscompile-init" depends="init">
                    <taskdef name="wscompile" classname="com.sun.xml.rpc.tools.ant.Wscompile">
                        <classpath path="${{wscompile.classpath}}"/>
                    </taskdef>
                    <mkdir dir="${{classes.dir}}/META-INF/wsdl"/>
                    <mkdir dir="${{build.generated.dir}}/wsclient"/>
                    <mkdir dir="${{build.generated.dir}}/wsservice"/>
                    <mkdir dir="${{build.generated.dir}}/wsbinary"/>
                    <mkdir dir="${{meta.inf}}/wsdl"/>
                </target>
            </xsl:if>
            
            <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-services/ejbjarproject3:web-service">
                <target name="fromwsdl-noop"/>
            </xsl:if>
            
            <xsl:for-each select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-services/ejbjarproject3:web-service">
                <xsl:variable name="wsname">
                    <xsl:value-of select="ejbjarproject3:web-service-name"/>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="ejbjarproject3:from-wsdl">
                        <target name="{$wsname}_wscompile" depends="init, wscompile-init">
                            <wscompile import="true" 
                                       config="${{{$wsname}.config.name}}"
                                       features="${{wscompile.service.{$wsname}.features}}"
                                       mapping="${{meta.inf}}/${{{$wsname}.mapping}}"
                                       classpath="${{wscompile.classpath}}:${{javac.classpath}}" 
                                       nonClassDir="${{classes.dir}}/META-INF/wsdl" 
                                       verbose="true" 
                                       xPrintStackTrace="true" 
                                       xSerializable="true"
                                       base="${{build.generated.dir}}/wsbinary"
                                       sourceBase="${{src.dir}}" 
                                       keep="true" 
                                       fork="true" />
                        </target>  
                    </xsl:when>
                    <xsl:otherwise>
                        <target name="{$wsname}_wscompile" depends="wscompile-init">
                            <wscompile
                                define="true"
                                fork="true"
                                keep="true"
                                base="${{build.generated.dir}}/wsbinary"
                                xPrintStackTrace="true"
                                verbose="true"
                                nonClassDir="${{classes.dir}}/META-INF/wsdl"
                                classpath="${{wscompile.classpath}}:${{classes.dir}}:${{javac.classpath}}"
                                mapping="${{classes.dir}}/META-INF/${{{$wsname}.mapping}}"
                                config="${{{$wsname}.config.name}}"
                                features="${{wscompile.service.{$wsname}.features}}"
                                sourceBase="${{build.generated.dir}}/wsservice">
                            </wscompile>
                        </target>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            
            <xsl:for-each select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-service-clients/ejbjarproject3:web-service-client">
                <xsl:variable name="wsclientname">
                    <xsl:value-of select="ejbjarproject3:web-service-client-name"/>
                </xsl:variable>
                <xsl:variable name="useimport">
                    <xsl:choose>
                        <xsl:when test="ejbjarproject3:web-service-stub-type">
                            <xsl:value-of select="ejbjarproject3:web-service-stub-type='jsr-109_client'"/>
                        </xsl:when>
                        <xsl:otherwise>true</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="useclient">
                    <xsl:choose>
                        <xsl:when test="ejbjarproject3:web-service-stub-type">
                            <xsl:value-of select="ejbjarproject3:web-service-stub-type='jaxrpc_static_client'"/>
                        </xsl:when>
                        <xsl:otherwise>false</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                
                <target name="{$wsclientname}-client-wscompile" depends="wscompile-init">
                    <property name="config_target" location="${{meta.inf}}/wsdl"/>
                    <copy file="${{meta.inf}}/wsdl/{$wsclientname}-config.xml"
                          tofile="${{build.generated.dir}}/wsclient/wsdl/{$wsclientname}-config.xml" filtering="on">
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
                        base="${{classes.dir}}"
                        sourceBase="${{build.generated.dir}}/wsclient"
                        classpath="${{wscompile.classpath}}:${{javac.classpath}}"
                        mapping="${{classes.dir}}/META-INF/{$wsclientname}-mapping.xml"
                        config="${{build.generated.dir}}/wsclient/wsdl/{$wsclientname}-config.xml">
                    </wscompile>
                </target>
            </xsl:for-each>
            
            <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-service-clients/ejbjarproject3:web-service-client">
                <target name="web-service-client-generate">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-service-clients/ejbjarproject3:web-service-client">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:variable name="wsname2">
                                <xsl:value-of select="ejbjarproject3:web-service-client-name"/>
                            </xsl:variable>
                            <xsl:value-of select="ejbjarproject3:web-service-client-name"/><xsl:text>-client-wscompile</xsl:text>
                        </xsl:for-each>
                    </xsl:attribute>
                </target>
                <target name="web-service-client-compile" depends="web-service-client-generate">
                    <ejbjarproject2:javac srcdir="${{build.generated.dir}}/wsclient" classpath="${{wscompile.classpath}}:${{javac.classpath}}" destdir="${{classes.dir}}"/>
                </target>
            </xsl:if>
            
            <target name="-pre-pre-compile">
                <xsl:attribute name="depends">init,deps-jar<xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-service-clients/ejbjarproject3:web-service-client">,web-service-client-generate</xsl:if></xsl:attribute>
                <mkdir dir="${{build.classes.dir}}"/>
                <mkdir dir="${{build.ear.classes.dir}}"/>
            </target>
            
            <target name="-pre-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="library-inclusion-in-archive" depends="compile">
                <xsl:for-each select="//ejbjarproject3:included-library">
                    <xsl:variable name="included.prop.name">
                        <xsl:value-of select="."/>
                    </xsl:variable>
                    <xsl:if test="//ejbjarproject3:included-library[@files]">
                        <xsl:if test="(@files &gt; 1) or (@files &gt; 0 and (@dirs &gt; 0))">
                            <xsl:call-template name="copyIterateFiles">
                                <xsl:with-param name="files" select="@files"/>
                                <xsl:with-param name="target" select="'${build.classes.dir}'"/>
                                <xsl:with-param name="libfile" select="$included.prop.name"/>
                            </xsl:call-template>
                        </xsl:if>
                        <xsl:if test="@files = 1 and (@dirs = 0 or not(@dirs))">
                            <xsl:variable name="target" select="'${build.classes.dir}'"/>
                            <xsl:variable name="libfile" select="concat('${',$included.prop.name,'}')"/>
                            <copy file="{$libfile}" todir="{$target}"/>
                        </xsl:if>
                        
                    </xsl:if>
                    <xsl:if test="//ejbjarproject3:included-library[@dirs]">
                        <xsl:if test="(@dirs &gt; 1) or (@files &gt; 0 and (@dirs &gt; 0))">
                            <xsl:call-template name="copyIterateDirs">
                                <xsl:with-param name="files" select="@dirs"/>
                                <xsl:with-param name="target" select="'${build.classes.dir}'"/>
                                <xsl:with-param name="libfile" select="$included.prop.name"/>
                            </xsl:call-template>
                        </xsl:if>
                        <xsl:if test="@dirs = 1 and (@files = 0 or not(@files))">
                            <xsl:variable name="target" select="'${build.classes.dir}'"/>
                            <xsl:variable name="libfile" select="concat('${',$included.prop.name,'}')"/>
                            <copy todir="{$target}">
                                <fileset dir="{$libfile}" includes="**/*"/>
                            </copy>
                        </xsl:if>
                    </xsl:if>
                </xsl:for-each>   
            </target> 
            
            <target name="library-inclusion-in-manifest" depends="compile">
                <xsl:for-each select="//ejbjarproject3:included-library">
                    <xsl:variable name="included.prop.name">
                        <xsl:value-of select="."/>
                    </xsl:variable>
                    <xsl:variable name="base.prop.name">
                        <xsl:value-of select="concat('included.lib.', $included.prop.name, '')"/>
                    </xsl:variable>
                    <xsl:if test="//ejbjarproject3:included-library[@files]">
                        <xsl:if test="(@files &gt; 1) or (@files &gt; 0 and (@dirs &gt; 0))">
                            <xsl:call-template name="manifestBasenameIterateFiles">
                                <xsl:with-param name="property" select="$base.prop.name"/>
                                <xsl:with-param name="files" select="@files"/>
                                <xsl:with-param name="libfile" select="$included.prop.name"/>
                            </xsl:call-template>
                        </xsl:if>
                        <xsl:if test="@files = 1 and (@dirs = 0 or not(@dirs))">
                            <xsl:variable name="libfile" select="concat('${',$included.prop.name,'}')"/>
                            <basename property="{$base.prop.name}" file="{$libfile}"/>
                        </xsl:if>
                    </xsl:if>
                    <xsl:if test="//ejbjarproject3:included-library[@files]">
                        <xsl:if test="(@files &gt; 1) or (@files &gt; 0 and (@dirs &gt; 0))">
                            <xsl:call-template name="copyIterateFiles">
                                <xsl:with-param name="files" select="@files"/>
                                <xsl:with-param name="target" select="'${dist.ear.dir}'"/>
                                <xsl:with-param name="libfile" select="$included.prop.name"/>
                            </xsl:call-template>
                        </xsl:if>
                        <xsl:if test="@files = 1 and (@dirs = 0 or not(@dirs))">
                            <xsl:variable name="target" select="'${dist.ear.dir}'"/>
                            <xsl:variable name="libfile" select="concat('${',$included.prop.name,'}')"/>
                            <copy file="{$libfile}" todir="{$target}"/>
                        </xsl:if>
                    </xsl:if>
                    <xsl:if test="//ejbjarproject3:included-library[@dirs]">
                        <xsl:if test="(@files &gt; 1) or (@files &gt; 0 and (@dirs &gt; 0))">
                            <xsl:call-template name="copyIterateDirs">
                                <xsl:with-param name="files" select="@dirs"/>
                                <xsl:with-param name="target" select="'${dist.ear.dir}'"/>
                                <xsl:with-param name="libfile" select="$included.prop.name"/>
                            </xsl:call-template>
                        </xsl:if>
                        <xsl:if test="@dirs = 1 and (@files = 0 or not(@files))">
                            <xsl:variable name="target" select="'${dist.ear.dir}'"/>
                            <xsl:variable name="libfile" select="concat('${',$included.prop.name,'}')"/>
                            <copy todir="{$target}">
                                <fileset dir="{$libfile}" includes="**/*"/>
                            </copy>
                        </xsl:if>
                    </xsl:if>
                </xsl:for-each>   
                
                <manifest file="${{build.ear.classes.dir}}/META-INF/MANIFEST.MF" mode="update">
                    <xsl:if test="//ejbjarproject3:included-library">
                        <attribute>
                            <xsl:attribute name="name">Class-Path</xsl:attribute>
                            <xsl:attribute name="value">
                                <!-- classpath element for directories -->
                                <xsl:if test="//ejbjarproject3:included-library[(@dirs &gt; 0)]">
                                    <xsl:text>. </xsl:text>
                                </xsl:if>
                                <!-- cp elements for included libraries and files -->
                                <xsl:for-each select="//ejbjarproject3:included-library">
                                    <xsl:variable name="base.prop.name">
                                        <xsl:value-of select="concat('included.lib.', .)"/>
                                    </xsl:variable>
                                    <xsl:variable name="included.prop.name">
                                        <xsl:value-of select="."/>
                                    </xsl:variable>
                                    <xsl:if test="(@files &gt; 1) or (@files &gt; 0 and (@dirs &gt; 0))">
                                        <xsl:call-template name="manifestPrintEntriesIterateFiles">
                                            <xsl:with-param name="property" select="$base.prop.name"/>
                                            <xsl:with-param name="files" select="@files"/>
                                            <xsl:with-param name="libfile" select="$included.prop.name"/>
                                        </xsl:call-template>
                                    </xsl:if>
                                    <xsl:if test="@files = 1 and (@dirs = 0 or not(@dirs))">
                                        <xsl:text>${</xsl:text><xsl:value-of select="$base.prop.name"/><xsl:text>} </xsl:text>
                                    </xsl:if>
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
                <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-services/ejbjarproject3:web-service">
                    <xsl:comment>For web services, refresh ejb-jar.xml and sun-ejb-jar.xml</xsl:comment>  
                    <copy todir="${{classes.dir}}" overwrite="true"> 
                        <fileset includes="META-INF/ejb-jar.xml META-INF/sun-ejb-jar.xml" dir="${{meta.inf}}"/>
                    </copy>
                </xsl:if>
            </target>
            
            <target name="-do-compile">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile,-copy-meta-inf<xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-service-clients/ejbjarproject3:web-service-client">,web-service-client-compile</xsl:if></xsl:attribute>
                <xsl:attribute name="if">have.sources</xsl:attribute>
                <ejbjarproject2:javac destdir="${{classes.dir}}"/>
                <copy todir="${{classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:source-roots"/>
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                </copy>
            </target>
            
            <target name="-post-compile">
                <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-services/ejbjarproject3:web-service">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-services/ejbjarproject3:web-service">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text>
                            </xsl:if>
                            <xsl:choose>
                                <xsl:when test="not(ejbjarproject3:from-wsdl)">
                                    <xsl:value-of select="ejbjarproject3:web-service-name"/><xsl:text>_wscompile</xsl:text>
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
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile,-do-compile,-post-compile</xsl:attribute>
                <xsl:attribute name="description">Compile project.</xsl:attribute>
            </target>
            
            <target name="-pre-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-compile-single">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile<xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:web-service-clients/ejbjarproject3:web-service-client">,web-service-client-compile</xsl:if></xsl:attribute>
                <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
                <ejbjarproject2:javac>
                    <customize>
                        <include name="${{javac.includes}}"/>
                    </customize>
                </ejbjarproject2:javac>
            </target>
            
            <target name="-post-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="compile-single">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile-single,-do-compile-single,-post-compile-single</xsl:attribute>
            </target>
            
            <xsl:comment>
                DIST BUILDING SECTION
            </xsl:comment>
            
            <target name="-pre-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-dist-with-manifest">
                <xsl:attribute name="depends">init,compile,-pre-dist,library-inclusion-in-archive</xsl:attribute>
                <xsl:attribute name="if">has.custom.manifest</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
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
            
            <target name="-do-dist" depends="init,compile,-pre-dist,library-inclusion-in-archive, -do-dist-without-manifest, -do-dist-with-manifest"/>
            
            <target name="-do-ear-dist">
                <xsl:attribute name="depends">init,compile,-pre-dist,library-inclusion-in-manifest</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.ear.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.ear.jar}}" compress="${{jar.compress}}" manifest="${{build.ear.classes.dir}}/META-INF/MANIFEST.MF">
                    <fileset dir="${{build.ear.classes.dir}}"/>
                </jar>
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
                EXECUTION SECTION
            </xsl:comment>
            <target name="run">
                <xsl:attribute name="depends">run-deploy</xsl:attribute>
                <xsl:attribute name="description">Deploy to server.</xsl:attribute>
            </target>
            
            <target name="-init-deploy">
                <property name="include.jar.manifest" value=""/>
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
                <xsl:attribute name="depends">init,-init-deploy,compile,library-inclusion-in-archive,dist,pre-run-deploy,-pre-nbmodule-run-deploy,-run-deploy-nb,-init-deploy-ant,-deploy-ant,-run-deploy-am,-post-nbmodule-run-deploy,post-run-deploy</xsl:attribute>
            </target>
            
            <target name="-run-deploy-nb" if="netbeans.home">
                <nbdeploy debugmode="false" forceRedeploy="${{forceRedeploy}}"/>
            </target>
            
            <target name="-init-deploy-ant" unless="netbeans.home">
                <property name="deploy.ant.archive" value="${{dist.jar}}"/>
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
                <xsl:attribute name="depends">dist</xsl:attribute>
                <nbverify file="${{dist.jar}}"/>
            </target>
            
            <target name="run-main">
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <ejbjarproject3:java classname="${{run.class}}"/>
            </target>
            
            <xsl:comment>
                DEBUGGING SECTION
            </xsl:comment>
            <target name="debug">
                <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
                <xsl:attribute name ="depends">init,compile,dist</xsl:attribute>
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <nbdeploy debugmode="true"/>
                <antcall target="connect-debugger"/>
            </target>
            
            <target name="connect-debugger" unless="is.debugged">
                <nbjpdaconnect name="${{name}}" host="${{jpda.host}}" address="${{jpda.address}}" transport="${{jpda.transport}}">
                    <classpath>
                        <path path="${{debug.classpath}}"/>
                    </classpath>
                    <sourcepath>
                        <path path="${{web.docbase.dir}}"/>
                    </sourcepath>
                    <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:explicit-platform">
                        <bootclasspath>
                            <path path="${{platform.bootcp}}"/>
                        </bootclasspath>
                    </xsl:if>
                </nbjpdaconnect>
            </target>
            
            <target name="-debug-start-debugger">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <ejbjarproject1:nbjpdastart />
            </target>
            
            <target name="-debug-start-debuggee-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="main.class">Must select one file in the IDE or set main.class</fail>
                <ejbjarproject1:debug />
            </target>
            
            <target name="debug-single-main">
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
                <ejbjarproject1:nbjpdareload/>
            </target>
            
            <target name="debug-fix">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,-do-debug-fix</xsl:attribute>
            </target>
            
            <xsl:comment>
                JAVADOC SECTION
            </xsl:comment>
            
            <target name="javadoc-build">
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
                    <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:explicit-platform">
                        <xsl:attribute name="executable">${platform.javadoc}</xsl:attribute>
                    </xsl:if>
                    <classpath>
                        <path path="${{javac.classpath}}:${{j2ee.platform.classpath}}"/>
                    </classpath>
                    <sourcepath>
                        <xsl:call-template name="createPathElements">
                            <xsl:with-param name="locations" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:source-roots"/>
                        </xsl:call-template>
                    </sourcepath>
                    <xsl:call-template name="createPackagesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:source-roots"/>
                        <xsl:with-param name="includes">*/**</xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:source-roots"/>
                        <xsl:with-param name="includes">*.java</xsl:with-param>
                    </xsl:call-template>
                </javadoc>
            </target>
            
            <target name="javadoc-browse">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="unless">no.javadoc.preview</xsl:attribute>
                <xsl:attribute name="depends">init,javadoc-build</xsl:attribute>
                <nbbrowse file="${{dist.javadoc.dir}}/index.html"/>
            </target>
            
            <target name="javadoc">
                <xsl:attribute name="depends">init,javadoc-build,javadoc-browse</xsl:attribute>
                <xsl:attribute name="description">Build Javadoc.</xsl:attribute>
            </target>
            
            <xsl:comment>
                JUNIT COMPILATION SECTION
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
                <xsl:element name="ejbjarproject2:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="debug">true</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}:${j2ee.platform.classpath}</xsl:attribute>
                </xsl:element>
                <copy todir="${{build.test.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:test-roots"/>
                        <xsl:with-param name="excludes">**/*.java</xsl:with-param>
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
                <xsl:element name="ejbjarproject2:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="debug">true</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}:${j2ee.platform.classpath}</xsl:attribute>
                    <customize>
                        <patternset includes="${{javac.includes}}"/>
                    </customize>
                </xsl:element>
            </target>
            
            <target name="-post-compile-test-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="compile-test-single">
                <xsl:attribute name="depends">init,compile,-pre-pre-compile-test,-pre-compile-test-single,-do-compile-test-single,-post-compile-test-single</xsl:attribute>
            </target>
            
            <xsl:comment>
                JUNIT EXECUTION SECTION
            </xsl:comment>
            
            <target name="-pre-test-run">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <mkdir dir="${{build.test.results.dir}}"/>
            </target>
            
            <target name="-do-test-run">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test,-pre-test-run</xsl:attribute>
                <ejbjarproject2:junit/>
            </target>
            
            <target name="-post-test-run">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test,-pre-test-run,-do-test-run</xsl:attribute>
                <fail if="tests.failed">Some tests failed; see details above.</fail>
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
                <ejbjarproject2:junit includes="${{test.includes}}"/>
            </target>
            
            <target name="-post-test-run-single">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single,-do-test-run-single</xsl:attribute>
                <fail if="tests.failed">Some tests failed; see details above.</fail>
            </target>
            
            <target name="test-single">
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single,-do-test-run-single,-post-test-run-single</xsl:attribute>
                <xsl:attribute name="description">Run single unit test.</xsl:attribute>
            </target>
            
            <xsl:comment>
                JUNIT DEBUGGING SECTION
            </xsl:comment>
            
            <target name="-debug-start-debuggee-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test</xsl:attribute>
                <fail unless="test.class">Must select one file in the IDE or set test.class</fail>
                <property name="test.report.file" location="${{build.test.results.dir}}/TEST-${{test.class}}.xml"/>
                <delete file="${{test.report.file}}"/>
                <xsl:comment> the directory must exist, otherwise the XML formatter would fail </xsl:comment>
                <mkdir dir="${{build.test.results.dir}}"/>
                <ejbjarproject1:debug classname="org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner"
                                      classpath="${{ant.home}}/lib/ant.jar:${{ant.home}}/lib/ant-junit.jar:${{debug.test.classpath}}"
                                      args="${{test.class}}">
                    <customize>
                        <arg value="showoutput=true"/>
                        <arg value="formatter=org.apache.tools.ant.taskdefs.optional.junit.BriefJUnitResultFormatter"/>
                        <arg value="formatter=org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter,${{test.report.file}}"/>
                    </customize>
                </ejbjarproject1:debug>
            </target>
            
            <target name="-debug-start-debugger-test">
                <xsl:attribute name="if">netbeans.home+have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test</xsl:attribute>
                <ejbjarproject1:nbjpdastart name="${{test.class}}" classpath="${{debug.test.classpath}}"/>
            </target>
            
            <target name="debug-test">
                <xsl:attribute name="depends">init,compile-test,-debug-start-debugger-test,-debug-start-debuggee-test</xsl:attribute>
            </target>
            
            <target name="-do-debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,compile-test-single</xsl:attribute>
                <ejbjarproject1:nbjpdareload dir="${{build.test.classes.dir}}"/>
            </target>
            
            <target name="debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,-do-debug-fix-test</xsl:attribute>
            </target>
            
            <xsl:comment>
                CLEANUP SECTION
            </xsl:comment>
            
            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-clean'"/>
            </xsl:call-template>
            
            <target name="-do-clean">
                <xsl:attribute name="depends">init</xsl:attribute>
                <delete dir="${{build.dir}}"/>
                <delete dir="${{dist.dir}}"/>
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
                <!-- shouldn't we also clean the libraries copied to ear project's build directory??? -->
                <xsl:attribute name="depends">clean</xsl:attribute>
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
    
    <!-- Multiple src roots -->
    
    <xsl:template name="createRootAvailableTest">
        <xsl:param name="roots"/>
        <xsl:param name="propName"/>
        <xsl:element name="condition">
            <xsl:attribute name="property"><xsl:value-of select="$propName"/></xsl:attribute>
            <or>
                <xsl:for-each select="$roots/ejbjarproject3:root">
                    <xsl:element name="available">
                        <xsl:attribute name="file"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
                    </xsl:element>
                </xsl:for-each>
            </or>
        </xsl:element>
    </xsl:template>
    
    <xsl:template name="createSourcePathValidityTest">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/ejbjarproject3:root">
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
        <xsl:for-each select="$roots/ejbjarproject3:root">
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
        <xsl:for-each select="$roots/ejbjarproject3:root">
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
        <xsl:for-each select="$locations/ejbjarproject3:root">
            <xsl:element name="pathelement">
                <xsl:attribute name="location"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="createPath">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/ejbjarproject3:root">
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
            <xsl:variable name="lib" select="concat('${',$libfile,'.libfile.',$files,'}')"/>
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
                <xsl:with-param name="index" select="1"/>
                <xsl:with-param name="libfile" select="$libfile"/>
                <xsl:with-param name="property" select="$property"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="manifestPrintEntriesIterateFilesIncreasingOrder" >
        <xsl:param name="property"/>
        <xsl:param name="files" /><!-- number of files in the libfile property -->
        <xsl:param name="index" /><!-- index of file in the libfile property -->
        <xsl:param name="libfile"/>
        <xsl:if test="$files &gt; 0">
            <xsl:variable name="propertyName" select="concat($property, '.', $index)"/>
            <xsl:text>${</xsl:text><xsl:value-of select="$propertyName"/><xsl:text>} </xsl:text>
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
        <xsl:if test="$files &gt; 0">
            <xsl:variable name="fileNo" select="$files+(-1)"/>
            <xsl:variable name="lib" select="concat('${',$libfile,'.libfile.',$files,'}')"/>
            <copy file="{$lib}" todir="{$target}"/>
            <xsl:call-template name="copyIterateFiles">
                <xsl:with-param name="files" select="$fileNo"/>
                <xsl:with-param name="target" select="$target"/>
                <xsl:with-param name="libfile" select="$libfile"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="copyIterateDirs" >
        <xsl:param name="files" />
        <xsl:param name="target"/>
        <xsl:param name="libfile"/>
        <xsl:if test="$files &gt; 0">
            <xsl:variable name="fileNo" select="$files+(-1)"/>
            <xsl:variable name="lib" select="concat('${',$libfile,'.libdir.',$files,'}')"/>
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
