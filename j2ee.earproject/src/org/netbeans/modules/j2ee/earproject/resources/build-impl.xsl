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
                xmlns:ear="http://www.netbeans.org/ns/j2ee-earproject/1"
                xmlns:ear2="http://www.netbeans.org/ns/j2ee-earproject/2"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                exclude-result-prefixes="xalan p ear projdeps">
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

        <xsl:variable name="name" select="/p:project/p:configuration/ear2:data/ear2:name"/>
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
                <xsl:attribute name="depends">pre-init,init-private,init-userdir,init-user,init-project</xsl:attribute>
                <xsl:if test="/p:project/p:configuration/ear2:data/ear2:explicit-platform">
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
                <xsl:if test="/p:project/p:configuration/ear2:data/ear2:use-manifest">
                    <fail unless="manifest.file">Must set manifest.file</fail>
                </xsl:if>
                <condition property="no.javadoc.preview">
                    <isfalse value="${{javadoc.preview}}"/>
                </condition>
                <condition property="do.compile.jsps">
                    <istrue value="${{compile.jsps}}"/>
                </condition>
                <condition property="do.display.browser">
                    <and>
                        <istrue value="${{display.browser}}"/>
                        <contains string="${{client.module.uri}}" substring=".war"/>
                    </and>
                </condition>
                <available property="has.custom.manifest" file="${{meta.inf}}/MANIFEST.MF"/>
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
                <fail unless="src.dir">Must set src.dir</fail>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="build.archive.dir">Must set build.archive.dir</fail>
                <fail unless="build.generated.dir">Must set build.generated.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.dir">Must set build.classes.dir</fail>
                <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail>
                <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>
                <fail unless="dist.jar">Must set dist.jar</fail>
            </target>

            <target name="init-macrodef-javac">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-earproject/1</xsl:attribute>
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
                            <xsl:attribute name="source">${javac.source}</xsl:attribute>
                            <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/ear2:data/ear2:explicit-platform">
                                <xsl:attribute name="fork">yes</xsl:attribute>
                                <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                            </xsl:if>
                            <xsl:attribute name="includeantruntime">false</xsl:attribute>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <customize/>
                        </javac>
                    </sequential>
                 </macrodef>
            </target>

            <target name="init-macrodef-nbjpda">
                <macrodef>
                    <xsl:attribute name="name">nbjpdastart</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-earproject/1</xsl:attribute>
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
                            <xsl:if test="/p:project/p:configuration/ear2:data/ear2:explicit-platform">
                                <bootclasspath>
                                    <path path="${{platform.bootcp}}"/>
                                </bootclasspath>
                            </xsl:if>
                        </nbjpdastart>
                    </sequential>
                </macrodef>
                <macrodef>
                    <xsl:attribute name="name">nbjpdareload</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-earproject/1</xsl:attribute>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-earproject/1</xsl:attribute>
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
                            <xsl:if test="/p:project/p:configuration/ear2:data/ear2:explicit-platform">
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
                <xsl:attribute name="depends">pre-init,init-private,init-userdir,init-user,init-project,do-init,post-init,init-check,init-macrodef-javac,init-macrodef-nbjpda,init-macrodef-debug,init-taskdefs</xsl:attribute>
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

            <!--<xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-war'"/>
                <xsl:with-param name="type" select="'j2ee_ear_archive'"/>
            </xsl:call-template>-->

            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-j2ee-archive'"/>
                <xsl:with-param name="type" select="'j2ee_ear_archive'"/>
            </xsl:call-template>

            <target name="pre-pre-compile">
                <xsl:attribute name="depends">init,deps-jar,deps-j2ee-archive</xsl:attribute>
                <mkdir dir="${{build.classes.dir}}"/>
            </target>

            <target name="pre-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="do-compile">
                <xsl:attribute name="depends">init,deps-jar,pre-pre-compile,pre-compile</xsl:attribute>
                <earproject:javac xmlns:earproject="http://www.netbeans.org/ns/j2ee-earproject/1"/>
                
                <copy todir="${{build.dir}}/META-INF">
                  <fileset dir="${{meta.inf}}"/>
                </copy>
                
                <xsl:for-each select="/p:project/p:configuration/ear2:data/ear2:web-module-additional-libraries/ear2:library[ear2:path-in-war]">
                    <xsl:variable name="copyto" select=" ear2:path-in-war"/>
                    <xsl:if test="//ear2:web-module-additional-libraries/ear2:library[@files]">
                      <xsl:if test="@files &gt; 1">
                        <xsl:call-template name="copyIterateFiles">
                            <xsl:with-param name="files" select="@files"/>
                            <xsl:with-param name="target" select="concat('${build.dir}/',$copyto)"/>
                            <xsl:with-param name="libfile" select="ear2:file"/>
                        </xsl:call-template>
                      </xsl:if>
                      <xsl:if test="@files = 1">
                            <xsl:variable name="target" select="concat('${build.dir}/',$copyto)"/>
                            <xsl:variable name="libfile" select="ear2:file"/>
                            <copy file="{$libfile}" todir="{$target}"/>
                      </xsl:if>
                    </xsl:if>
                    <xsl:if test="//ear2:web-module-additional-libraries/ear2:library[@dirs]">
                      <xsl:if test="@dirs &gt; 1">
                        <xsl:call-template name="copyIterateDirs">
                            <xsl:with-param name="files" select="@dirs"/>
                            <xsl:with-param name="target" select="concat('${build.dir}/',$copyto)"/>
                            <xsl:with-param name="libfile" select="ear2:file"/>
                        </xsl:call-template>
                      </xsl:if>
                      <xsl:if test="@dirs = 1">
                            <xsl:variable name="target" select="concat('${build.dir}/',$copyto)"/>
                            <xsl:variable name="libfile" select="ear2:file"/>
                            <copy todir="{$target}">
                                <fileset dir="{$libfile}" includes="**/*"/>
                            </copy>
                      </xsl:if>
                    </xsl:if>
                </xsl:for-each>
                
            </target>

            <target name="post-compile">
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
                <earproject:javac xmlns:earproject="http://www.netbeans.org/ns/j2ee-earproject/1">
                    <customize>
                        <include name="${{javac.includes}}"/>
                    </customize>
                </earproject:javac>
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

            <target name="do-dist-without-manifest">
                <xsl:attribute name="depends">init,compile,pre-dist</xsl:attribute>
                <xsl:attribute name="unless">has.custom.manifest</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}">
                    <fileset dir="${{build.dir}}"/>
                </jar>
            </target>

            <target name="do-dist-with-manifest">
                <xsl:attribute name="depends">init,compile,pre-dist</xsl:attribute>
                <xsl:attribute name="if">has.custom.manifest</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}" manifest="${{meta.inf}}/MANIFEST.MF">
                    <fileset dir="${{build.dir}}"/>
                </jar>
            </target>
            
            <target name="post-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="dist">
                <xsl:attribute name="depends">init,compile,pre-dist,do-dist-without-manifest,do-dist-with-manifest,post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution (JAR).</xsl:attribute>
            </target>

            <xsl:comment>
    =================
    EXECUTION SECTION
    =================
    </xsl:comment>
    <target name="run">
        <xsl:attribute name="depends">run-deploy,run-display-browser</xsl:attribute>
        <xsl:attribute name="description">Deploy to server.</xsl:attribute>
    </target>
            
    <target name="run-deploy">
        <xsl:attribute name="depends">dist</xsl:attribute>
        <nbdeploy debugmode="false" forceRedeploy="${{forceRedeploy}}" clientUrlPart="${{client.urlPart}}" clientModuleUri="${{client.module.uri}}"/>
    </target>
    
    <target name="verify">
        <xsl:attribute name="depends">dist</xsl:attribute>
        <nbverify file="${{dist.jar}}"/>
    </target>
    
            <target name="run-display-browser" if="do.display.browser">
                <xsl:attribute name="depends">run-deploy</xsl:attribute>
                <nbbrowse url="${{client.url}}"/>
            </target>
    <xsl:comment>
    =================
    DEBUGGING SECTION
    =================
    </xsl:comment>
    <target name="debug">
        <xsl:attribute name="depends">run-debug,run-display-browser</xsl:attribute>
        <xsl:attribute name="description">Deploy to server.</xsl:attribute>
    </target>
    <target name="run-debug">
        <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
        <xsl:attribute name ="depends">dist</xsl:attribute>
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <nbdeploy debugmode="true" clientUrlPart="${{client.urlPart}}" clientModuleUri="${{client.module.uri}}"/>
        <nbjpdaconnect name="${{name}}" host="${{jpda.host}}" address="${{jpda.address}}" transport="${{jpda.transport}}">
            <classpath>
                <path path="${{debug.classpath}}"/>
            </classpath>
            <sourcepath>
                <path path="${{web.docbase.dir}}"/>
            </sourcepath>
            <xsl:if test="/p:project/p:configuration/ear2:data/ear2:explicit-platform">
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
                <javadoc destdir="${{dist.javadoc.dir}}" source="${{javac.source}}"
                         notree="${{javadoc.notree}}"
                         use="${{javadoc.use}}"
                         nonavbar="${{javadoc.nonavbar}}"
                         noindex="${{javadoc.noindex}}"
                         splitindex="${{javadoc.splitindex}}"
                         author="${{javadoc.author}}"
                         version="${{javadoc.version}}"
                         windowtitle="${{javadoc.windowtitle}}"
                         private="${{javadoc.private}}" >
                         <!-- encoding="${{javadoc.encoding}}" -->
                    <classpath>
                        <path path="${{javac.classpath}}"/>
                    </classpath>
                    <sourcepath>
                        <pathelement location="${{src.dir}}"/>
                    </sourcepath>
                    <xsl:if test="/p:project/p:configuration/ear2:data/ear2:explicit-platform">
                        <bootclasspath>
                            <path path="${{platform.bootcp}}"/>
                        </bootclasspath>
                    </xsl:if>
                    <fileset dir="${{src.dir}}"/>
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
                <delete dir="${{build.dir}}"/>
            </target>

            <target name="post-clean">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="clean">
                <xsl:attribute name="depends">init,deps-clean,do-clean,post-clean</xsl:attribute>
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
                <ant target="{$subtarget}" inheritall="false" antfile="${{project.{$subproj}}}/{$script}">
                    <property name="dist.ear.dir" location="${{build.dir}}"/>
                </ant>
            </xsl:for-each>
        </target>
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
