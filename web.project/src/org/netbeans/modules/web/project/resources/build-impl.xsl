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
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                exclude-result-prefixes="xalan p web projdeps">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

<xsl:comment> *** GENERATED FROM project.xml - DO NOT EDIT *** </xsl:comment>

<xsl:variable name="name" select="/p:project/p:name"/>
<project name="{$name}-impl" default="build" basedir="..">
    <target name="init">
        <!--
        <xsl:variable name="cp">
            <xsl:for-each select="/p:project/p:configuration/web:data/web:web-module-libraries/web:library">
                <xsl:value-of select="web:file"/>
                <xsl:text>;</xsl:text>
            </xsl:for-each>
        </xsl:variable>
        <property name="javac.classpath" value="{$cp}"/>
        -->
        <property file="nbproject/private/private.properties"/>
        <property name="user.properties.file" location="${{netbeans.user}}/build.properties"/>
        <property file="${{user.properties.file}}"/>
        <property file="nbproject/project.properties"/>
        <xsl:if test="/p:project/p:configuration/web:data/web:explicit-platform">
            <!-- XXX Ugly but Ant does not yet support recursive property evaluation: -->
            <property name="file.tmp" location="${{java.io.tmpdir}}/platform.properties"/>
            <echo file="${{file.tmp}}">
                platform.home=$${platforms.${platform.active}.home}
                platform.bootcp=$${platforms.${platform.active}.bootclasspath}
                build.compiler=$${platforms.${platform.active}.compiler}
            </echo>
            <property file="${{file.tmp}}"/>
            <delete file="${{file.tmp}}"/>
            <fail unless="platform.home">Must set platform.home</fail>
            <fail unless="platform.bootcp">Must set platform.bootcp</fail>
        </xsl:if>
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
        <xsl:if test="/p:project/p:configuration/web:data/web:use-manifest">
            <fail unless="manifest.file">Must set manifest.file</fail>
        </xsl:if>

        <condition property="no.deps">
            <istrue value="${{no.dependencies}}"/>
        </condition>
        <condition property="no.javadoc.preview">
            <isfalse value="${{javadoc.preview}}"/>
        </condition>
        <taskdef name="copyfiles" classname="org.netbeans.modules.web.project.ant.CopyFiles" classpath="${{libs.copyfiles.classpath}}"/>
        <condition property="do.compile.jsps">
            <istrue value="${{compile.jsps}}"/>
        </condition>
    </target>

    <xsl:call-template name="deps.target">
        <xsl:with-param name="targetname" select="'deps-jar'"/>
        <xsl:with-param name="type" select="'jar'"/>
    </xsl:call-template>

    <target name="compile" depends="init,deps-jar">
        <mkdir dir="${{build.classes.dir}}"/>
        <copy todir="${{build.web.dir}}">
          <fileset excludes="WEB-INF/classes/**" dir="${{web.docbase.dir}}"/>
        </copy>
        <xsl:choose>
            <xsl:when test="/p:project/p:configuration/web:data/web:explicit-platform">
                <javac srcdir="${{src.dir}}" destdir="${{build.classes.dir}}" debug="${{javac.debug}}" deprecation="${{javac.deprecation}}" target="${{javac.target}}" source="${{javac.source}}" includeantruntime="false" fork="yes" executable="${{platform.home}}/bin/javac">
                    <classpath>
                        <path path="${{javac.classpath}}"/>
                    </classpath>
                </javac>
            </xsl:when>
            <xsl:otherwise>
                <javac srcdir="${{src.dir}}" destdir="${{build.classes.dir}}" debug="${{javac.debug}}" deprecation="${{javac.deprecation}}" target="${{javac.target}}" source="${{javac.source}}" includeantruntime="false">
                    <classpath>
                        <path path="${{javac.classpath}}"/>
                    </classpath>
                </javac>
            </xsl:otherwise>
        </xsl:choose>
        <copy todir="${{build.classes.dir}}">
            <fileset dir="${{src.dir}}" excludes="${{build.classes.excludes}}"/>
        </copy>
        <!-- copy libraries -->
        <xsl:for-each select="/p:project/p:configuration/web:data/web:web-module-libraries/web:library[web:path-in-war]">
            <xsl:variable name="copyto" select=" web:path-in-war"/>
            <xsl:variable name="libfile" select="web:file"/>
            <copyfiles todir="${{build.web.dir}}/{$copyto}" files="{$libfile}"/>
        </xsl:for-each>
    </target>

    <target name="compile-jsps" depends="compile" if="do.compile.jsps"> 

      <taskdef classname="org.apache.jasper.JspC" name="jasper2" > 
        <classpath path="${{jspc.classpath}}"/> 
      </taskdef> 

      <mkdir dir="${{build.generated.dir}}/src"/>
      <jasper2
             validateXml="false" 
             uriroot="${{basedir}}/${{build.web.dir}}" 
             outputDir="${{basedir}}/${{build.generated.dir}}/src" /> 
             
       <mkdir dir="${{build.generated.dir}}/classes"/>
        <xsl:choose>
            <xsl:when test="/p:project/p:configuration/web:data/web:explicit-platform">
                <javac srcdir="${{build.generated.dir}}/src" destdir="${{build.generated.dir}}/classes" debug="${{javac.debug}}" deprecation="${{javac.deprecation}}" target="${{javac.target}}" source="${{javac.source}}" includeantruntime="false" fork="yes" executable="${{platform.home}}/bin/javac">
                    <classpath>
                        <path path="${{javac.classpath}}:${{build.classes.dir}}"/>
                        <path path="${{jspc.classpath}}"/>
                    </classpath>
                </javac>
            </xsl:when>
            <xsl:otherwise>
                <javac srcdir="${{build.generated.dir}}/src" destdir="${{build.generated.dir}}/classes" debug="${{javac.debug}}" deprecation="${{javac.deprecation}}" target="${{javac.target}}" source="${{javac.source}}" includeantruntime="false">
                    <classpath>
                        <path path="${{javac.classpath}}:${{build.classes.dir}}"/>
                        <path path="${{jspc.classpath}}"/>
                    </classpath>
                </javac>
            </xsl:otherwise>
        </xsl:choose>
    </target> 


    <target name="compile-single" depends="init,deps-jar">
        <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>

        <!-- XXX this block of <condition>s is pretty ugly; better to use a templated target, or <macrodef> -->
        <property name="task-tmp.src.dir" value="${{src.dir}}"/>
                                
        <property name="task-tmp.out.dir" value="${{build.classes.dir}}"/> 
        
        <property name="task-tmp.classpath" value="${{javac.classpath}}"/>
        
        <property name="task-tmp.debug" value="${{javac.debug}}"/>
        
        <property location="${{task-tmp.src.dir}}" name="tmp-task.src.dir.absolute"/>        
        
        <mkdir dir="${{task-tmp.out.dir}}"/>

        <xsl:choose>
            <xsl:when test="/p:project/p:configuration/web:data/web:explicit-platform">
                <javac srcdir="${{task-tmp.src.dir}}" destdir="${{task-tmp.out.dir}}"
                    debug="${{task-tmp.debug}}" deprecation="${{javac.deprecation}}" target="${{javac.target}}"
                    source="${{javac.source}}" includes="${{javac.includes}}" includeantruntime="false"
                    fork="yes" executable="${{platform.home}}/bin/javac">
                    <classpath>
                        <path path="${{task-tmp.classpath}}"/>
                    </classpath>
                </javac>
            </xsl:when>
            <xsl:otherwise>
                <javac srcdir="${{task-tmp.src.dir}}" destdir="${{task-tmp.out.dir}}"
                    debug="${{task-tmp.debug}}" deprecation="${{javac.deprecation}}" target="${{javac.target}}"
                    source="${{javac.source}}" includes="${{javac.includes}}" includeantruntime="false">
                    <classpath>
                        <path path="${{task-tmp.classpath}}"/>
                    </classpath>
                </javac>
            </xsl:otherwise>
        </xsl:choose>        
    </target>
    
    <target name="dist" depends="init,compile,compile-jsps">
        <dirname property="dist.jar.dir" file="${{dist.war}}"/>
        <mkdir dir="${{dist.jar.dir}}"/>
        <jar jarfile="${{dist.war}}" compress="${{jar.compress}}">
            <fileset dir="${{build.web.dir}}"/>
        </jar>
    </target>

    <target name="run" depends="init,compile,compile-jsps">
        <nbdeploy debugmode="false" clientUrlPart="${{client.urlPart}}">
<!--            <xsl:call-template name="run-java-body"/>-->
        </nbdeploy>
        <nbbrowse url="${{client.url}}"/>
    </target>

<!--    <xsl:template name="run-java-body">
        <classpath>
            <path path="${{run.classpath}}"/>
        </classpath>
        <arg line="${{application.args}}"/>
    </xsl:template>

    <target name="debug" depends="init,compile,do-debug">
    </target>
-->

    <target name="debug-nb" depends="init,compile,compile-jsps" if="netbeans.home">
        <nbdeploy debugmode="true" clientUrlPart="${{client.urlPart}}">
<!--        <xsl:call-template name="debug-java-body"/>-->
        </nbdeploy>
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

    <target name="debug-fix-nb" depends="init" if="netbeans.home">
        <tstamp>
            <format property="before.compile" pattern="MM/DD/yyyy hh:mm aa" locale="en"/>
        </tstamp>
        <antcall target="compile-single"/>
        <nbjpdareload>
            <fileset dir="${{build.classes.dir}}" includes="**/*.class">
                <date datetime="${{before.compile}}" when="after"/>
            </fileset>
        </nbjpdareload>
    </target>
    
    <target name="javadoc" depends="init">
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
            <xsl:if test="/p:project/p:configuration/web:data/web:explicit-platform">
            <bootclasspath>
                <path path="${{platform.bootcp}}"/>
            </bootclasspath>
            </xsl:if>
            <fileset dir="${{src.dir}}"/>
        </javadoc>
    </target>
    
    <target name="javadoc-nb" depends="init,javadoc" if="netbeans.home" unless="no.javadoc.preview">
        <nbbrowse file="${{dist.javadoc.dir}}/index.html"/>
    </target>

    <xsl:call-template name="deps.target">
        <xsl:with-param name="targetname" select="'deps-clean'"/>
    </xsl:call-template>

    <target name="clean" depends="init,deps-clean">
        <delete dir="${{build.dir}}"/>
        <delete dir="${{dist.dir}}"/>
        <!-- XXX explicitly delete all build.* and dist.* dirs in case they are not subdirs -->
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
        <target name="{$targetname}" depends="init" unless="no.deps">
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
                <ant dir="${{project.{$subproj}}}{$scriptdirslash}" target="{$subtarget}" inheritall="false">
                    <xsl:if test="$scriptfile != 'build.xml'">
                        <xsl:attribute name="antfile">
                            <xsl:value-of select="$scriptfile"/>
                        </xsl:attribute>
                    </xsl:if>
                </ant>
            </xsl:for-each>
        </target>
    </xsl:template>

    <xsl:template name="debug-java-body">
        <jvmarg value="-Xdebug"/>
        <jvmarg value="-Xnoagent"/>
        <jvmarg value="-Djava.compiler=none"/>
        <jvmarg value="-Xrunjdwp:transport=dt_socket,address=${{jpda.address}}"/>
        <classpath>
            <path path="${{debug.classpath}}"/>
        </classpath>
        <arg line="${{application.args}}"/>
    </xsl:template>

</xsl:stylesheet>
