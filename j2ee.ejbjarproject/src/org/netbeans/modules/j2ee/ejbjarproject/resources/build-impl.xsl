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
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:ejbjarproject1="http://www.netbeans.org/ns/j2ee-ejbjarproject/1"
                xmlns:ejbjarproject2="http://www.netbeans.org/ns/j2ee-ejbjarproject/2"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                exclude-result-prefixes="xalan p ejb projdeps">
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

        <xsl:variable name="name" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:name"/>
        <project name="{$name}-impl">
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

            <target name="pre-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="init-private">
                <xsl:attribute name="depends">pre-init</xsl:attribute>
                <property file="nbproject/private/private.properties"/>
            </target>

            <target name="init-userdir">
                <xsl:attribute name="depends">pre-init,init-private</xsl:attribute>
                <property name="user.properties.file" location="${{netbeans.user}}/build.properties"/>
            </target>

            <target name="init-user">
                <xsl:attribute name="depends">pre-init,init-private,init-userdir</xsl:attribute>
                <property file="${{user.properties.file}}"/>
            </target>

            <target name="init-project">
                <xsl:attribute name="depends">pre-init,init-private,init-userdir,init-user</xsl:attribute>
                <property file="nbproject/project.properties"/>
            </target>

            <target name="do-init">
                <xsl:attribute name="depends">pre-init,init-private,init-userdir,init-user,init-project,init-macrodef-property</xsl:attribute>
                <xsl:if test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:explicit-platform">
                    <!--Setting java and javac default location -->
                    <property name="platforms.${{platform.active}}.javac" value="${{platform.home}}/bin/javac"/>
                    <property name="platforms.${{platform.active}}.java" value="${{platform.home}}/bin/java"/>
                    <!-- XXX Ugly but Ant does not yet support recursive property evaluation: -->
                    <tempfile property="file.tmp" prefix="platform" suffix=".properties"/>
                    <echo file="${{file.tmp}}">
                        platform.home=$${platforms.${platform.active}.home}
                        platform.bootcp=$${platforms.${platform.active}.bootclasspath}                
                        build.compiler=$${platforms.${platform.active}.compiler}
                        platform.java=$${platforms.${platform.active}.java}
                        platform.javac=$${platforms.${platform.active}.javac}
                    </echo>
                    <property file="${{file.tmp}}"/>
                    <delete file="${{file.tmp}}"/>
                    <fail unless="platform.home">Must set platform.home</fail>
                    <fail unless="platform.bootcp">Must set platform.bootcp</fail>                        
                    <fail unless="platform.java">Must set platform.java</fail>
                    <fail unless="platform.javac">Must set platform.javac</fail>
                </xsl:if>
                <xsl:comment> The two properties below are usually overridden </xsl:comment>
                <xsl:comment> by the active platform. Just a fallback. </xsl:comment>
                <property name="default.javac.source" value="1.4"/>
                <property name="default.javac.target" value="1.4"/>
                <xsl:if test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:use-manifest">
                    <fail unless="manifest.file">Must set manifest.file</fail>
                </xsl:if>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:test-roots"/>
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
                <available file="${{meta.inf}}/MANIFEST.MF" property="has.custom.manifest"/>
                <condition property="classes.dir" value="${{build.ear.classes.dir}}">
                    <isset property="dist.ear.dir"/>
                </condition>
                <property name="classes.dir" value="${{build.classes.dir}}"/>
            </target>

            <target name="post-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="init-check">
                <xsl:attribute name="depends">pre-init,init-private,init-userdir,init-user,init-project,do-init</xsl:attribute>
                <!-- XXX XSLT 2.0 would make it possible to use a for-each here -->
                <!-- Note that if the properties were defined in project.xml that would be easy -->
                <!-- But required props should be defined by the AntBasedProjectType, not stored in each project -->
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:source-roots"/>
                </xsl:call-template>
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:test-roots"/>
                </xsl:call-template>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="build.generated.dir">Must set build.generated.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.dir">Must set build.classes.dir</fail>
                <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail>
                <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>
                <fail unless="dist.jar">Must set dist.jar</fail>
            </target>
            
            <target name="init-macrodef-property">
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

            <target name="init-macrodef-javac">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-ejbjarproject/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:source-roots"/>
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
                            <xsl:attribute name="source">${javac.source}</xsl:attribute>
                            <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:explicit-platform">
                                <xsl:attribute name="fork">yes</xsl:attribute>
                                <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
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

            <target name="init-macrodef-junit">
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
                            <xsl:if test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <batchtest todir="${{build.test.results.dir}}">
                                <xsl:call-template name="createFilesets">
                                    <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:test-roots"/>
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


            <target name="init-macrodef-nbjpda">
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
                            <xsl:if test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:explicit-platform">
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

            <target name="init-macrodef-debug">
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
                    <sequential>
                        <java fork="true" classname="@{{classname}}">
                            <xsl:if test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                                <bootclasspath>
                                    <path path="${{platform.bootcp}}"/>
                                </bootclasspath>
                            </xsl:if>
                            <jvmarg value="-Xdebug"/>
                            <jvmarg value="-Xnoagent"/>
                            <jvmarg value="-Djava.compiler=none"/>
                            <jvmarg value="-Xrunjdwp:transport=dt_socket,address=${{jpda.address}}"/>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <arg line="@{{args}}"/>
                        </java>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="init-taskdefs">
                <taskdef name="copyfiles" classname="org.netbeans.modules.web.project.ant.CopyFiles" classpath="${{copyfiles.classpath}}"/>
            </target>

            
            <target name="init">
                <xsl:attribute name="depends">pre-init,init-private,init-userdir,init-user,init-project,do-init,post-init,init-check,init-macrodef-property,init-macrodef-javac,init-macrodef-junit,init-macrodef-nbjpda,init-macrodef-debug,init-taskdefs</xsl:attribute>
            </target>

            <xsl:comment>
    ===================
    COMPILATION SECTION
    ===================
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
                <xsl:attribute name="unless">no.dependencies</xsl:attribute>
            </target>

            <xsl:if test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:web-services/ejbjarproject2:web-service|/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:web-service-clients/ejbjarproject2:web-service-client">
				<target name="wscompile-init" depends="init">
					<taskdef name="wscompile" classname="com.sun.xml.rpc.tools.ant.Wscompile">
					  <classpath path="${{wscompile.classpath}}"/>
					</taskdef>
					<mkdir dir="${{classes.dir}}/META-INF/wsdl"/>
					<mkdir dir="${{build.generated.dir}}/wssrc"/>
                    <mkdir dir="${{meta.inf}}/wsdl"/>
				</target>
			</xsl:if>

            <xsl:for-each select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:web-services/ejbjarproject2:web-service">
              <xsl:variable name="wsname">
                <xsl:value-of select="ejbjarproject2:web-service-name"/>
              </xsl:variable>
              <xsl:choose>
              <xsl:when test="ejbjarproject2:from-wsdl">
                <target name="{$wsname}_wscompile" depends="init, wscompile-init">
                  <wscompile import="true" 
                     config="${{src.dir}}/${{{$wsname}.config.name}}"
                     features="${{wscompile.service.{$wsname}.features}}"
                     mapping="${{meta.inf}}/wsdl/${{{$wsname}.mapping}}"
                     classpath="${{wscompile.classpath}}" 
                     nonClassDir="${{classes.dir}}/META-INF/wsdl" 
                     verbose="true" 
                     xPrintStackTrace="true" 
                     xSerializable="true"
                     base="${{build.generated.dir}}/wssrc" 
                     sourceBase="${{src.dir}}" 
                     keep="true" 
                     fork="true" />
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
                     nonClassDir="${{classes.dir}}/META-INF/wsdl"
                     classpath="${{wscompile.classpath}}:${{classes.dir}}"
                     mapping="${{classes.dir}}/META-INF/wsdl/${{{$wsname}.mapping}}"
                     config="${{src.dir}}/${{{$wsname}.config.name}}"
                     features="${{wscompile.service.{$wsname}.features}}">
                  </wscompile>
                </target>
              </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>

            <xsl:for-each select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:web-service-clients/ejbjarproject2:web-service-client">
                <xsl:variable name="wsclientname">
                    <xsl:value-of select="ejbjarproject2:web-service-client-name"/>
                </xsl:variable>
                <xsl:variable name="useimport">
                    <xsl:choose>
                        <xsl:when test="ejbjarproject2:web-service-stub-type">
                            <xsl:value-of select="ejbjarproject2:web-service-stub-type='jsr-109_client'"/>
                        </xsl:when>
                        <xsl:otherwise>true</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="useclient">
                    <xsl:choose>
                        <xsl:when test="ejbjarproject2:web-service-stub-type">
                            <xsl:value-of select="ejbjarproject2:web-service-stub-type='jaxrpc_static_client'"/>
                        </xsl:when>
                        <xsl:otherwise>false</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <target name="{$wsclientname}_client_wscompile" depends="wscompile-init">
                    <property name="config_target" location="${{meta.inf}}/wsdl"/>
                    <copy file="${{meta.inf}}/wsdl/{$wsclientname}-config.xml"
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
                        base="${{classes.dir}}"
                        sourceBase="${{build.generated.dir}}/wssrc"
                        classpath="${{wscompile.classpath}}"
                        mapping="${{classes.dir}}/META-INF/wsdl/{$wsclientname}-mapping.xml"
                        config="${{build.generated.dir}}/wssrc/wsdl/{$wsclientname}-config.xml">
                    </wscompile>
                </target>
            </xsl:for-each>
                        
            <target name="pre-pre-compile">
                <xsl:attribute name="depends">init,deps-jar</xsl:attribute>
                <mkdir dir="${{build.classes.dir}}"/>
                <mkdir dir="${{build.ear.classes.dir}}"/>
            </target>
            
            <target name="pre-compile">
                <xsl:if test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:web-service-clients/ejbjarproject2:web-service-client">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:web-service-clients/ejbjarproject2:web-service-client">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:variable name="wsname2">
                                <xsl:value-of select="ejbjarproject2:web-service-client-name"/>
                            </xsl:variable>
                            <xsl:value-of select="ejbjarproject2:web-service-client-name"/><xsl:text>_client_wscompile</xsl:text>
                        </xsl:for-each>
                    </xsl:attribute>
                </xsl:if>
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="library-inclusion-in-archive" depends="compile">
                <xsl:for-each select="//ejbjarproject2:included-library">
                    <xsl:variable name="included.prop.name">
                        <xsl:value-of select="."/>
                    </xsl:variable>
                    <copyfiles todir="${{build.classes.dir}}">
                        <xsl:attribute name="files">${<xsl:value-of select="$included.prop.name"/>}</xsl:attribute>
                     </copyfiles>
                </xsl:for-each>   
            </target> 

            <target name="library-inclusion-in-manifest" depends="compile">
                <xsl:for-each select="//ejbjarproject2:included-library">
                    <xsl:variable name="included.prop.name">
                        <xsl:value-of select="."/>
                    </xsl:variable>
                    <xsl:variable name="base.prop.name">
                        <xsl:value-of select="concat('included.lib.', $included.prop.name, '')"/>
                    </xsl:variable>
                    <basename>
                        <xsl:attribute name="property"><xsl:value-of select="$base.prop.name"/></xsl:attribute>
                        <xsl:attribute name="file">${<xsl:value-of select="$included.prop.name"/>}</xsl:attribute>
                     </basename>
                     <copyfiles todir="${{dist.ear.dir}}">
                        <xsl:attribute name="files">${<xsl:value-of select="$included.prop.name"/>}</xsl:attribute>
                     </copyfiles>
                </xsl:for-each>   
                
                <manifest file="${{build.ear.classes.dir}}/META-INF/MANIFEST.MF" mode="update">
                    <xsl:if test="//ejbjarproject2:included-library">
                        <attribute>
                            <xsl:attribute name="name">Class-Path</xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:for-each select="//ejbjarproject2:included-library">
                                    <xsl:variable name="base.prop.name">
                                        <xsl:value-of select="concat('${included.lib.', ., '}')"/>
                                    </xsl:variable>
                                    <xsl:if test="position()>1">,</xsl:if>
                                    <xsl:value-of select="$base.prop.name"/>
                                </xsl:for-each>  
                            </xsl:attribute>
                         </attribute>
                     </xsl:if>
                </manifest>
                
            </target>
            
            <target name="do-compile">
                <xsl:attribute name="depends">init,deps-jar,pre-pre-compile,pre-compile</xsl:attribute>
                <xsl:if test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:web-services/ejbjarproject2:web-service">
                    <xsl:comment>For web services, refresh the Tie and SerializerRegistry classes</xsl:comment> 
                    <delete> 
                      <fileset dir="${{classes.dir}}" includes="**/*_Tie.* **/*_SerializerRegistry.*"/>
                    </delete>
                </xsl:if>
                <ejbjarproject2:javac destdir="${{classes.dir}}"/>
                <copy todir="${{classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:source-roots"/>
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                    <fileset dir="${{src.dir}}" excludes="${{build.classes.excludes}}"/>
                    <fileset dir="${{meta.inf}}" includes="**/*.dbschema"/>
                </copy>
                <copy todir="${{classes.dir}}/META-INF">
                  <fileset dir="${{meta.inf}}" excludes="**/*.dbschema ${{meta.inf.excludes}}"/> 
                </copy>
                <xsl:if test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:web-services/ejbjarproject2:web-service">
                    <xsl:comment>For web services, refresh ejb-jar.xml and sun-ejb-jar.xml</xsl:comment>  
                    <copy todir="${{classes.dir}}" overwrite="true"> 
                      <fileset includes="META-INF/ejb-jar.xml META-INF/sun-ejb-jar.xml" dir="${{meta.inf}}"/>
                    </copy>
                 </xsl:if>
            </target>

            <target name="post-compile">
                <xsl:if test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:web-services/ejbjarproject2:web-service">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:web-services/ejbjarproject2:web-service">
                            <xsl:if test="not(ejbjarproject2:from-wsdl)">
                                <xsl:if test="position()!=1 and not(preceding-sibling::ejbjarproject2:web-service/ejbjarproject2:from-wsdl)"><xsl:text>, </xsl:text></xsl:if>
                                <xsl:variable name="wsname2">
                                    <xsl:value-of select="ejbjarproject2:web-service-name"/>
                                </xsl:variable>
                                <xsl:value-of select="ejbjarproject2:web-service-name"/><xsl:text>_wscompile</xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:attribute>
                </xsl:if>
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="compile">
                <xsl:attribute name="depends">init,deps-jar,pre-pre-compile,pre-compile,do-compile,post-compile</xsl:attribute>
                <xsl:attribute name="description">Compile project.</xsl:attribute>
            </target>

            <target name="pre-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="do-compile-single">
                <xsl:attribute name="depends">init,deps-jar,pre-pre-compile</xsl:attribute>
                <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
                <ejbjarproject2:javac>
                    <customize>
                        <include name="${{javac.includes}}"/>
                    </customize>
                </ejbjarproject2:javac>
            </target>

            <target name="post-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="compile-single">
                <xsl:attribute name="depends">init,deps-jar,pre-pre-compile,pre-compile-single,do-compile-single,post-compile-single</xsl:attribute>
            </target>

            <xsl:comment>
    ====================
    DIST BUILDING SECTION
    ====================
    </xsl:comment>

            <target name="pre-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="do-dist-with-manifest">
                <xsl:attribute name="depends">init,compile,pre-dist,library-inclusion-in-archive</xsl:attribute>
                <xsl:attribute name="if">has.custom.manifest</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}" manifest="${{build.classes.dir}}/META-INF/MANIFEST.MF">
                    <fileset dir="${{build.classes.dir}}"/>
                </jar>
            </target>
            
            <target name="do-dist-without-manifest">
                <xsl:attribute name="depends">init,compile,pre-dist,library-inclusion-in-archive</xsl:attribute>
                <xsl:attribute name="unless">has.custom.manifest</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}">
                    <fileset dir="${{build.classes.dir}}"/>
                </jar>
            </target>
            
            <target name="do-dist" depends="init,compile,pre-dist,library-inclusion-in-archive, do-dist-without-manifest, do-dist-with-manifest"/>

            <target name="do-ear-dist">
                <xsl:attribute name="depends">init,compile,pre-dist,library-inclusion-in-manifest</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.ear.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.ear.jar}}" compress="${{jar.compress}}" manifest="${{build.ear.classes.dir}}/META-INF/MANIFEST.MF">
                    <fileset dir="${{build.ear.classes.dir}}"/>
                </jar>
            </target>
            
            <target name="post-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="dist">
                <xsl:attribute name="depends">init,compile,pre-dist,do-dist,post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution (JAR).</xsl:attribute>
            </target>

            <target name="dist-ear">
                <xsl:attribute name="depends">init,compile,pre-dist,do-ear-dist,post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution (JAR) to be packaged into an EAR.</xsl:attribute>
            </target>
            
            <xsl:comment>
    =================
    EXECUTION SECTION
    =================
    </xsl:comment>
    <target name="run">
        <xsl:attribute name="depends">run-deploy</xsl:attribute>
        <xsl:attribute name="description">Deploy to server.</xsl:attribute>
    </target>
            
    <target name="init-deploy">
        <property name="include.jar.manifest" value=""/>
    </target>
    
    <target name="run-deploy">
        <xsl:attribute name="depends">init,init-deploy,compile,library-inclusion-in-archive,dist</xsl:attribute>
        <nbdeploy debugmode="false" forceRedeploy="${{forceRedeploy}}"/>
    </target>
    
    <target name="verify">
        <xsl:attribute name="depends">dist</xsl:attribute>
        <nbverify file="${{dist.jar}}"/>
    </target>
    
    <xsl:comment>
    =================
    DEBUGGING SECTION
    =================
    </xsl:comment>
    <target name="debug">
        <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
        <xsl:attribute name ="depends">init,compile,dist</xsl:attribute>
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <nbdeploy debugmode="true" clientUrlPart="${{client.urlPart}}"/>
        <nbjpdaconnect name="${{name}}" host="${{jpda.host}}" address="${{jpda.address}}" transport="${{jpda.transport}}">
            <classpath>
                <path path="${{debug.classpath}}"/>
            </classpath>
            <sourcepath>
                <path path="${{web.docbase.dir}}"/>
            </sourcepath>
            <xsl:if test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:explicit-platform">
            <bootclasspath>
                <path path="${{platform.bootcp}}"/>
            </bootclasspath>
            </xsl:if>
        </nbjpdaconnect>
    </target>

    <target name="pre-debug-fix">
        <xsl:attribute name="depends">init</xsl:attribute>
        <fail unless="fix.includes">Must set fix.includes</fail>
        <property name="javac.includes" value="${{fix.includes}}.java"/>
    </target>

    <target name="do-debug-fix">
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <xsl:attribute name="depends">init,pre-debug-fix,compile-single</xsl:attribute>
        <j2seproject:nbjpdareload xmlns:j2seproject="http://www.netbeans.org/ns/j2se-project/1"/>
    </target>

    <target name="debug-fix">
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <xsl:attribute name="depends">init,pre-debug-fix,do-debug-fix</xsl:attribute>
    </target>
    
            <xsl:comment>
    ===============
    JAVADOC SECTION
    ===============
    </xsl:comment>

            <target name="javadoc-build">
                <xsl:attribute name="depends">init</xsl:attribute>
                <mkdir dir="${{dist.javadoc.dir}}"/>
                <!-- XXX do an up-to-date check first -->
                <xsl:choose>
                    <xsl:when test="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:explicit-platform">
                        <!-- XXX #46901: <javadoc> does not support an explicit executable -->
                        <ejbjarproject1:property name="platform.javadoc.tmp" value="platforms.${{platform.active}}.javadoc"/>
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
                        <!-- -classpath '' cannot be passed safely on Windows; cf. #46901. -->
                        <condition property="javadoc.classpath.opt" value="&quot;&quot;">
                            <and>
                                <equals arg1="${{javac.classpath}}" arg2=""/>
                                <equals arg1="${{j2ee.platform.classpath}}" arg2=""/>
                            </and>
                        </condition>
                        <path id="javadoc.classpath.temp">
                            <pathelement path="${{javac.classpath}}"/>
                            <pathelement path="${{j2ee.platform.classpath}}"/>
                        </path>
                        <property name="javadoc.classpath.opt" refid="javadoc.classpath.temp"/>
                        <apply executable="${{platform.javadoc}}" failonerror="true" parallel="true">
                            <arg value="-d"/>
                            <arg file="${{dist.javadoc.dir}}"/>
                            <xsl:if test ="not(/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:explicit-platform/@explicit-source-supported ='false')">
                                <arg value="-source"/>
                                <arg value="${{javac.source}}"/>
                            </xsl:if>
                            <arg value="-windowtitle"/>
                            <arg value="${{javadoc.windowtitle}}"/>
                            <arg line="${{javadoc.notree.opt}} ${{javadoc.use.opt}} ${{javadoc.nonavbar.opt}} ${{javadoc.noindex.opt}} ${{javadoc.splitindex.opt}} ${{javadoc.author.opt}} ${{javadoc.version.opt}} ${{javadoc.private.opt}}"/>
                            <arg value="-classpath"/>
                            <arg value="${{javadoc.classpath.opt}}"/>
                            <arg value="-sourcepath"/>
                            <xsl:element name="arg">
                                <xsl:attribute name="path">
                                    <xsl:call-template name="createPath">
                                        <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:source-roots"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                            </xsl:element>
                            <xsl:call-template name="createFilesets">
                                    <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:source-roots"/>
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
                                    <xsl:with-param name="locations" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:source-roots"/>
                                </xsl:call-template>
                            </sourcepath>
                            <xsl:call-template name="createFilesets">
                                    <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:source-roots"/>
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
                <xsl:element name="ejbjarproject2:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="debug">true</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}</xsl:attribute>
                </xsl:element>
                <copy todir="${{build.test.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:test-roots"/>
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
                            <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject2:data/ejbjarproject2:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="debug">true</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}</xsl:attribute>
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
    =======================
    JUNIT DEBUGGING SECTION
    =======================
    </xsl:comment>

            <target name="-debug-start-debuggee-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test</xsl:attribute>
                <fail unless="test.class">Must select one file in the IDE or set test.class</fail>
                <ejbjarproject1:debug classname="junit.textui.TestRunner" classpath="${{debug.test.classpath}}" args="${{test.class}}"/>
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
                <xsl:attribute name="depends">init,pre-debug-fix,compile-test-single</xsl:attribute>
                <ejbjarproject1:nbjpdareload dir="${{build.test.classes.dir}}"/>
            </target>

            <target name="debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,pre-debug-fix,-do-debug-fix-test</xsl:attribute>
            </target>
            
    <xsl:comment>
    ===============
    CLEANUP SECTION
    ===============
    </xsl:comment>

            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-clean'"/>
            </xsl:call-template>

            <target name="do-clean">
                <xsl:attribute name="depends">init</xsl:attribute>
                <delete dir="${{build.dir}}"/>
                <delete dir="${{dist.dir}}"/>
            </target>

            <target name="post-clean">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="clean">
                <xsl:attribute name="depends">init,deps-clean,do-clean,post-clean</xsl:attribute>
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

    <!-- Multiple src roots -->
    
    <xsl:template name="createRootAvailableTest">
        <xsl:param name="roots"/>
        <xsl:param name="propName"/>
        <xsl:element name="condition">
            <xsl:attribute name="property"><xsl:value-of select="$propName"/></xsl:attribute>
            <or>
                <xsl:for-each select="$roots/ejbjarproject2:root">
                    <xsl:element name="available">
			        <xsl:attribute name="file"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
		            </xsl:element>
                </xsl:for-each>
            </or>
        </xsl:element>
    </xsl:template>

    <xsl:template name="createSourcePathValidityTest">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/ejbjarproject2:root">
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
        <xsl:for-each select="$roots/ejbjarproject2:root">
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
        <xsl:for-each select="$locations/ejbjarproject2:root">
            <xsl:element name="pathelement">
			    <xsl:attribute name="location"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
		    </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="createPath">
            <xsl:param name="roots"/>
            <xsl:for-each select="$roots/ejbjarproject2:root">
                <xsl:if test="position() != 1">
                        <xsl:text>:</xsl:text>
                </xsl:if>
                <xsl:text>${</xsl:text>
                <xsl:value-of select="@id"/>
                <xsl:text>}</xsl:text>
            </xsl:for-each>						
    </xsl:template>


</xsl:stylesheet>
