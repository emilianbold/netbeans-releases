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
                xmlns:j2se="http://www.netbeans.org/ns/j2se-project/1"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                exclude-result-prefixes="xalan p j2se projdeps">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

<xsl:comment> *** GENERATED FROM project.xml - DO NOT EDIT *** </xsl:comment>

<xsl:variable name="name" select="/p:project/p:name"/>
<project name="{$name}-impl" default="build" basedir="..">

    <target name="init">
        <property file="nbproject/private/private.properties"/>
        <property name="user.properties.file" location="${{netbeans.user}}/build.properties"/>
        <property file="${{user.properties.file}}"/>
        <property file="nbproject/project.properties"/>
        <xsl:if test="/p:project/p:configuration/j2se:data/j2se:explicit-platform">
            <!--Setting java and javac default location -->
            <property name="platforms.${{platform.active}}.javac" value="${{platform.home}}/bin/javac"/>
            <property name="platforms.${{platform.active}}.java" value="${{platform.home}}/bin/java"/>
            <!-- XXX Ugly but Ant does not yet support recursive property evaluation: -->            
            <property name="file.tmp" location="${{java.io.tmpdir}}/platform.properties"/>
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
        <!-- XXX XSLT 2.0 would make it possible to use a for-each here -->
        <!-- Note that if the properties were defined in project.xml that would be easy -->
        <!-- But required props should be defined by the AntBasedProjectType, not stored in each project -->
        <fail unless="src.dir">Must set src.dir</fail>
        <fail unless="test.src.dir">Must set test.src.dir</fail>
        <fail unless="build.dir">Must set build.dir</fail>
        <fail unless="dist.dir">Must set dist.dir</fail>
        <fail unless="build.classes.dir">Must set build.classes.dir</fail>
        <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail>
        <fail unless="build.test.classes.dir">Must set build.test.classes.dir</fail>
        <fail unless="build.test.results.dir">Must set build.test.results.dir</fail>
        <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>
        <fail unless="dist.jar">Must set dist.jar</fail>
        <xsl:if test="/p:project/p:configuration/j2se:data/j2se:use-manifest">
            <fail unless="manifest.file">Must set manifest.file</fail>
        </xsl:if>
        <available property="have.tests" file="${{test.src.dir}}"/>
        <condition property="netbeans.home+have.tests">
            <and>
                <isset property="netbeans.home"/>
                <isset property="have.tests"/>
            </and>
        </condition>
        <condition property="no.deps">
            <istrue value="${{no.dependencies}}"/>
        </condition>
        <condition property="no.javadoc.preview">
            <isfalse value="${{javadoc.preview}}"/>
        </condition>
    </target>

    <xsl:call-template name="deps.target">
        <xsl:with-param name="targetname" select="'deps-jar'"/>
        <xsl:with-param name="type" select="'jar'"/>
    </xsl:call-template>

    <target name="compile" depends="init,deps-jar">
        <mkdir dir="${{build.classes.dir}}"/>
        <xsl:choose>
            <xsl:when test="/p:project/p:configuration/j2se:data/j2se:explicit-platform">
                <javac srcdir="${{src.dir}}" destdir="${{build.classes.dir}}" debug="${{javac.debug}}" deprecation="${{javac.deprecation}}" source="${{javac.source}}" includeantruntime="false" fork="yes" executable="${{platform.javac}}">
                    <classpath>
                        <path path="${{javac.classpath}}"/>
                    </classpath>
                </javac>
            </xsl:when>
            <xsl:otherwise>
                <javac srcdir="${{src.dir}}" destdir="${{build.classes.dir}}" debug="${{javac.debug}}" deprecation="${{javac.deprecation}}" source="${{javac.source}}" includeantruntime="false">
                    <classpath>
                        <path path="${{javac.classpath}}"/>
                    </classpath>
                </javac>
            </xsl:otherwise>
        </xsl:choose>
        <copy todir="${{build.classes.dir}}">
            <fileset dir="${{src.dir}}" excludes="${{build.classes.excludes}}"/>
        </copy>
    </target>

    <target name="compile-single" depends="init,deps-jar">
        <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>

        <!-- XXX this block of <condition>s is pretty ugly; better to use a templated target, or <macrodef> -->
        <condition property="task-tmp.src.dir" value="${{test.src.dir}}">
            <istrue value="${{is.test}}"/>
        </condition>
        <property name="task-tmp.src.dir" value="${{src.dir}}"/>
                                
        <condition property="task-tmp.out.dir" value="${{build.test.classes.dir}}">
            <istrue value="${{is.test}}"/>
        </condition>
        <property name="task-tmp.out.dir" value="${{build.classes.dir}}"/> 
        
        <condition property="task-tmp.classpath" value="${{javac.test.classpath}}">
            <istrue value="${{is.test}}"/>
        </condition>
        <property name="task-tmp.classpath" value="${{javac.classpath}}"/>
        
        <condition property="task-tmp.debug" value="true">
            <istrue value="${{is.test}}"/>
        </condition>
        <property name="task-tmp.debug" value="${{javac.debug}}"/>
        
        <property location="${{task-tmp.src.dir}}" name="tmp-task.src.dir.absolute"/>        
        
        <mkdir dir="${{task-tmp.out.dir}}"/>

        <xsl:choose>
            <xsl:when test="/p:project/p:configuration/j2se:data/j2se:explicit-platform">
                <javac srcdir="${{task-tmp.src.dir}}" destdir="${{task-tmp.out.dir}}"
                    debug="${{task-tmp.debug}}" deprecation="${{javac.deprecation}}"
                    source="${{javac.source}}" includes="${{javac.includes}}" includeantruntime="false"
                    fork="yes" executable="${{platform.javac}}">
                    <classpath>
                        <path path="${{task-tmp.classpath}}"/>
                    </classpath>
                </javac>
            </xsl:when>
            <xsl:otherwise>
                <javac srcdir="${{task-tmp.src.dir}}" destdir="${{task-tmp.out.dir}}"
                    debug="${{task-tmp.debug}}" deprecation="${{javac.deprecation}}"
                    source="${{javac.source}}" includes="${{javac.includes}}" includeantruntime="false">
                    <classpath>
                        <path path="${{task-tmp.classpath}}"/>
                    </classpath>
                </javac>
            </xsl:otherwise>
        </xsl:choose>        
    </target>
    
    <target name="jar" depends="init,compile">
        <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
        <mkdir dir="${{dist.jar.dir}}"/>
        <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}">
            <xsl:if test="/p:project/p:configuration/j2se:data/j2se:use-manifest">
                <!-- Assume this is a J2SE application. -->
                <!-- Any Main-Class set in the manifest takes precedence. -->
                <xsl:attribute name="manifest">${manifest.file}</xsl:attribute>
                <manifest>
                    <attribute name="Main-Class" value="${{main.class}}"/>
                </manifest>
            </xsl:if>
            <fileset dir="${{build.classes.dir}}"/>
        </jar>
    </target>

    <target name="run" depends="init,compile">
        <xsl:choose>
            <xsl:when test="/p:project/p:configuration/j2se:data/j2se:explicit-platform">
                <java fork="true" classname="${{main.class}}" jvm="${{platform.java}}">
                    <xsl:call-template name="run-java-body"/>
                </java>
            </xsl:when>
            <xsl:otherwise>
                <java fork="true" classname="${{main.class}}">
                    <xsl:call-template name="run-java-body"/>
                </java>
            </xsl:otherwise>
        </xsl:choose>
    </target>

    <target name="do-debug" depends="init">
        <xsl:choose>
            <xsl:when test="/p:project/p:configuration/j2se:data/j2se:explicit-platform">
                <java fork="true" classname="${{main.class}}" jvm="${{platform.java}}">
                    <xsl:call-template name="debug-java-body"/>
                </java>
            </xsl:when>
            <xsl:otherwise>
                <java fork="true" classname="${{main.class}}">
                    <xsl:call-template name="debug-java-body"/>
                </java>
            </xsl:otherwise>
        </xsl:choose>
    </target>
    
    <target name="debug" depends="init,compile,do-debug">
    </target>

    <target name="debug-nb" depends="init,compile" if="netbeans.home">
        <nbjpdastart transport="dt_socket" addressproperty="jpda.address" name="${{main.class}}">
            <classpath>
                <path path="${{debug.classpath}}"/>
            </classpath>
            <xsl:if test="/p:project/p:configuration/j2se:data/j2se:explicit-platform">
            <bootclasspath>
                <path path="${{platform.bootcp}}"/>
            </bootclasspath>
            </xsl:if>
        </nbjpdastart>
        <antcall target="do-debug"/>
    </target>

    <target name="debug-fix-nb" depends="init" if="netbeans.home">
        <fail unless="fix.includes">Must set fix.includes</fail>
        <property name="javac.includes" value="${{fix.includes}}.java"/>
        <antcall target="compile-single"/>
        <condition property="task-tmp.out.dir" value="${{build.test.classes.dir}}">
            <istrue value="${{is.test}}"/>
        </condition>
        <property name="task-tmp.out.dir" value="${{build.classes.dir}}"/>
        <nbjpdareload>
            <fileset includes="${{fix.includes}}*.class" dir="${{task-tmp.out.dir}}"/>
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
            <xsl:if test="/p:project/p:configuration/j2se:data/j2se:explicit-platform">
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

    
    <target name="test-build" depends="init,compile" if="have.tests">
        <mkdir dir="${{build.test.classes.dir}}"/>
        <javac srcdir="${{test.src.dir}}" destdir="${{build.test.classes.dir}}"
               debug="true" deprecation="${{javac.deprecation}}"
               source="${{javac.source}}" includeantruntime="false">
            <classpath>
                <path path="${{javac.test.classpath}}"/>
            </classpath>
            <xsl:if test="/p:project/p:configuration/j2se:data/j2se:explicit-platform">
            <bootclasspath>
                <path path="${{platform.bootcp}}"/>
            </bootclasspath>
            </xsl:if>
        </javac>
        <copy todir="${{build.test.classes.dir}}">
            <fileset dir="${{test.src.dir}}">
                <exclude name="**/*.java"/>
            </fileset>
        </copy>
    </target>

    <target name="test" depends="init,test-build" if="have.tests">
        <mkdir dir="${{build.test.results.dir}}"/>
        <xsl:choose>
            <xsl:when test="/p:project/p:configuration/j2se:data/j2se:explicit-platform">
                <junit showoutput="true" fork="true" failureproperty="tests.failed" errorproperty="tests.failed" jvm="${{platform.java}}">
                    <xsl:call-template name="test-junit-body"/>
                </junit>
            </xsl:when>
            <xsl:otherwise>
                <junit showoutput="true" fork="true" failureproperty="tests.failed" errorproperty="tests.failed">
                    <xsl:call-template name="test-junit-body"/>
                </junit>
            </xsl:otherwise>
        </xsl:choose>
        <!-- TBD
        <junitreport todir="${{build.test.results.dir}}">
            <fileset dir="${{build.test.results.dir}}">
                <include name="TEST-*.xml"/>
            </fileset>
            <report format="noframes" todir="${{build.test.results.dir}}"/>
        </junitreport>
        -->
        <fail if="tests.failed">Some tests failed; see details above.</fail>
    </target>

    <target name="test-nb" depends="init,test" if="netbeans.home+have.tests">
        <!-- TBD
        <nbbrowse file="${{build.test.results.dir}}/junit-noframes.html"/>
        -->
    </target>

    <target name="test-single" depends="init,test-build" if="have.tests">
        <fail unless="test.includes">Must select some files in the IDE or set test.includes</fail>
        <mkdir dir="${{build.test.results.dir}}"/>
        <xsl:choose>
            <xsl:when test="/p:project/p:configuration/j2se:data/j2se:explicit-platform">
                <junit showoutput="true" fork="true" failureproperty="tests.failed" errorproperty="tests.failed" jvm="${{platform.java}}">
                    <xsl:call-template name="test-single-junit-body"/>
                </junit>
            </xsl:when>
            <xsl:otherwise>
                <junit showoutput="true" fork="true" failureproperty="tests.failed" errorproperty="tests.failed">
                    <xsl:call-template name="test-single-junit-body"/>
                </junit>
            </xsl:otherwise>
        </xsl:choose>
        <fail if="tests.failed">Some tests failed; see details above.</fail>
    </target>

    <target name="test-single-nb" depends="init,test-single" if="netbeans.home+have.tests">
        <!-- nothing -->
    </target>
    
    <target name="do-debug-test-single" depends="init" if="have.tests">
        <fail unless="test.class">Must select one file in the IDE or set test.class</fail>
        <xsl:choose>
            <xsl:when test="/p:project/p:configuration/j2se:data/j2se:explicit-platform">
                <java fork="true" classname="junit.textui.TestRunner" jvm="${{platform.java}}">
                    <xsl:call-template name="debug-test-single-java-body"/>
                </java>
            </xsl:when>
            <xsl:otherwise>
                <java fork="true" classname="junit.textui.TestRunner">
                    <xsl:call-template name="debug-test-single-java-body"/>
                </java>
            </xsl:otherwise>
        </xsl:choose>
    </target>

    <target name="debug-test-single" depends="init,test-build,do-debug-test-single" if="have.tests">
    </target>

    <target name="debug-test-single-nb" depends="init,test-build" if="netbeans.home+have.tests">
        <nbjpdastart transport="dt_socket" addressproperty="jpda.address" name="${{test.class}}">
            <classpath>
                <path path="${{debug.test.classpath}}"/>
            </classpath>
            <xsl:if test="/p:project/p:configuration/j2se:data/j2se:explicit-platform">
                <bootclasspath>
                    <path path="${{platform.bootcp}}"/>
                </bootclasspath>
            </xsl:if>
        </nbjpdastart>
        <antcall target="do-debug-test-single"/>
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

    <xsl:template name="run-java-body">
        <classpath>
            <path path="${{run.classpath}}"/>
        </classpath>
        <arg line="${{application.args}}"/>
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

    <xsl:template name="test-junit-body">
        <batchtest todir="${{build.test.results.dir}}">
            <fileset dir="${{test.src.dir}}">
                <!-- XXX could include only out-of-date tests... -->
                <include name="**/*Test.java"/>
            </fileset>
        </batchtest>
        <classpath>
            <path path="${{run.test.classpath}}"/>
        </classpath>
        <formatter type="brief" usefile="false"/>
        <!-- TBD
        <formatter type="xml"/>
        -->
    </xsl:template>

    <xsl:template name="test-single-junit-body">
        <batchtest todir="${{build.test.results.dir}}">
            <fileset dir="${{test.src.dir}}" includes="${{test.includes}}"/>
        </batchtest>
        <classpath>
            <path path="${{run.test.classpath}}"/>
        </classpath>
        <formatter type="brief" usefile="false"/>
        <!-- TBD
        <formatter type="xml"/>
        -->
    </xsl:template>

    <xsl:template name="debug-test-single-java-body">
        <jvmarg value="-Xdebug"/>
        <jvmarg value="-Xnoagent"/>
        <jvmarg value="-Djava.compiler=none"/>
        <jvmarg value="-Xrunjdwp:transport=dt_socket,address=${{jpda.address}}"/>
        <classpath>
            <path path="${{debug.test.classpath}}"/>
        </classpath>
        <arg line="${{test.class}}"/>
        <arg line="${{application.args}}"/>
    </xsl:template>

</xsl:stylesheet>
