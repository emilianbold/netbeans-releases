<?xml version="1.0" encoding="UTF-8"?>
<!--
                Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
Microsystems, Inc. All Rights Reserved.
-->
<!--
XXX should not have changed /1 to /2 for URI of *all* macrodefs; only the ones
that actually changed semantically as a result of supporting multiple compilation
units. E.g. <webproject1:property/> did not change at all, whereas
<webproject1:javac/> did. Need to only update URIs where necessary; otherwise we
cause gratuitous incompatibilities for people overriding macrodef targets. Also
we will need to have an upgrade guide that enumerates all build script incompatibilities
introduced by support for multiple source roots. -jglick
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:webproject1="http://www.netbeans.org/ns/web-project/1"
                xmlns:webproject2="http://www.netbeans.org/ns/web-project/2"
                xmlns:webproject3="http://www.netbeans.org/ns/web-project/3"
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

        <xsl:variable name="name" select="/p:project/p:configuration/webproject3:data/webproject3:name"/>
        <!-- Synch with build-impl.xsl: -->
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-impl">
            <xsl:attribute name="default">build</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>

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
                <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                    <webproject1:property name="platform.home" value="platforms.${{platform.active}}.home"/>
                    <webproject1:property name="platform.bootcp" value="platforms.${{platform.active}}.bootclasspath"/>
                    <webproject1:property name="platform.compiler" value="platforms.${{platform.active}}.compile"/>
                    <webproject1:property name="platform.javac.tmp" value="platforms.${{platform.active}}.javac"/>
                    <condition property="platform.javac" value="${{platform.home}}/bin/javac">
                        <equals arg1="${{platform.javac.tmp}}" arg2="$${{platforms.${{platform.active}}.javac}}"/>
                    </condition>
                    <property name="platform.javac" value="${{platform.javac.tmp}}"/>
                    <webproject1:property name="platform.java.tmp" value="platforms.${{platform.active}}.java"/>
                    <condition property="platform.java" value="${{platform.home}}/bin/java">
                        <equals arg1="${{platform.java.tmp}}" arg2="$${{platforms.${{platform.active}}.java}}"/>
                    </condition>
                    <property name="platform.java" value="${{platform.java.tmp}}"/>
                    <property file="${{file.tmp}}"/>
                    <delete file="${{file.tmp}}"/>
                    <fail unless="platform.home">Must set platform.home</fail>
                    <fail unless="platform.bootcp">Must set platform.bootcp</fail>
                    <fail unless="platform.java">Must set platform.java</fail>
                    <fail unless="platform.javac">Must set platform.javac</fail>
                    <fail if="platform.invalid">Platform is not correctly set up</fail>
                </xsl:if>
                <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:use-manifest">
                    <fail unless="manifest.file">Must set manifest.file</fail>
                </xsl:if>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
                    <xsl:with-param name="propName">have.tests</xsl:with-param>
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
                <property name="javac.compilerargs" value=""/>
                <condition property="no.deps">
                    <and>
                        <istrue value="${{no.dependencies}}"/>
                    </and>
                </condition>
                <property name="build.web.excludes" value="${{build.classes.excludes}}"/>
                <condition property="do.compile.jsps">
                    <istrue value="${{compile.jsps}}"/>
                </condition>
                <condition property="do.display.browser">
                    <istrue value="${{display.browser}}"/>
                </condition>
                <available file="${{conf.dir}}/MANIFEST.MF" property="has.custom.manifest"/>
                <property value="${{build.web.dir}}/META-INF" name="build.meta.inf.dir"/>
                <property name="build.classes.dir.real" value="${{build.classes.dir}}"/>
                <property name="build.web.dir.real" value="${{build.web.dir}}"/>
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
                    <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                </xsl:call-template>
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
                </xsl:call-template>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="build.web.dir">Must set build.web.dir</fail>
                <fail unless="build.generated.dir">Must set build.generated.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.dir">Must set build.classes.dir</fail>
                <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail>
                <fail unless="build.test.classes.dir">Must set build.test.classes.dir</fail>
                <fail unless="build.test.results.dir">Must set build.test.results.dir</fail>
                <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>
                <fail unless="dist.war">Must set dist.war</fail>
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
                                <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
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
                            <xsl:if test ="not(/p:project/p:configuration/webproject3:data/webproject3:explicit-platform/@explicit-source-supported ='false')">
                                <xsl:attribute name="source">${javac.source}</xsl:attribute>
                            </xsl:if>
                            <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
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
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <batchtest todir="${{build.test.results.dir}}">
                                <xsl:call-template name="createFilesets">
                                    <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
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
                            <!-- TBD
                            <formatter type="xml"/>
                            -->
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
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
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

            <target name="-init-macrodef-nbjpda">
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
                        <nbjpdastart transport="dt_socket" addressproperty="jpda.address" name="@{{name}}">
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
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
                            <fileset includes="${{fix.includes}}*.class" dir="@{{dir}}"/>
                        </nbjpdareload>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-debug">
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
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <jvmarg value="-Xdebug"/>
                            <jvmarg value="-Xnoagent"/>
                            <jvmarg value="-Djava.compiler=none"/>
                            <jvmarg value="-Xrunjdwp:transport=dt_socket,address=${{jpda.address}}"/>
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

            <target name="init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-do-init,-post-init,-init-check,-init-macrodef-property,-init-macrodef-javac,-init-macrodef-junit,-init-macrodef-java,-init-macrodef-nbjpda,-init-macrodef-debug</xsl:attribute>
            </target>

            <xsl:comment>
    ======================
    COMPILATION SECTION
    ======================
    </xsl:comment>
            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-module-jar'"/>
                <xsl:with-param name="type" select="'jar'"/>
            </xsl:call-template>

            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-ear-jar'"/>
                <xsl:with-param name="type" select="'jar'"/>
                <xsl:with-param name="ear" select="'true'"/>
            </xsl:call-template>

            <target name="deps-jar">
                <xsl:attribute name="depends">init, deps-module-jar, deps-ear-jar</xsl:attribute>
                <xsl:attribute name="unless">no.deps</xsl:attribute>
            </target>
            
            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service|/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">
                <target name="wscompile-init" depends="init">
                    <taskdef name="wscompile" classname="com.sun.xml.rpc.tools.ant.Wscompile"
                        classpath="${{wscompile.classpath}}"/>
                    <taskdef name="wsclientuptodate" classname="org.netbeans.modules.websvc.core.ant.WsClientUpToDate" 
                        classpath="${{wsclientuptodate.classpath}}"/>

                    <mkdir dir="${{build.web.dir.real}}/WEB-INF/wsdl"/>
                    <mkdir dir="${{web.docbase.dir}}/WEB-INF/wsdl"/>
                    <mkdir dir="${{build.classes.dir.real}}"/>
                    <mkdir dir="${{build.generated.dir}}/wssrc"/>
                    
                    <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">
                        <xsl:variable name="wsclientname">
                            <xsl:value-of select="webproject3:web-service-client-name"/>
                        </xsl:variable>

                        <wsclientuptodate property="wscompile.client.{$wsclientname}.notrequired"
                            sourcewsdl="${{web.docbase.dir}}/WEB-INF/wsdl/{$wsclientname}.wsdl" 
                            targetdir="${{build.generated.dir}}/wssrc"/>
                    </xsl:for-each>
                </target>
            </xsl:if>

            <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
              <xsl:variable name="wsname">
                <xsl:value-of select="webproject3:web-service-name"/>
              </xsl:variable>
              <xsl:choose>
              <xsl:when test="webproject3:from-wsdl">
                <target name="{$wsname}_wscompile" depends="init, wscompile-init">
                  <wscompile import="true" 
                     config="${{src.dir}}/${{{$wsname}.config.name}}"
                     features="${{wscompile.service.{$wsname}.features}}"
                     mapping="${{web.docbase.dir}}/WEB-INF/wsdl/${{{$wsname}.mapping}}"
                     classpath="${{wscompile.classpath}}" 
                     nonClassDir="${{build.web.dir.real}}/WEB-INF/wsdl" 
                     verbose="true" 
                     xPrintStackTrace="true" 
                     base="${{build.generated.dir}}/wssrc"
                     sourceBase="${{src.dir}}" 
                     keep="true" 
                     fork="true"/>
                </target>  
              </xsl:when>
              <xsl:otherwise>
                 <target name="{$wsname}_wscompile" depends="wscompile-init">
                  <wscompile
                     server="true"
                     fork="true"
                     keep="true"
                     base="${{build.generated.dir}}/wssrc"
                     xPrintStackTrace="true"
                     verbose="true"
                     nonClassDir="${{build.web.dir.real}}/WEB-INF/wsdl"
                     classpath="${{wscompile.classpath}}:${{build.classes.dir.real}}"
                     mapping="${{build.web.dir.real}}/WEB-INF/wsdl/${{{$wsname}.mapping}}"
                     config="${{src.dir}}/${{{$wsname}.config.name}}"
                     features="${{wscompile.service.{$wsname}.features}}">
                  </wscompile>
                </target>
              </xsl:otherwise>
              </xsl:choose> 
            </xsl:for-each>

            <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">
                <xsl:variable name="wsclientname">
                    <xsl:value-of select="webproject3:web-service-client-name"/>
                </xsl:variable>
                <xsl:variable name="useimport">
                    <xsl:choose>
                        <xsl:when test="webproject3:web-service-stub-type">
                            <xsl:value-of select="webproject3:web-service-stub-type='jsr-109_client'"/>
                        </xsl:when>
                        <xsl:otherwise>true</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="useclient">
                    <xsl:choose>
                        <xsl:when test="webproject3:web-service-stub-type">
                            <xsl:value-of select="webproject3:web-service-stub-type='jaxrpc_static_client'"/>
                        </xsl:when>
                        <xsl:otherwise>false</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <target name="{$wsclientname}_client_wscompile" depends="wscompile-init" unless="wscompile.client.{$wsclientname}.notrequired">
                    <property name="config_target" location="${{web.docbase.dir}}/WEB-INF/wsdl"/>
                    <copy file="${{web.docbase.dir}}/WEB-INF/wsdl/{$wsclientname}-config.xml"
                        tofile="${{build.generated.dir}}/wssrc/wsdl/{$wsclientname}-config.xml" filtering="on">
                        <filterset>
                            <!-- replace token with reference to WSDL file in source tree, not build tree, since the
                                 the file probably has not have been copied to the build tree yet. -->
                            <filter token="CONFIG_ABSOLUTE_PATH" value="${{config_target}}"/>
                        </filterset>
                    </copy>
                    <wscompile
                        xPrintStackTrace="true" verbose="false" fork="true" keep="true"
                        client="{$useclient}" import="{$useimport}"
                        features="${{wscompile.client.{$wsclientname}.features}}"
                        base="${{build.classes.dir.real}}"
                        sourceBase="${{build.generated.dir}}/wssrc"
                        classpath="${{wscompile.classpath}}"
                        mapping="${{build.web.dir.real}}/WEB-INF/wsdl/{$wsclientname}-mapping.xml"
                        config="${{build.generated.dir}}/wssrc/wsdl/{$wsclientname}-config.xml">
                    </wscompile>
                </target>
            </xsl:for-each>

            <target name="-pre-pre-compile">
                <xsl:attribute name="depends">init,deps-jar</xsl:attribute>
                <mkdir dir="${{build.classes.dir.real}}"/>
            </target>

            <target name="-pre-compile">
                <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:variable name="wsname2">
                                <xsl:value-of select="webproject3:web-service-client-name"/>
                            </xsl:variable>
                            <xsl:value-of select="webproject3:web-service-client-name"/><xsl:text>_client_wscompile</xsl:text>
                        </xsl:for-each>
                    </xsl:attribute>
                </xsl:if>
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-do-compile">
                <xsl:attribute name="depends">init, deps-jar, -pre-pre-compile, -pre-compile, -copy-manifest, library-inclusion-in-archive,library-inclusion-in-manifest</xsl:attribute>
                
                <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
                    <xsl:comment>For web services, refresh the Tie and SerializerRegistry classes</xsl:comment> 
                    <delete> 
                      <fileset dir="${{build.classes.dir.real}}" includes="**/*_Tie.* **/*_SerializerRegistry.*"/>
                    </delete>
                </xsl:if>

                <webproject2:javac destdir="${{build.classes.dir.real}}"/>
               
                <copy todir="${{build.classes.dir.real}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                </copy>
                
                <copy todir="${{build.web.dir.real}}">
                  <fileset excludes="${{build.web.excludes}}" dir="${{web.docbase.dir}}">
                   <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
                     <xsl:attribute name="excludes">WEB-INF/classes/** WEB-INF/web.xml WEB/sun-web.xml</xsl:attribute>
                   </xsl:if>
                  </fileset>
                </copy>
                
                <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
                    <xsl:comment>For web services, refresh web.xml and sun-web.xml</xsl:comment>  
                    <copy todir="${{build.web.dir.real}}" overwrite="true"> 
                      <fileset includes="WEB-INF/web.xml WEB-INF/sun-web.xml" dir="${{web.docbase.dir}}"/>
                    </copy>
                 </xsl:if>
                 
            </target>
            
            <target name="-copy-manifest" if="has.custom.manifest">
                <mkdir dir="${{build.meta.inf.dir}}"/>
                <copy todir="${{build.meta.inf.dir}}">
                    <fileset dir="${{conf.dir}}" includes="MANIFEST.MF"/>
                </copy>
            </target>
            
            
            <target name="-post-compile">
				<xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
					<xsl:attribute name="depends">
						<xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
                           <xsl:if test="not(webproject3:from-wsdl)">
							<xsl:if test="position()!=1 and not(preceding-sibling::webproject3:web-service/webproject3:from-wsdl)"><xsl:text>, </xsl:text></xsl:if>
							<xsl:variable name="wsname2">
								<xsl:value-of select="webproject3:web-service-name"/>
							</xsl:variable>
							<xsl:value-of select="webproject3:web-service-name"/><xsl:text>_wscompile</xsl:text>
                           </xsl:if> 
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
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile</xsl:attribute>
                <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
                <webproject2:javac>
                    <customize>
                        <patternset includes="${{javac.includes}}"/>
                    </customize>
                </webproject2:javac>
            </target>

            <target name="-post-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="compile-single">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile-single,-do-compile-single,-post-compile-single</xsl:attribute>
            </target>

            <target name="compile-jsps">
                <xsl:attribute name="depends">compile</xsl:attribute>
                <xsl:attribute name="if">do.compile.jsps</xsl:attribute>
                <xsl:attribute name="description">Test compile JSP pages to expose compilation errors.</xsl:attribute>

                <mkdir dir="${{build.generated.dir}}/src"/>
                <java classname="org.netbeans.modules.web.project.ant.JspC"
                   fork="true"
                   failonerror="true"
                >
                    <arg value="-uriroot"/>
                    <arg file="${{basedir}}/${{build.web.dir.real}}"/>
                    <arg value="-d"/>
                    <arg file="${{basedir}}/${{build.generated.dir}}/src"/>
                    <arg value="-die1"/>
                    <classpath path="${{java.home}}/../lib/tools.jar:${{copyfiles.classpath}}:${{jspcompilation.classpath}}"/>
                </java>
                <mkdir dir="${{build.generated.dir}}/classes"/>
                <webproject2:javac
                    srcdir="${{build.generated.dir}}/src"
                    destdir="${{build.generated.dir}}/classes"
                    classpath="${{j2ee.platform.classpath}}:${{build.classes.dir.real}}:${{jspcompilation.classpath}}"/>

            </target>

            <target name="-do-compile-single-jsp">
                <xsl:attribute name="depends">compile</xsl:attribute>
                <xsl:attribute name="if">jsp.includes</xsl:attribute>
                <fail unless="javac.jsp.includes">Must select some files in the IDE or set javac.jsp.includes</fail>

                <mkdir dir="${{build.generated.dir}}/src"/>
                <java classname="org.netbeans.modules.web.project.ant.JspCSingle"
                   fork="true"
                   failonerror="true"
                >
                    <arg value="-uriroot"/>
                    <arg file="${{basedir}}/${{build.web.dir.real}}"/>
                    <arg value="-d"/>
                    <arg file="${{basedir}}/${{build.generated.dir}}/src"/>
                    <arg value="-die1"/>
                    <arg value="-jspc.files"/>
                    <arg path="${{jsp.includes}}"/>
                    <classpath path="${{java.home}}/../lib/tools.jar:${{copyfiles.classpath}}:${{jspcompilation.classpath}}"/>
                </java>
                <mkdir dir="${{build.generated.dir}}/classes"/>
                <webproject2:javac
                    srcdir="${{build.generated.dir}}/src"
                    destdir="${{build.generated.dir}}/classes"
                    classpath="${{j2ee.platform.classpath}}:${{build.classes.dir.real}}:${{jspcompilation.classpath}}">
                    <customize>
                        <patternset includes="${{javac.jsp.includes}}"/>
                    </customize>
                </webproject2:javac>
                <!--
                <webproject:javac xmlns:webproject="http://www.netbeans.org/ns/web-project/1">
                    <xsl:with-param name="srcdir" select="'${{build.generated.dir}}/src'"/>
                    <xsl:with-param name="destdir" select="'${{build.generated.dir}}/classes'"/>
                    <xsl:with-param name="classpath" select="'${{javac.classpath}}:${{j2ee.platform.classpath}}:${{build.classes.dir.real}}'"/>
                    <xsl:with-param name="classpath" select="'${{javac.classpath}}:${{j2ee.platform.classpath}}:${{build.classes.dir.real}}:${{jspc.classpath}}'"/>
               </webproject:javac>
               -->
            </target>

            <target name="compile-single-jsp">
                <fail unless="jsp.includes">Must select a file in the IDE or set jsp.includes</fail>
                <antcall target="-do-compile-single-jsp"/>
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

            <target name="-do-dist-without-manifest" unless="has.custom.manifest">
                <xsl:attribute name="depends">init,compile,compile-jsps,-pre-dist</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.war}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.war}}" compress="${{jar.compress}}">
                    <fileset dir="${{build.web.dir.real}}"/>
                </jar>
            </target>
            
            <target name="-do-dist-with-manifest" if="has.custom.manifest">
                <xsl:attribute name="depends">init,compile,compile-jsps,-pre-dist</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.war}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar manifest="${{build.meta.inf.dir}}/MANIFEST.MF" jarfile="${{dist.war}}" compress="${{jar.compress}}">
                    <fileset dir="${{build.web.dir.real}}"/>
                </jar>
            </target>
            
            <target name="do-dist">
                <xsl:attribute name="depends">init,compile,compile-jsps,-pre-dist,-do-dist-with-manifest,-do-dist-without-manifest</xsl:attribute>
            </target>
            
            <target name="library-inclusion-in-manifest" depends="init">
                <xsl:attribute name="if">dist.ear.dir</xsl:attribute>
                <!-- copy libraries into ear  -->
                <xsl:for-each select="//webproject3:web-module-libraries/webproject3:library[webproject3:path-in-war]">
                    <xsl:variable name="included.prop.name">
                        <xsl:value-of select="webproject3:file"/>
                    </xsl:variable>
                    <xsl:variable name="base.prop.name">
                        <xsl:value-of select="concat('included.lib.', substring-before(substring-after(webproject3:file,'{'),'}'), '')"/>
                    </xsl:variable>
                    <basename>
                        <xsl:attribute name="property"><xsl:value-of select="$base.prop.name"/></xsl:attribute>
                        <xsl:attribute name="file"><xsl:value-of select="$included.prop.name"/></xsl:attribute>
                     </basename>
                     
                    <xsl:if test="//webproject3:web-module-libraries/webproject3:library[@files]">
                        <xsl:call-template name="copyIterateFiles">
                            <xsl:with-param name="files" select="@files"/>
                            <xsl:with-param name="target" select="'${dist.ear.dir}'"/>
                            <xsl:with-param name="libfile" select="webproject3:file"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="//webproject3:web-module-libraries/webproject3:library[@dirs]">
                        <xsl:call-template name="copyIterateDirs">
                            <xsl:with-param name="files" select="@dirs"/>
                            <xsl:with-param name="target" select="'${dist.ear.dir}'"/>
                            <xsl:with-param name="libfile" select="webproject3:file"/>
                        </xsl:call-template>
                    </xsl:if>
                </xsl:for-each>
                <!-- copy additional content into web module -->
                <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-module-additional-libraries/webproject3:library[webproject3:path-in-war]">
                    <xsl:variable name="copyto" select=" webproject3:path-in-war"/>
                    <xsl:if test="//webproject3:web-module-additional-libraries/webproject3:library[@files]">
                        <xsl:call-template name="copyIterateFiles">
                            <xsl:with-param name="files" select="@files"/>
                            <xsl:with-param name="target" select="concat('${build.web.dir.real}/',$copyto)"/>
                            <xsl:with-param name="libfile" select="webproject3:file"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="//webproject3:web-module-additional-libraries/webproject3:library[@dirs]">
                        <xsl:call-template name="copyIterateDirs">
                            <xsl:with-param name="files" select="@files"/>
                            <xsl:with-param name="target" select="concat('${build.web.dir.real}/',$copyto)"/>
                            <xsl:with-param name="libfile" select="webproject3:file"/>
                        </xsl:call-template>
                    </xsl:if>
                </xsl:for-each>
                
                <mkdir dir="${{build.web.dir.real}}/META-INF"/>
                <manifest file="${{build.web.dir.real}}/META-INF/MANIFEST.MF" mode="update">
                    <xsl:if test="//webproject3:web-module-libraries/webproject3:library[webproject3:path-in-war]">
                        <attribute>
                            <xsl:attribute name="name">Class-Path</xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:for-each select="//webproject3:web-module-libraries/webproject3:library[webproject3:path-in-war]">
                                    <xsl:variable name="base.prop.name">
                                        <xsl:value-of select="concat('${included.lib.', substring-before(substring-after(webproject3:file,'{'),'}'), '}')"/>
                                    </xsl:variable>
                                    <xsl:if test="position()>1"><xsl:text> </xsl:text></xsl:if>
                                    <xsl:value-of select="$base.prop.name"/>
                                </xsl:for-each>  
                            </xsl:attribute>
                         </attribute>
                    </xsl:if>
                </manifest>
            </target>
            
            <target name="library-inclusion-in-archive" depends="init">
                <xsl:attribute name="unless">dist.ear.dir</xsl:attribute>
                  <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-module-libraries/webproject3:library[webproject3:path-in-war]">
                    <xsl:variable name="copyto" select=" webproject3:path-in-war"/>
                    <xsl:if test="//webproject3:library[@files]">
                        <xsl:call-template name="copyIterateFiles">
                            <xsl:with-param name="files" select="@files"/>
                            <xsl:with-param name="target" select="concat('${build.web.dir.real}/',$copyto)"/>
                            <xsl:with-param name="libfile" select="webproject3:file"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="//webproject3:library[@dirs]">
                        <xsl:call-template name="copyIterateDirs">
                            <xsl:with-param name="files" select="@dirs"/>
                            <xsl:with-param name="target" select="concat('${build.web.dir.real}/',$copyto)"/>
                            <xsl:with-param name="libfile" select="webproject3:file"/>
                        </xsl:call-template>
                    </xsl:if>
                </xsl:for-each>
                
                <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-module-additional-libraries/webproject3:library[webproject3:path-in-war]">
                    <xsl:variable name="copyto" select=" webproject3:path-in-war"/>
                    <xsl:if test="//webproject3:library[@files]">
                        <xsl:call-template name="copyIterateFiles">
                            <xsl:with-param name="files" select="@files"/>
                            <xsl:with-param name="target" select="concat('${build.web.dir.real}/',$copyto)"/>
                            <xsl:with-param name="libfile" select="webproject3:file"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="//webproject3:library[@dirs]">
                        <xsl:call-template name="copyIterateDirs">
                            <xsl:with-param name="files" select="@files"/>
                            <xsl:with-param name="target" select="concat('${build.web.dir.real}/',$copyto)"/>
                            <xsl:with-param name="libfile" select="webproject3:file"/>
                        </xsl:call-template>
                    </xsl:if>
                </xsl:for-each>
             </target>
            
            <target name="do-ear-dist">
                <xsl:attribute name="depends">init,compile,compile-jsps,-pre-dist,library-inclusion-in-manifest</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.ear.war}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.ear.war}}" compress="${{jar.compress}}" manifest="${{build.web.dir.real}}/META-INF/MANIFEST.MF">
                    <fileset dir="${{build.web.dir.real}}"/>
                </jar>
            </target>
            
            <target name="-post-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="dist">
                <xsl:attribute name="depends">init,compile,-pre-dist,do-dist,-post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution (WAR).</xsl:attribute>
            </target>

            <target name="dist-ear">
                <xsl:attribute name="depends">init,compile,-pre-dist,do-ear-dist,-post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution (WAR) to be packaged into an EAR.</xsl:attribute>
            </target>
            
            <xsl:comment>
    ======================
    EXECUTION SECTION
    ======================
    </xsl:comment>

            <target name="run">
                <xsl:attribute name="depends">run-deploy,run-display-browser</xsl:attribute>
                <xsl:attribute name="description">Deploy to server and show in browser.</xsl:attribute>
            </target>

            <target name="run-deploy">
                <xsl:attribute name="depends">init,compile,compile-jsps,-do-compile-single-jsp,dist</xsl:attribute>
                <nbdeploy debugmode="false" clientUrlPart="${{client.urlPart}}" forceRedeploy="${{forceRedeploy}}"/>
            </target>

            <target name="verify">
                <xsl:attribute name="depends">init,dist</xsl:attribute>
                <nbverify file="${{dist.war}}"/>
            </target>
            
            <target name="run-display-browser" if="do.display.browser">
                <xsl:attribute name="depends">run-deploy</xsl:attribute>
                <nbbrowse url="${{client.url}}"/>
            </target>

            <target name="run-main">
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <webproject1:java classname="${{run.class}}"/>
            </target>


            <xsl:comment>
    ======================
    DEBUGGING SECTION
    ======================
    </xsl:comment>
    <target name="debug">
        <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
        <xsl:attribute name ="depends">init,compile,compile-jsps,-do-compile-single-jsp,dist</xsl:attribute>
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <nbdeploy debugmode="true" clientUrlPart="${{client.urlPart}}"/>
        <nbjpdaconnect name="${{name}}" host="${{jpda.host}}" address="${{jpda.address}}" transport="${{jpda.transport}}">
            <classpath>
                <path path="${{debug.classpath}}:${{ws.debug.classpaths}}"/>
            </classpath>
            <sourcepath>
                <path path="${{web.docbase.dir}}:${{ws.web.docbase.dirs}}"/>
            </sourcepath>
            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
            <bootclasspath>
                <path path="${{platform.bootcp}}"/>
            </bootclasspath>
            </xsl:if>
        </nbjpdaconnect>
        <antcall target="debug-display-browser"/>
    </target>

    <target name="debug-display-browser" if="do.display.browser">
        <nbbrowse url="${{client.url}}"/>
    </target>

    <target name="debug-single">
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <xsl:attribute name="depends">init,compile,compile-jsps,-do-compile-single-jsp,debug</xsl:attribute>
    </target>

    <target name="-debug-start-debugger">
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <xsl:attribute name="depends">init</xsl:attribute>
        <webproject1:nbjpdastart name="${{debug.class}}"/>
    </target>

    <target name="-debug-start-debuggee-single">
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <xsl:attribute name="depends">init,compile-single</xsl:attribute>
        <fail unless="debug.class">Must select one file in the IDE or set debug.class</fail>
        <webproject1:debug classname="${{debug.class}}"/>
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
        <webproject1:nbjpdareload/>
    </target>

    <target name="debug-fix">
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <xsl:attribute name="depends">init,-pre-debug-fix,-do-debug-fix</xsl:attribute>
    </target>

    <xsl:comment>
    ======================
    JAVADOC SECTION
    ======================
    </xsl:comment>

            <target name="javadoc-build">
                <xsl:attribute name="depends">init</xsl:attribute>
                <mkdir dir="${{dist.javadoc.dir}}"/>
                <!-- XXX do an up-to-date check first -->
                <xsl:choose>
                    <xsl:when test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                        <!-- XXX #46901: <javadoc> does not support an explicit executable -->
                        <webproject1:property name="platform.javadoc.tmp" value="platforms.${{platform.active}}.javadoc"/>
                        <condition property="platform.javadoc" value="${{platform.home}}/bin/javadoc">
                            <equals arg1="${{platform.javadoc.tmp}}" arg2="$${{platforms.${{platform.active}}.javadoc}}"/>
                        </condition>
                        <property name="platform.javadoc" value="${{platform.javadoc.tmp}}"/>
                        <condition property="javadoc.notree.opt" value="-notree">
                            <istrue value="${{javadoc.notree}}"/>
                        </condition>
                        <property name="javadoc.notree.opt" value=""/>
                        <condition property="javadoc.use.opt" value="-use">
                            <istrue value="${{javadoc.use}}"/>
                        </condition>
                        <property name="javadoc.use.opt" value=""/>
                        <condition property="javadoc.nonavbar.opt" value="-nonavbar">
                            <istrue value="${{javadoc.nonavbar}}"/>
                        </condition>
                        <property name="javadoc.nonavbar.opt" value=""/>
                        <condition property="javadoc.noindex.opt" value="-noindex">
                            <istrue value="${{javadoc.noindex}}"/>
                        </condition>
                        <property name="javadoc.noindex.opt" value=""/>
                        <condition property="javadoc.splitindex.opt" value="-splitindex">
                            <istrue value="${{javadoc.splitindex}}"/>
                        </condition>
                        <property name="javadoc.splitindex.opt" value=""/>
                        <condition property="javadoc.author.opt" value="-author">
                            <istrue value="${{javadoc.author}}"/>
                        </condition>
                        <property name="javadoc.author.opt" value=""/>
                        <condition property="javadoc.version.opt" value="-version">
                            <istrue value="${{javadoc.version}}"/>
                        </condition>
                        <property name="javadoc.version.opt" value=""/>
                        <condition property="javadoc.private.opt" value="-private">
                            <istrue value="${{javadoc.private}}"/>
                        </condition>
                        <property name="javadoc.private.opt" value=""/>
                        <!-- For weird space-in-path behaviors on Windows, see #46901 and #50548 and #54387 -->
                        <condition property="javadoc.classpath.opt" value='""'>                            
                            <equals arg1="${{javac.classpath}}" arg2=""/>                            
                        </condition>
                        <path id="javadoc.classpath.temp">
                            <pathelement path="${{javac.classpath}}:${{j2ee.platform.classpath}}"/>
                        </path>        
                        <property name="javadoc.classpath.opt" refid="javadoc.classpath.temp"/>
                        <apply executable="${{platform.javadoc}}" failonerror="true" parallel="true">
                            <arg value="-d"/>
                            <arg file="${{dist.javadoc.dir}}"/>
                            <xsl:if test ="not(/p:project/p:configuration/webproject3:data/webproject3:explicit-platform/@explicit-source-supported ='false')">
                                <arg value="-source"/>
                                <arg value="${{javac.source}}"/>
                            </xsl:if>
                            <arg value="-windowtitle"/>
                            <arg value="${{javadoc.windowtitle}}"/>
                            <arg line="${{javadoc.notree.opt}} ${{javadoc.use.opt}} ${{javadoc.nonavbar.opt}} ${{javadoc.noindex.opt}} ${{javadoc.splitindex.opt}} ${{javadoc.author.opt}} ${{javadoc.version.opt}} ${{javadoc.private.opt}} ${{javadoc.classpath.opt}}"/>
                            <arg value="-classpath"/>
                            <arg value="${{javadoc.classpath.opt}}"/>
                            <arg value="-sourcepath"/>
                            <xsl:element name="arg">
                                <xsl:attribute name="path">
                                    <xsl:call-template name="createPath">
                                        <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                            </xsl:element>
                            <xsl:call-template name="createFilesets">
                                    <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                                    <xsl:with-param name="includes">**/*.java</xsl:with-param>
                           </xsl:call-template>
                        </apply>
                    </xsl:when>
                    <xsl:otherwise>
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
                            <xsl:attribute name="failonerror">true</xsl:attribute> <!-- #47325 -->
                            <classpath>
                                <path path="${{javac.classpath}}:${{j2ee.platform.classpath}}"/>
                            </classpath>
                            <sourcepath>
                                <xsl:call-template name="createPathElements">
                                    <xsl:with-param name="locations" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                                </xsl:call-template>
                            </sourcepath>
                            <xsl:call-template name="createFilesets">
                                    <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                            </xsl:call-template>
                        </javadoc>
                    </xsl:otherwise>
                </xsl:choose>
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
                <xsl:element name="webproject2:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="debug">true</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}:${j2ee.platform.classpath}</xsl:attribute>
                </xsl:element>
                <copy todir="${{build.test.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
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
                <xsl:element name="webproject2:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
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
                <webproject2:junit/>
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
                <webproject2:junit includes="${{test.includes}}"/>
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
            
    =======================
    JUNIT DEBUGGING SECTION
    =======================
    </xsl:comment>

            <target name="-debug-start-debuggee-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test</xsl:attribute>
                <fail unless="test.class">Must select one file in the IDE or set test.class</fail>
                <webproject1:debug classname="junit.textui.TestRunner" classpath="${{debug.test.classpath}}" args="${{test.class}}"/>
            </target>

            <target name="-debug-start-debugger-test">
                <xsl:attribute name="if">netbeans.home+have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test</xsl:attribute>
                <webproject1:nbjpdastart name="${{test.class}}" classpath="${{debug.test.classpath}}"/>
            </target>

            <target name="debug-test">
                <xsl:attribute name="depends">init,compile-test,-debug-start-debugger-test,-debug-start-debuggee-test</xsl:attribute>
            </target>

            <target name="-do-debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,compile-test-single</xsl:attribute>
                <webproject1:nbjpdareload dir="${{build.test.classes.dir}}"/>
            </target>

            <target name="debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,-do-debug-fix-test</xsl:attribute>
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
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="unless">dist.ear.dir</xsl:attribute>
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
                <xsl:for-each select="$roots/webproject3:root">
                    <xsl:element name="available">
			        <xsl:attribute name="file"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
		            </xsl:element>
                </xsl:for-each>
            </or>
        </xsl:element>
    </xsl:template>

    <xsl:template name="createSourcePathValidityTest">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/webproject3:root">
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
        <xsl:for-each select="$roots/webproject3:root">
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

    <xsl:template name="createPathElements">
        <xsl:param name="locations"/>
        <xsl:for-each select="$locations/webproject3:root">
            <xsl:element name="pathelement">
			    <xsl:attribute name="location"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
		    </xsl:element>
        </xsl:for-each>
    </xsl:template>

	<xsl:template name="createPath">
		<xsl:param name="roots"/>
		<xsl:for-each select="$roots/webproject3:root">
		    <xsl:if test="position() != 1">
			    <xsl:text>:</xsl:text>
		    </xsl:if>
		    <xsl:text>${</xsl:text>
		    <xsl:value-of select="@id"/>
		    <xsl:text>}</xsl:text>
		</xsl:for-each>						
	</xsl:template>
        
        <xsl:template name="copyIterateFiles" >
            <xsl:param name="files" />
            <xsl:param name="target"/>
            <xsl:param name="libfile"/>
            <xsl:if test="$files &gt; 0">
                <xsl:variable name="fileNo" select="$files+(-1)"/>
                <xsl:variable name="lib" select="concat(substring-before($libfile,'}'),'.libfile.',$files,'}')"/>
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
