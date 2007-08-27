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
                xmlns:j2seproject1="http://www.netbeans.org/ns/j2se-project/1"
                xmlns:j2seproject2="http://www.netbeans.org/ns/j2se-project/2"
                xmlns:j2seproject3="http://www.netbeans.org/ns/j2se-project/3"
                xmlns:jaxrpc="http://www.netbeans.org/ns/j2se-project/jax-rpc"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                xmlns:projdeps2="http://www.netbeans.org/ns/ant-project-references/2"
                exclude-result-prefixes="xalan p projdeps projdeps2">
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
        
        <xsl:variable name="name" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:name"/>
        <!-- Synch with build-impl.xsl: -->
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-impl">
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>
            
            <target name="default">
                <xsl:attribute name="depends">test,jar,javadoc</xsl:attribute>
                <xsl:attribute name="description">Build and test whole project.</xsl:attribute>
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
                <property file="nbproject/private/config.properties"/>
                <property file="nbproject/private/configs/${{config}}.properties"/>
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
                <property file="nbproject/configs/${{config}}.properties"/>
                <property file="nbproject/project.properties"/>
            </target>
            
            <target name="-do-init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-init-macrodef-property</xsl:attribute>
                <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                    <j2seproject1:property name="platform.home" value="platforms.${{platform.active}}.home"/>
                    <j2seproject1:property name="platform.bootcp" value="platforms.${{platform.active}}.bootclasspath"/>
                    <j2seproject1:property name="platform.compiler" value="platforms.${{platform.active}}.compile"/>
                    <j2seproject1:property name="platform.javac.tmp" value="platforms.${{platform.active}}.javac"/>
                    <condition property="platform.javac" value="${{platform.home}}/bin/javac">
                        <equals arg1="${{platform.javac.tmp}}" arg2="$${{platforms.${{platform.active}}.javac}}"/>
                    </condition>
                    <property name="platform.javac" value="${{platform.javac.tmp}}"/>
                    <j2seproject1:property name="platform.java.tmp" value="platforms.${{platform.active}}.java"/>
                    <condition property="platform.java" value="${{platform.home}}/bin/java">
                        <equals arg1="${{platform.java.tmp}}" arg2="$${{platforms.${{platform.active}}.java}}"/>
                    </condition>
                    <property name="platform.java" value="${{platform.java.tmp}}"/>
                    <j2seproject1:property name="platform.javadoc.tmp" value="platforms.${{platform.active}}.javadoc"/>
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
                <available file="${{manifest.file}}" property="manifest.available"/>
                <condition property="manifest.available+main.class">
                    <and>
                        <isset property="manifest.available"/>
                        <isset property="main.class"/>
                        <not>
                            <equals arg1="${{main.class}}" arg2="" trim="true"/>
                        </not>
                    </and>
                </condition>
                <condition property="manifest.available+main.class+mkdist.available">
                    <and>
                        <istrue value="${{manifest.available+main.class}}"/>
                        <isset property="libs.CopyLibs.classpath"/>
                    </and>
                </condition>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                    <xsl:with-param name="propName">have.tests</xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                    <xsl:with-param name="propName">have.sources</xsl:with-param>
                </xsl:call-template>
                <condition property="netbeans.home+have.tests">
                    <and>
                        <isset property="netbeans.home"/>
                        <isset property="have.tests"/>
                    </and>
                </condition>
                <condition property="no.javadoc.preview">
                    <and>
                        <isset property="javadoc.preview"/>
                        <isfalse value="${{javadoc.preview}}"/>
                    </and>
                </condition>
                <property name="run.jvmargs" value=""/>
                <property name="javac.compilerargs" value=""/>
                <property name="work.dir" value="${{basedir}}"/>
                <condition property="no.deps">
                    <and>
                        <istrue value="${{no.dependencies}}"/>
                    </and>
                </condition>
                <property name="javac.debug" value="true"/>
                <property name="javadoc.preview" value="true"/>
                <property name="application.args" value=""/>
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
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                </xsl:call-template>
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                </xsl:call-template>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.dir">Must set build.classes.dir</fail>
                <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail>
                <fail unless="build.test.classes.dir">Must set build.test.classes.dir</fail>
                <fail unless="build.test.results.dir">Must set build.test.results.dir</fail>
                <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>
                <fail unless="dist.jar">Must set dist.jar</fail>
            </target>
            
            <target name="-init-macrodef-property">
                <macrodef>
                    <xsl:attribute name="name">property</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/1</xsl:attribute>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.classpath}</xsl:attribute>
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
                        <xsl:attribute name="name">javac.compilerargs.jaxws</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <javac>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <xsl:attribute name="sourcepath"/>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                            <xsl:if test ="not(/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform/@explicit-source-supported ='false')">                            
                                <xsl:attribute name="source">${javac.source}</xsl:attribute>
                                <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            </xsl:if>                            
                            <xsl:attribute name="includes">@{includes}</xsl:attribute>
                            <xsl:attribute name="excludes">@{excludes}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <xsl:attribute name="fork">yes</xsl:attribute>
                                <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                                <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
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
                <macrodef> <!-- #36033, #85707 -->
                    <xsl:attribute name="name">depend</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.classpath}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <depend>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="cache">${build.dir}/depcache</xsl:attribute>
                            <xsl:attribute name="includes">${includes}</xsl:attribute>
                            <xsl:attribute name="excludes">${excludes}</xsl:attribute>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                        </depend>
                    </sequential>
                </macrodef>
                <macrodef> <!-- #85707 -->
                    <xsl:attribute name="name">force-recompile</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <fail unless="javac.includes">Must set javac.includes</fail>
                        <!-- XXX one little flaw in this weird trick: does not work on folders. -->
                        <pathconvert>
                            <xsl:attribute name="property">javac.includes.binary</xsl:attribute>
                            <xsl:attribute name="pathsep">,</xsl:attribute>
                            <path>
                                <filelist>
                                    <xsl:attribute name="dir">@{destdir}</xsl:attribute>
                                    <xsl:attribute name="files">${javac.includes}</xsl:attribute>
                                </filelist>
                            </path>
                            <globmapper>
                                <xsl:attribute name="from">*.java</xsl:attribute>
                                <xsl:attribute name="to">*.class</xsl:attribute>
                            </globmapper>
                        </pathconvert>
                        <delete>
                            <files includes="${{javac.includes.binary}}"/>
                        </delete>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-junit">
                <macrodef>
                    <xsl:attribute name="name">junit</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <junit>
                            <xsl:attribute name="showoutput">true</xsl:attribute>
                            <xsl:attribute name="fork">true</xsl:attribute>
                            <xsl:attribute name="dir">${basedir}</xsl:attribute> <!-- #47474: match <java> --> 
                            <xsl:attribute name="failureproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="errorproperty">tests.failed</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <batchtest todir="${{build.test.results.dir}}">
                                <xsl:call-template name="createFilesets">
                                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                                    <xsl:with-param name="includes">@{includes}</xsl:with-param>
                                    <xsl:with-param name="includes2">**/*Test.java</xsl:with-param>
                                    <xsl:with-param name="excludes">@{excludes}</xsl:with-param>
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
                            <jvmarg line="${{run.jvmargs}}"/>
                        </junit>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-nbjpda">
                <macrodef>
                    <xsl:attribute name="name">nbjpdastart</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/1</xsl:attribute>
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
                        <nbjpdastart transport="dt_socket" addressproperty="jpda.address" name="@{{name}}" stopclassname="@{{stopclassname}}">
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <bootclasspath>
                                    <path path="${{platform.bootcp}}"/>
                                </bootclasspath>
                            </xsl:if>
                        </nbjpdastart>
                    </sequential>
                </macrodef>
                <macrodef>
                    <xsl:attribute name="name">nbjpdareload</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/1</xsl:attribute>
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
                    <xsl:when test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                    <sequential>
                        <java fork="true" classname="@{{classname}}">
                            <xsl:attribute name="dir">${work.dir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <jvmarg line="${{debug-args-line}}"/>
                            <jvmarg value="-Xrunjdwp:transport=dt_socket,address=${{jpda.address}}"/>
                            <jvmarg line="${{run.jvmargs}}"/>
                            <classpath>
                                <path path="@{{classpath}}"/>
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
            
            <target name="-init-macrodef-java">
                <macrodef>
                    <xsl:attribute name="name">java</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/1</xsl:attribute>
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
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <jvmarg line="${{run.jvmargs}}"/>
                            <classpath>
                                <path path="${{run.classpath}}"/>
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
            
            <target name="-init-presetdef-jar">
                <presetdef>
                    <xsl:attribute name="name">jar</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/1</xsl:attribute>
                    <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}">
                        <j2seproject1:fileset dir="${{build.classes.dir}}"/>
                        <!-- XXX should have a property serving as the excludes list -->
                    </jar>
                </presetdef>
            </target>
            
            <target name="init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-do-init,-post-init,-init-check,-init-macrodef-property,-init-macrodef-javac,-init-macrodef-junit,-init-macrodef-nbjpda,-init-macrodef-debug,-init-macrodef-java,-init-presetdef-jar</xsl:attribute>
            </target>
            
            <xsl:comment>
                ===================
                COMPILATION SECTION
                ===================
            </xsl:comment>
            
            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-jar'"/>
                <xsl:with-param name="type" select="'jar'"/>
            </xsl:call-template>
            
            <xsl:if test="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">
                <target name="wscompile-init" depends="init">
                    <taskdef name="wscompile" classname="com.sun.xml.rpc.tools.ant.Wscompile"
                             classpath="${{wscompile.classpath}}"/>
                    <taskdef name="wsclientuptodate" classname="org.netbeans.modules.websvc.jaxrpc.ant.WsClientUpToDate"
                             classpath="${{wsclientuptodate.classpath}}"/>
                    
                    <mkdir dir="${{build.classes.dir}}"/>
                    <mkdir dir="${{build.generated.dir}}/wsclient"/>
                    <mkdir dir="${{build.generated.dir}}/wsbinary"/>
                    
                    <xsl:for-each select="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">
                        <xsl:variable name="wsclientname">
                            <xsl:value-of select="jaxrpc:web-service-client-name"/>
                        </xsl:variable>
                        
                        <wsclientuptodate property="wscompile.client.{$wsclientname}.notrequired"
                                          sourcewsdl="${{meta.inf.dir}}/wsdl/{$wsclientname}.wsdl"
                                          targetdir="${{build.generated.dir}}/wsclient"/>
                    </xsl:for-each>
                </target>
            </xsl:if>
            
            <xsl:for-each select="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">
                <xsl:variable name="wsclientname">
                    <xsl:value-of select="jaxrpc:web-service-client-name"/>
                </xsl:variable>
                <xsl:variable name="useimport">
                    <xsl:choose>
                        <xsl:when test="jaxrpc:web-service-stub-type">
                            <xsl:value-of select="jaxrpc:web-service-stub-type='jsr-109_client'"/>
                        </xsl:when>
                        <xsl:otherwise>true</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="useclient">
                    <xsl:choose>
                        <xsl:when test="jaxrpc:web-service-stub-type">
                            <xsl:value-of select="jaxrpc:web-service-stub-type='jaxrpc_static_client'"/>
                        </xsl:when>
                        <xsl:otherwise>false</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                
                <target name="{$wsclientname}-client-wscompile" depends="wscompile-init" unless="wscompile.client.{$wsclientname}.notrequired">
                    <property name="config_target" location="${{meta.inf.dir}}/wsdl"/>
                    <copy file="${{meta.inf.dir}}/wsdl/{$wsclientname}-config.xml"
                          tofile="${{build.generated.dir}}/wsclient/wsdl/{$wsclientname}-config.xml" filtering="on" encoding="UTF-8">
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
                        base="${{build.generated.dir}}/wsbinary"
                        sourceBase="${{build.generated.dir}}/wsclient"
                        classpath="${{wscompile.classpath}}:${{javac.classpath}}"
                        mapping="${{build.generated.dir}}/wsclient/wsdl/{$wsclientname}-mapping.xml"
                        httpproxy="${{wscompile.client.{$wsclientname}.proxy}}"
                        config="${{build.generated.dir}}/wsclient/wsdl/{$wsclientname}-config.xml">
                    </wscompile>
                </target>
            </xsl:for-each>
            
            <xsl:if test="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">
                <target name="web-service-client-generate">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:variable name="wsname2">
                                <xsl:value-of select="jaxrpc:web-service-client-name"/>
                            </xsl:variable>
                            <xsl:value-of select="jaxrpc:web-service-client-name"/><xsl:text>-client-wscompile</xsl:text>
                        </xsl:for-each>
                    </xsl:attribute>
                </target>
                <target name="web-service-client-compile" depends="web-service-client-generate">
                    <j2seproject3:depend srcdir="${{build.generated.dir}}/wsclient" classpath="${{wscompile.classpath}}:${{javac.classpath}}" destdir="${{build.classes.dir}}"/>
                    <j2seproject3:javac srcdir="${{build.generated.dir}}/wsclient" classpath="${{wscompile.classpath}}:${{javac.classpath}}" destdir="${{build.classes.dir}}"/>
                </target>
            </xsl:if>
            
            <target name="-pre-pre-compile">
                <xsl:attribute name="depends">init,deps-jar<xsl:if test="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">,web-service-client-generate</xsl:if></xsl:attribute>
                <mkdir dir="${{build.classes.dir}}"/>
            </target>
            
            <target name="-pre-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-compile">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile<xsl:if test="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">,web-service-client-compile</xsl:if></xsl:attribute>
                <xsl:attribute name="if">have.sources</xsl:attribute>
                <j2seproject3:depend/>
                <j2seproject3:javac/>
                <copy todir="${{build.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
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
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile<xsl:if test="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">,web-service-client-compile</xsl:if></xsl:attribute>
                <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
                <j2seproject3:force-recompile/>
                <j2seproject3:javac includes="${{javac.includes}}" excludes=""/>
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
                JAR BUILDING SECTION
                ====================
            </xsl:comment>
            
            <target name="-pre-pre-jar">
                <xsl:attribute name="depends">init</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
            </target>
            
            <target name="-pre-jar">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-jar-without-manifest">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar</xsl:attribute>
                <xsl:attribute name="unless">manifest.available</xsl:attribute>
                <j2seproject1:jar/>
            </target>
            
            <target name="-do-jar-with-manifest">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar</xsl:attribute>
                <xsl:attribute name="if">manifest.available</xsl:attribute>
                <xsl:attribute name="unless">manifest.available+main.class</xsl:attribute>
                <j2seproject1:jar manifest="${{manifest.file}}"/>
            </target>
            
            <target name="-do-jar-with-mainclass">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar</xsl:attribute>
                <xsl:attribute name="if">manifest.available+main.class</xsl:attribute>
                <xsl:attribute name="unless">manifest.available+main.class+mkdist.available</xsl:attribute>
                <j2seproject1:jar manifest="${{manifest.file}}">
                    <j2seproject1:manifest>
                        <j2seproject1:attribute name="Main-Class" value="${{main.class}}"/>
                    </j2seproject1:manifest>
                </j2seproject1:jar>
                <echo>To run this application from the command line without Ant, try:</echo>
                <property name="build.classes.dir.resolved" location="${{build.classes.dir}}"/>
                <property name="dist.jar.resolved" location="${{dist.jar}}"/>
                <pathconvert property="run.classpath.with.dist.jar">
                    <path path="${{run.classpath}}"/>
                    <map from="${{build.classes.dir.resolved}}" to="${{dist.jar.resolved}}"/>
                </pathconvert>
                <echo><xsl:choose>
                        <xsl:when test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">${platform.java}</xsl:when>
                        <xsl:otherwise>java</xsl:otherwise>
                </xsl:choose> -cp "${run.classpath.with.dist.jar}" ${main.class}</echo>
            </target>
            
            <target name="-do-jar-with-libraries">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar</xsl:attribute>
                <xsl:attribute name="if">manifest.available+main.class+mkdist.available</xsl:attribute>
                
                <property name="build.classes.dir.resolved" location="${{build.classes.dir}}"/>
                <pathconvert property="run.classpath.without.build.classes.dir">
                    <path path="${{run.classpath}}"/>
                    <map from="${{build.classes.dir.resolved}}" to=""/>
                </pathconvert>        
                <pathconvert property="jar.classpath" pathsep=" ">
                    <path path="${{run.classpath.without.build.classes.dir}}"/>
                    <chainedmapper>
                        <flattenmapper/>
                        <globmapper from="*" to="lib/*"/>
                    </chainedmapper>
                </pathconvert>        
                <taskdef classname="org.netbeans.modules.java.j2seproject.copylibstask.CopyLibs" name="copylibs" classpath="${{libs.CopyLibs.classpath}}"/>
                <copylibs manifest="${{manifest.file}}" runtimeclasspath="${{run.classpath.without.build.classes.dir}}" jarfile="${{dist.jar}}" compress="${{jar.compress}}">
                    <fileset dir="${{build.classes.dir}}"/>
                    <manifest>
                        <attribute name="Main-Class" value="${{main.class}}"/>
                        <attribute name="Class-Path" value="${{jar.classpath}}"/>
                    </manifest>
                </copylibs>                                
                <echo>To run this application from the command line without Ant, try:</echo>
                <property name="dist.jar.resolved" location="${{dist.jar}}"/>
                <echo><xsl:choose>
                        <xsl:when test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">${platform.java}</xsl:when>
                        <xsl:otherwise>java</xsl:otherwise>
                </xsl:choose> -jar "${dist.jar.resolved}"</echo>                
            </target>
            
            <target name="-post-jar">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="jar">
                <xsl:attribute name="depends">init,compile,-pre-jar,-do-jar-with-manifest,-do-jar-without-manifest,-do-jar-with-mainclass,-do-jar-with-libraries,-post-jar</xsl:attribute>
                <xsl:attribute name="description">Build JAR.</xsl:attribute>
            </target>
            
            <xsl:comment>
                =================
                EXECUTION SECTION
                =================
            </xsl:comment>
            
            <target name="run">
                <xsl:attribute name="depends">init,compile</xsl:attribute>
                <xsl:attribute name="description">Run a main class.</xsl:attribute>
                <j2seproject1:java>
                    <customize>
                        <arg line="${{application.args}}"/>
                    </customize>
                </j2seproject1:java>
            </target>
            
            <target name="run-single">
                <xsl:attribute name="depends">init,compile</xsl:attribute>
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <j2seproject1:java classname="${{run.class}}"/>
            </target>
            
            <xsl:comment>
                =================
                DEBUGGING SECTION
                =================
            </xsl:comment>
            
            <target name="-debug-start-debugger">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <j2seproject1:nbjpdastart name="${{debug.class}}"/>
            </target>
            
            <target name="-debug-start-debuggee">
                <xsl:attribute name="depends">init,compile</xsl:attribute>
                <j2seproject3:debug>
                    <customize>
                        <arg line="${{application.args}}"/>
                    </customize>
                </j2seproject3:debug>
            </target>
            
            <target name="debug">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-debug-start-debugger,-debug-start-debuggee</xsl:attribute>
                <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
            </target>
            
            <target name="-debug-start-debugger-stepinto">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <j2seproject1:nbjpdastart stopclassname="${{main.class}}"/>
            </target>
            
            <target name="debug-stepinto">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-debug-start-debugger-stepinto,-debug-start-debuggee</xsl:attribute>
            </target>
            
            <target name="-debug-start-debuggee-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="debug.class">Must select one file in the IDE or set debug.class</fail>
                <j2seproject3:debug classname="${{debug.class}}"/>
            </target>
            
            <target name="debug-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-debug-start-debugger,-debug-start-debuggee-single</xsl:attribute>
            </target>
            
            <target name="-pre-debug-fix">
                <xsl:attribute name="depends">init</xsl:attribute>
                <fail unless="fix.includes">Must set fix.includes</fail>
                <property name="javac.includes" value="${{fix.includes}}.java"/>
            </target>
            
            <target name="-do-debug-fix">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,compile-single</xsl:attribute>
                <j2seproject1:nbjpdareload/>
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
                    <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                        <xsl:attribute name="executable">${platform.javadoc}</xsl:attribute>
                    </xsl:if>                                                        
                    <classpath>
                        <path path="${{javac.classpath}}"/>
                    </classpath>
                    <!-- Does not work with includes/excludes:
                    <sourcepath>
                        <xsl:call-template name="createPathElements">
                            <xsl:with-param name="locations" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                        </xsl:call-template>
                    </sourcepath>
                    -->
                    <xsl:call-template name="createPackagesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                    </xsl:call-template>
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                        <xsl:with-param name="includes2">**/*.java</xsl:with-param>
                    </xsl:call-template>
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
                <xsl:element name="j2seproject3:depend">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}</xsl:attribute>
                </xsl:element>
                <xsl:element name="j2seproject3:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="debug">true</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}</xsl:attribute>
                </xsl:element>
                <copy todir="${{build.test.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
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
                <xsl:element name="j2seproject3:force-recompile">
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                </xsl:element>
                <xsl:element name="j2seproject3:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
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
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
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
                <j2seproject3:junit/>
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
                <j2seproject3:junit includes="${{test.includes}}" excludes=""/>
            </target>
            
            <target name="-post-test-run-single">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single,-do-test-run-single</xsl:attribute>
                <fail if="tests.failed">Some tests failed; see details above.</fail>
            </target>
            
            <target name="test-single">
                <xsl:attribute name="depends">init,compile-test,-pre-test-run-single,-do-test-run-single,-post-test-run-single</xsl:attribute>
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
                <mkdir dir="${{build.test.results.dir}}"/>
                <!--Ugly, puts ant and ant-junit to the test classpath, but there is probably no other solution how to run the XML formatter -->
                <j2seproject3:debug classname="org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner" classpath="${{ant.home}}/lib/ant.jar:${{ant.home}}/lib/ant-junit.jar:${{debug.test.classpath}}">
                    <customize>                        
                        <syspropertyset>
                            <propertyref prefix="test-sys-prop."/>
                            <mapper type="glob" from="test-sys-prop.*" to="*"/>
                        </syspropertyset>                        
                        <arg value="${{test.class}}"/>
                        <arg value="showoutput=true"/>
                        <arg value="formatter=org.apache.tools.ant.taskdefs.optional.junit.BriefJUnitResultFormatter"/>
                        <arg value="formatter=org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter,${{test.report.file}}"/>
                    </customize>
                </j2seproject3:debug>
            </target>
            
            <target name="-debug-start-debugger-test">
                <xsl:attribute name="if">netbeans.home+have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test</xsl:attribute>
                <j2seproject1:nbjpdastart name="${{test.class}}" classpath="${{debug.test.classpath}}"/>
            </target>
            
            <target name="debug-test">
                <xsl:attribute name="depends">init,compile-test,-debug-start-debugger-test,-debug-start-debuggee-test</xsl:attribute>
            </target>
            
            <target name="-do-debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,compile-test-single</xsl:attribute>
                <j2seproject1:nbjpdareload dir="${{build.test.classes.dir}}"/>
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
                <j2seproject1:java classname="sun.applet.AppletViewer">
                    <customize>
                        <arg value="${{applet.url}}"/>
                    </customize>
                </j2seproject1:java>
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
                <j2seproject3:debug classname="sun.applet.AppletViewer">
                    <customize>
                        <arg value="${{applet.url}}"/>
                    </customize>
                </j2seproject3:debug>
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
            
        </project>
        
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
        <target name="{$targetname}">
            <xsl:attribute name="depends">init</xsl:attribute>
            <xsl:attribute name="unless">no.deps</xsl:attribute>
            
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
                <ant target="{$subtarget}" inheritall="false" antfile="${{project.{$subproj}}}/{$script}"/>
            </xsl:for-each>
            
        </target>
    </xsl:template>
    
    <xsl:template name="createRootAvailableTest">
        <xsl:param name="roots"/>
        <xsl:param name="propName"/>
        <xsl:element name="condition">
            <xsl:attribute name="property"><xsl:value-of select="$propName"/></xsl:attribute>
            <or>
                <xsl:for-each select="$roots/j2seproject3:root">
                    <xsl:element name="available">
                        <xsl:attribute name="file"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
                    </xsl:element>
                </xsl:for-each>
            </or>
        </xsl:element>
    </xsl:template>
    
    <xsl:template name="createSourcePathValidityTest">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/j2seproject3:root">
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
        <xsl:for-each select="$roots/j2seproject3:root">
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
        <xsl:for-each select="$roots/j2seproject3:root">
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
        <xsl:for-each select="$locations/j2seproject3:root">
            <xsl:element name="pathelement">
                <xsl:attribute name="location"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="createPath">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/j2seproject3:root">
            <xsl:if test="position() != 1">
                <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:text>${</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>}</xsl:text>
        </xsl:for-each>						
    </xsl:template>
    
</xsl:stylesheet>
