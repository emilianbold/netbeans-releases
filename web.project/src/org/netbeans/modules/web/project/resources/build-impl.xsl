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
                xmlns:web="http://www.netbeans.org/ns/web-project/1"
                xmlns:webproject="http://www.netbeans.org/ns/web-project/1"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                exclude-result-prefixes="xalan p web projdeps">
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

        <xsl:variable name="name" select="/p:project/p:configuration/web:data/web:name"/>
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

            <target name="-do-init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-init-macrodef-property</xsl:attribute>
                <xsl:if test="/p:project/p:configuration/web:data/web:explicit-platform">
                    <webproject:property name="platform.home" value="platforms.${{platform.active}}.home"/>
                    <webproject:property name="platform.bootcp" value="platforms.${{platform.active}}.bootclasspath"/>
                    <webproject:property name="platform.compiler" value="platforms.${{platform.active}}.compile"/>
                    <webproject:property name="platform.javac.tmp" value="platforms.${{platform.active}}.javac"/>
                    <condition property="platform.javac" value="${{platform.home}}/bin/javac">
                        <equals arg1="${{platform.javac.tmp}}" arg2="$${{platforms.${{platform.active}}.javac}}"/>
                    </condition>
                    <property name="platform.javac" value="${{platform.javac.tmp}}"/>
                    <webproject:property name="platform.java.tmp" value="platforms.${{platform.active}}.java"/>
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
                </xsl:if>
                <xsl:if test="/p:project/p:configuration/web:data/web:use-manifest">
                    <fail unless="manifest.file">Must set manifest.file</fail>
                </xsl:if>
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
                <fail unless="src.dir">Must set src.dir</fail>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="build.web.dir">Must set build.web.dir</fail>
                <fail unless="build.generated.dir">Must set build.generated.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.dir">Must set build.classes.dir</fail>
                <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail>
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
                        <property>
                            <xsl:attribute name="name">@{name}</xsl:attribute>
                            <xsl:attribute name="value">${@{value}}</xsl:attribute>
                        </property>
                    </sequential>
                  </macrodef>
            </target>
            
            <target name="-init-macrodef-javac">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">${src.dir}</xsl:attribute>
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
                            <xsl:if test ="not(/p:project/p:configuration/web:data/web:explicit-platform/@explicit-source-supported ='false')">                            
                                <xsl:attribute name="source">${javac.source}</xsl:attribute>
                            </xsl:if>
                            <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/web:data/web:explicit-platform">
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
                            <xsl:if test="/p:project/p:configuration/web:data/web:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <jvmarg line="${{runmain.jvmargs}}"/>
                            <classpath>
                                <path path="${{build.classes.dir}}:${{javac.classpath}}"/>
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
                            <xsl:if test="/p:project/p:configuration/web:data/web:explicit-platform">
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
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
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
                            <xsl:if test="/p:project/p:configuration/web:data/web:explicit-platform">
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
            
            <target name="-init-taskdefs">
                <taskdef name="copyfiles" classname="org.netbeans.modules.web.project.ant.CopyFiles" classpath="${{copyfiles.classpath}}"/>
            </target>
            
            <target name="init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-do-init,-post-init,-init-check,-init-macrodef-property,-init-macrodef-javac,-init-macrodef-java,-init-macrodef-nbjpda,-init-macrodef-debug,-init-taskdefs</xsl:attribute>
            </target>

            <xsl:comment>
    ======================
    COMPILATION SECTION
    ======================
    </xsl:comment>

            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-jar'"/>
                <xsl:with-param name="type" select="'jar'"/>
            </xsl:call-template>

            <target name="-pre-pre-compile">
                <xsl:attribute name="depends">init,deps-jar</xsl:attribute>
                <mkdir dir="${{build.classes.dir}}"/>
            </target>

            <target name="-pre-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-do-compile">
                <xsl:attribute name="depends">init, deps-jar, -pre-pre-compile, -pre-compile</xsl:attribute>
                <webproject:javac xmlns:webproject="http://www.netbeans.org/ns/web-project/1"/>
                <copy todir="${{build.classes.dir}}">
                    <fileset dir="${{src.dir}}" excludes="${{build.classes.excludes}}"/>
                </copy>
                <copy todir="${{build.web.dir}}">
                  <fileset excludes="${{build.web.excludes}}" dir="${{web.docbase.dir}}"/>
                </copy>
                <xsl:for-each select="/p:project/p:configuration/web:data/web:web-module-libraries/web:library[web:path-in-war]">
                    <xsl:variable name="copyto" select=" web:path-in-war"/>
                    <xsl:variable name="libfile" select="web:file"/>
                    <copyfiles todir="${{build.web.dir}}/{$copyto}" files="{$libfile}"/>
                </xsl:for-each>
                <xsl:for-each select="/p:project/p:configuration/web:data/web:web-module-additional-libraries/web:library[web:path-in-war]">
                    <xsl:variable name="copyto" select=" web:path-in-war"/>
                    <xsl:variable name="libfile" select="web:file"/>
                    <copyfiles todir="${{build.web.dir}}/{$copyto}" files="{$libfile}"/>
                </xsl:for-each>
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
                <webproject:javac xmlns:webproject="http://www.netbeans.org/ns/web-project/1">
                    <customize>
                        <patternset includes="${{javac.includes}}"/>
                    </customize>
                </webproject:javac>
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
                    <arg file="${{basedir}}/${{build.web.dir}}"/>
                    <arg value="-d"/>
                    <arg file="${{basedir}}/${{build.generated.dir}}/src"/>
                    <arg value="-die1"/>
                    <classpath path="${{java.home}}/../lib/tools.jar:${{copyfiles.classpath}}:${{jspc.classpath}}"/> 
                </java>
                <mkdir dir="${{build.generated.dir}}/classes"/>
                <webproject:javac xmlns:webproject="http://www.netbeans.org/ns/web-project/1"
                    srcdir="${{build.generated.dir}}/src"
                    destdir="${{build.generated.dir}}/classes"
                    classpath="${{javac.classpath}}:${{build.classes.dir}}:${{jspc.classpath}}"/>

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
                    <arg file="${{basedir}}/${{build.web.dir}}"/>
                    <arg value="-d"/>
                    <arg file="${{basedir}}/${{build.generated.dir}}/src"/>
                    <arg value="-die1"/>
                    <arg value="-jspc.files"/>
                    <arg path="${{jsp.includes}}"/>
                    <classpath path="${{java.home}}/../lib/tools.jar:${{copyfiles.classpath}}:${{jspc.classpath}}"/> 
                </java>
                <mkdir dir="${{build.generated.dir}}/classes"/>
                <webproject:javac xmlns:webproject="http://www.netbeans.org/ns/web-project/1"
                    srcdir="${{build.generated.dir}}/src"
                    destdir="${{build.generated.dir}}/classes"
                    classpath="${{javac.classpath}}:${{build.classes.dir}}:${{jspc.classpath}}">
                    <customize>
                        <patternset includes="${{javac.jsp.includes}}"/>
                    </customize>
                </webproject:javac>
                <!--
                <webproject:javac xmlns:webproject="http://www.netbeans.org/ns/web-project/1">
                    <xsl:with-param name="srcdir" select="'${{build.generated.dir}}/src'"/>
                    <xsl:with-param name="destdir" select="'${{build.generated.dir}}/classes'"/>
                    <xsl:with-param name="classpath" select="'${{javac.classpath}}:${{build.classes.dir}}'"/>
                    <xsl:with-param name="classpath" select="'${{javac.classpath}}:${{build.classes.dir}}:${{jspc.classpath}}'"/>
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

            <target name="do-dist">
                <xsl:attribute name="depends">init,compile,compile-jsps,-pre-dist</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.war}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.war}}" compress="${{jar.compress}}">
                    <fileset dir="${{build.web.dir}}"/>
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
                <xsl:attribute name="depends">init,compile,compile-jsps,-do-compile-single-jsp</xsl:attribute>
                <nbdeploy debugmode="false" clientUrlPart="${{client.urlPart}}" forceRedeploy="${{forceRedeploy}}"/>
            </target>
            
            <target name="run-display-browser" if="do.display.browser">
                <xsl:attribute name="depends">run-deploy</xsl:attribute>
                <nbbrowse url="${{client.url}}"/>
            </target>
            
            <target name="run-main">
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <webproject:java classname="${{run.class}}"/>
            </target>

            
            <xsl:comment>
    ======================
    DEBUGGING SECTION
    ======================
    </xsl:comment>
    <target name="debug">
        <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
        <xsl:attribute name ="depends">init,compile,compile-jsps,-do-compile-single-jsp</xsl:attribute>
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <nbdeploy debugmode="true" clientUrlPart="${{client.urlPart}}"/>
        <nbjpdaconnect name="${{name}}" host="${{jpda.host}}" address="${{jpda.address}}" transport="${{jpda.transport}}">
            <classpath>
                <path path="${{debug.classpath}}"/>
            </classpath>
            <sourcepath>
                <path path="${{web.docbase.dir}}"/>
            </sourcepath>
            <xsl:if test="/p:project/p:configuration/web:data/web:explicit-platform">
            <bootclasspath>
                <path path="${{platform.bootcp}}"/>
            </bootclasspath>
            </xsl:if>
        </nbjpdaconnect>
        <nbbrowse url="${{client.url}}"/>
    </target>
    
    <target name="debug-single">
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <xsl:attribute name="depends">init,compile,compile-jsps,-do-compile-single-jsp,debug</xsl:attribute>
    </target>

    <target name="-debug-start-debugger">
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <xsl:attribute name="depends">init</xsl:attribute>
        <webproject:nbjpdastart name="${{debug.class}}"/>
    </target>

    <target name="-debug-start-debuggee-single">
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <xsl:attribute name="depends">init,compile-single</xsl:attribute>
        <fail unless="debug.class">Must select one file in the IDE or set debug.class</fail>
        <webproject:debug classname="${{debug.class}}"/>
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
        <webproject:nbjpdareload xmlns:webproject="http://www.netbeans.org/ns/web-project/1"/>
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
                    <xsl:when test="/p:project/p:configuration/web:data/web:explicit-platform">
                        <!-- XXX #46901: <javadoc> does not support an explicit executable -->
                        <webproject:property name="platform.javadoc.tmp" value="platforms.${{platform.active}}.javadoc"/>
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
                        <condition property="javadoc.classpath.opt" value="-classpath ${{javac.classpath}}">
                            <!-- -classpath '' cannot be passed safely on Windows; cf. #46901. -->
                            <not>
                                <equals arg1="${{javac.classpath}}" arg2=""/>
                            </not>
                        </condition>
                        <property name="javadoc.classpath.opt" value=""/>
                        <apply executable="${{platform.javadoc}}" failonerror="true" parallel="true">
                            <arg value="-d"/>
                            <arg file="${{dist.javadoc.dir}}"/>
                            <xsl:if test ="not(/p:project/p:configuration/web:data/web:explicit-platform/@explicit-source-supported ='false')">
                                <arg value="-source"/>
                                <arg value="${{javac.source}}"/>
                            </xsl:if>
                            <arg value="-windowtitle"/>
                            <arg value="${{javadoc.windowtitle}}"/>
                            <arg line="${{javadoc.notree.opt}} ${{javadoc.use.opt}} ${{javadoc.nonavbar.opt}} ${{javadoc.noindex.opt}} ${{javadoc.splitindex.opt}} ${{javadoc.author.opt}} ${{javadoc.version.opt}} ${{javadoc.private.opt}} ${{javadoc.classpath.opt}}"/>
                            <arg value="-sourcepath"/>
                            <arg file="${{src.dir}}"/>
                            <fileset dir="${{src.dir}}" includes="**/*.java"/>
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
                            <classpath>
                                <path path="${{javac.classpath}}"/>
                            </classpath>
                            <sourcepath>
                                <pathelement location="${{src.dir}}"/>
                            </sourcepath>
                            <fileset dir="${{src.dir}}"/>                
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
    ======================
    CLEANUP SECTION
    ======================
    </xsl:comment>

            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-clean'"/>
            </xsl:call-template>

            <target name="do-clean">
                <xsl:attribute name="depends">init</xsl:attribute>
                <delete dir="${{build.dir}}"/>
                <delete dir="${{dist.dir}}"/>
                <!-- XXX explicitly delete all build.* and dist.* dirs in case they are not subdirs -->
                <!--
                <delete dir="${{build.generated.dir}}"/>
                <delete dir="${{build.web.dir}}"/> 
                -->
            </target>

            <target name="-post-clean">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="clean">
                <xsl:attribute name="depends">init,deps-clean,do-clean,-post-clean</xsl:attribute>
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
        <target name="{$targetname}">
            <xsl:attribute name="depends">init</xsl:attribute>
            <xsl:attribute name="unless">no.deps</xsl:attribute>
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
                    <!-- XXX #43624: cannot use inline attr on JDK 1.5 -->
                    <xsl:attribute name="dir">${project.<xsl:value-of select="$subproj"/>}<xsl:value-of select="$scriptdirslash"/></xsl:attribute>
                    <xsl:if test="$scriptfile != 'build.xml'">
                        <xsl:attribute name="antfile">
                            <xsl:value-of select="$scriptfile"/>
                        </xsl:attribute>
                    </xsl:if>
                </ant>
            </xsl:for-each>
        </target>
    </xsl:template>

</xsl:stylesheet>
