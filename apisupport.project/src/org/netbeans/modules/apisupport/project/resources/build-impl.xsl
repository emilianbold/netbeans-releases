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
                xmlns:p="http://www.netbeans.org/ns/project"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:nbm="http://www.netbeans.org/ns/nb-module-project"
                exclude-result-prefixes="xalan p nbm">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

<xsl:comment> *** GENERATED FROM project.xml - DO NOT EDIT *** </xsl:comment>

<project name="{/p:project/p:configuration/nbm:data/nbm:path}-impl" default="netbeans" basedir="..">

    <target name="init">
        <property file="nbproject/private/private.properties"/>
        <!--
        <property name="user.properties.file" location="${{netbeans.user}}/build.properties"/>
        <property file="${{user.properties.file}}"/>
        -->
        <property file="nbproject/project.properties"/>
        <fail unless="XXX.XXX">Must set XXX.XXX</fail>
    </target>

    <target name="compile" depends="init">
        <mkdir dir="${{build.classes.dir}}"/>
        <javac srcdir="${{src.dir}}" destdir="${{build.classes.dir}}" debug="${{javac.debug}}" optimize="${{javac.optimize}}" deprecation="${{javac.deprecation}}" source="${{javac.source}}" includeantruntime="false">
            <classpath>
                <path path="${{javac.classpath}}"/>
            </classpath>
        </javac>
        <copy todir="${{build.classes.dir}}">
            <fileset dir="${{src.dir}}" excludes="${{build.classes.excludes}}"/>
        </copy>
    </target>

    <target name="compile-single" depends="init">
        <fail unless="selected.files">Must select some files in the IDE or set selected.files</fail>
        <property name="src.dir.absolute" location="${{src.dir}}"/>
        <pathconvert property="javac.includes" pathsep=",">
            <path path="${{selected.files}}"/>
            <map from="${{src.dir.absolute}}${{file.separator}}" to=""/>
        </pathconvert>
        <mkdir dir="${{build.classes.dir}}"/>
        <javac srcdir="${{src.dir}}" destdir="${{build.classes.dir}}"
               debug="${{javac.debug}}" optimize="${{javac.optimize}}" deprecation="${{javac.deprecation}}"
               source="${{javac.source}}" includes="${{javac.includes}}" includeantruntime="false">
            <classpath>
                <path path="${{javac.classpath}}"/>
            </classpath>
        </javac>
    </target>

    <target name="jar" depends="init,compile">
        <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
        <mkdir dir="${{dist.jar.dir}}"/>
        <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}">
        </jar>
    </target>

    <!-- XXX conditional -->
    <target name="javadoc" depends="init">
        <mkdir dir="${{dist.javadoc.dir}}"/>
        <!-- XXX do an up-to-date check first -->
        <javadoc destdir="${{dist.javadoc.dir}}" source="${{javac.source}}">
            <classpath>
                <path path="${{javac.classpath}}"/>
            </classpath>
            <sourcepath>
                <pathelement location="${{src.dir}}"/>
            </sourcepath>
            <fileset dir="${{src.dir}}"/>
        </javadoc>
    </target>

    <target name="javadoc-nb" depends="init,javadoc" if="netbeans.home">
        <nbbrowse file="${{dist.javadoc.dir}}/index.html"/>
    </target>

    <!-- XXX conditional -->
    <target name="test-build" depends="init,compile" if="have.tests">
        <mkdir dir="${{build.test.classes.dir}}"/>
        <javac srcdir="test" destdir="${{build.test.classes.dir}}"
               debug="true" optimize="false" deprecation="${{javac.deprecation}}"
               source="${{javac.source}}" includeantruntime="false">
            <classpath>
                <path path="${{javac.test.classpath}}"/>
            </classpath>
        </javac>
        <copy todir="${{build.test.classes.dir}}">
            <fileset dir="${{test.src.dir}}">
                <exclude name="**/*.java"/>
            </fileset>
        </copy>
    </target>

    <target name="test" depends="init,test-build" if="have.tests">
        <mkdir dir="${{build.test.results.dir}}"/>
        <junit showoutput="true" fork="true" failureproperty="tests.failed" errorproperty="tests.failed">
            <xsl:call-template name="test-junit-body"/>
        </junit>
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
        <fail unless="selected.files">Must select some files in the IDE or set selected.files</fail>
        <property name="test.src.dir.absolute" location="${{test.src.dir}}"/>
        <pathconvert property="test.includes" pathsep=",">
            <path path="${{selected.files}}"/>
            <map from="${{test.src.dir.absolute}}${{file.separator}}" to=""/>
        </pathconvert>
        <mkdir dir="${{build.test.results.dir}}"/>
        <junit showoutput="true" fork="true" failureproperty="tests.failed" errorproperty="tests.failed">
        <xsl:call-template name="test-single-junit-body"/>
        </junit>
        <fail if="tests.failed">Some tests failed; see details above.</fail>
    </target>

    <target name="test-single-nb" depends="init,test-single" if="netbeans.home+have.tests">
        <!-- nothing -->
    </target>

    <target name="init-test-class" unless="test.class">
        <fail unless="selected.files">Must select one file in the IDE or set selected.files</fail>
        <property name="test.src.dir.absolute" location="${{test.src.dir}}"/>
        <!-- XXX this is pretty ugly; but no apparent way to add <mapper> to <pathconvert>: -->
        <pathconvert property="test.class.tmp" dirsep=".">
            <path path="${{selected.files}}"/>
            <map from="${{test.src.dir.absolute}}${{file.separator}}" to=""/>
        </pathconvert>
        <basename file="${{test.class.tmp}}" property="test.class" suffix=".java"/>
    </target>
    
    <target name="do-debug-test-single" depends="init,init-test-class" if="have.tests">
        <java fork="true" classname="junit.textui.TestRunner">
        <xsl:call-template name="debug-test-single-java-body"/>
        </java>
    </target>

    <target name="debug-test-single" depends="init,init-test-class,test-build,do-debug-test-single" if="have.tests">
    </target>

    <target name="debug-test-single-nb" depends="init,init-test-class,test-build" if="netbeans.home+have.tests">
        <nbjpdastart transport="dt_socket" addressproperty="jpda.address" name="${{test.class}}"/>
        <antcall target="do-debug-test-single"/>
    </target>

    <target name="clean" depends="init">
        <delete dir="${{build.dir}}"/>
        <delete dir="${{netbeans.dir}}"/>
    </target>

</project>

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
