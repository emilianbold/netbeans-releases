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
                xmlns:nbm="http://www.netbeans.org/ns/nb-module-project/1"
                exclude-result-prefixes="xalan p nbm">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    
    <xsl:variable name="modules" select="document('modules.xml')/modules"/>
    <xsl:variable name="deps" select="/p:project/p:configuration/nbm:data/nbm:module-dependencies"/>
    <xsl:variable name="path" select="/p:project/p:configuration/nbm:data/nbm:path"/>
    
    <xsl:template match="/">

<xsl:comment>
                Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
Microsystems, Inc. All Rights Reserved.
</xsl:comment>
<xsl:comment> *** GENERATED FROM project.xml - DO NOT EDIT *** </xsl:comment>

<xsl:variable name="build.prerequisites">
    <xsl:for-each select="$deps/nbm:dependency[nbm:build-prerequisite]">
        <xsl:if test="position() &gt; 1">
            <xsl:text>,</xsl:text>
        </xsl:if>
            <xsl:variable name="cnb" select="nbm:code-name-base"/>
            <xsl:variable name="match" select="$modules/module[cnb = $cnb]"/>
            <!--
            <xsl:message>Found match: <xsl:value-of select="$match"/> for '<xsl:value-of select="$cnb"/>'</xsl:message>
            -->
            <xsl:choose>
                <xsl:when test="count($match) = 0">
                    <xsl:message>Warning: could not find module named <xsl:value-of select="$cnb"/>!</xsl:message>
                </xsl:when>
                <xsl:when test="count($match) > 1">
                    <xsl:message>Warning: more than one match for module <xsl:value-of select="$cnb"/>!</xsl:message>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>all-</xsl:text>
                    <xsl:value-of select="$match/path"/>
                </xsl:otherwise>
            </xsl:choose>
    </xsl:for-each>
</xsl:variable>
<xsl:comment>
NOTE: nbbuild/build.xml should contain:
&lt;target name="all-<xsl:value-of select="$path"/>" depends="<xsl:value-of select="$build.prerequisites"/>"&gt;
    &lt;echo message="Building module <xsl:value-of select="$path"/>..."/&gt;
    &lt;ant dir="../<xsl:value-of select="$path"/>" target="netbeans"/&gt;
&lt;/target&gt;
</xsl:comment>


<project name="{$path}/impl" default="netbeans" basedir="..">

    <target name="init">
        <!-- Synch the following with NbModuleProject.makeEvalDefs: -->
        <property file="nbproject/private/private.properties"/>
        <!--
        <property name="user.properties.file" location="${{netbeans.user}}/build.properties"/>
        <property file="${{user.properties.file}}"/>
        -->
        <property file="nbproject/project.properties"/>
        <property name="code.name.base.dashes" value="{translate(/p:project/p:name, '.', '-')}"/>
        <property name="domain" value="{substring-before(/p:project/p:configuration/nbm:data/nbm:path, '/')}"/>
        <property name="module.jar.dir" value="modules"/>
        <property name="module.jar" value="${{module.jar.dir}}/${{code.name.base.dashes}}.jar"/>
        <property name="nbm" value="${{code.name.base.dashes}}.nbm"/>
        <property name="nbm.needs.restart" value="false"/>
        <property name="homepage.base" value="netbeans.org"/>
        <property name="dist.base" value="www.netbeans.org/download/nbms/40"/>
        <fail unless="nbroot">Must set nbroot</fail>
        <xsl:if test="/p:project/p:configuration/nbm:data/nbm:javadoc">
            <fail unless="javadoc.name">Must set javadoc.name</fail>
            <fail unless="javadoc.title">Must set javadoc.title</fail>
        </xsl:if>
        <property name="license.file" location="${{nbroot}}/nbbuild/standard-nbm-license.txt"/>
        <property name="nbm_alias" value="nb_ide"/>
        <property name="build.compiler.debug" value="true"/>
        <property name="build.compiler.deprecation" value="true"/>
        <property name="build.sysclasspath" value="ignore"/>
        <property name="manifest.mf" location="manifest.mf"/>
        <property name="src.dir" location="src"/>
        <property name="build.classes.dir" location="build/classes"/>
        <path id="cp">
            <xsl:for-each select="$deps/nbm:dependency[count(nbm:compile-dependency) = 1]">
                <xsl:variable name="cnb" select="nbm:code-name-base"/>
                <xsl:variable name="match" select="$modules/module[cnb = $cnb]"/>
                <!--
                <xsl:message>Found match: <xsl:value-of select="$match"/> for '<xsl:value-of select="$cnb"/>'</xsl:message>
                -->
                <xsl:choose>
                    <xsl:when test="count($match) = 0">
                        <xsl:message>Warning: could not find module named <xsl:value-of select="$cnb"/>!</xsl:message>
                    </xsl:when>
                    <xsl:when test="count($match) > 1">
                        <xsl:message>Warning: more than one match for module <xsl:value-of select="$cnb"/>!</xsl:message>
                    </xsl:when>
                    <xsl:otherwise>
                        <pathelement location="${{nbroot}}/{$match/path}/netbeans/{$match/jar}"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <pathelement path="${{cp.extra}}"/>
        </path>
        <xsl:if test="/p:project/p:configuration/nbm:data/nbm:unit-tests">
            <property name="test.src.dir" location="test/unit/src"/>
            <property name="build.test.classes.dir" location="build/test/classes"/>
            <property name="build.test.results.dir" location="build/test/results"/>
            <path id="test.cp">
                <path refid="cp"/>
                <pathelement location="netbeans/${{module.jar}}"/>
                <pathelement location="${{nbroot}}/xtest/lib/junit.jar"/>
                <pathelement location="${{nbroot}}/xtest/lib/nbjunit.jar"/>
                <pathelement path="${{test.cp.extra}}"/>
            </path>
            <path id="test.run.cp">
                <path refid="test.cp"/>
                <pathelement location="${{build.test.classes.dir}}"/>
                <pathelement path="${{test.run.cp.extra}}"/>
            </path>
        </xsl:if>
    </target>

    <target name="compile" depends="init">
        <mkdir dir="${{build.classes.dir}}"/>
        <javac srcdir="${{src.dir}}" destdir="${{build.classes.dir}}" debug="${{build.compiler.debug}}" deprecation="${{build.compiler.deprecation}}" source="1.4" includeantruntime="false">
            <classpath refid="cp"/>
        </javac>
        <copy todir="${{build.classes.dir}}">
            <fileset dir="${{src.dir}}" excludesfile="${{nbroot}}/nbbuild/standard-jar-excludes.txt"/>
        </copy>
    </target>

    <target name="compile-single" depends="init">
        <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
        <mkdir dir="${{build.classes.dir}}"/>
        <!-- XXX consider forcing debug=true and deprecation=true -->
        <!-- XXX consider deleting the .class file first to force a rebuild -->
        <javac srcdir="${{src.dir}}" destdir="${{build.classes.dir}}"
               debug="${{build.compiler.debug}}" deprecation="${{build.compiler.deprecation}}"
               source="1.4" includes="${{javac.includes}}" includeantruntime="false">
            <classpath refid="cp"/>
        </javac>
    </target>

    <target name="jar" depends="init,compile">
        <mkdir dir="netbeans/${{module.jar.dir}}"/>
        <tstamp>
            <format property="buildnumber" pattern="yyMMdd" timezone="UTC"/>
        </tstamp>
        <jar jarfile="netbeans/${{module.jar}}" compress="false" manifest="${{manifest.mf}}">
            <manifest>
                <attribute name="OpenIDE-Module-Public-Packages">
                    <xsl:attribute name="value">
                        <xsl:call-template name="public.packages">
                            <xsl:with-param name="glob" select="'.*'"/>
                            <xsl:with-param name="whenempty" select="'-'"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </attribute>
                <xsl:variable name="openide.dep" select="$deps/nbm:dependency[nbm:code-name-base = 'org.openide' and nbm:run-dependency]"/>
                <xsl:if test="$openide.dep">
                    <!-- Special-cased. -->
                    <attribute name="OpenIDE-Module-IDE-Dependencies" value="IDE/{$openide.dep/nbm:run-dependency/nbm:release-version} &gt; {$openide.dep/nbm:run-dependency/nbm:specification-version}"/>
                </xsl:if>
                <xsl:variable name="module.deps" select="$deps/nbm:dependency[nbm:code-name-base != 'org.openide' and nbm:run-dependency]"/>
                <xsl:if test="$module.deps">
                    <attribute name="OpenIDE-Module-Module-Dependencies">
                        <xsl:attribute name="value">
                            <xsl:for-each select="$module.deps">
                                <xsl:if test="position() &gt; 1">
                                    <xsl:text>, </xsl:text>
                                </xsl:if>
                                <xsl:value-of select="nbm:code-name-base"/>
                                <xsl:if test="nbm:run-dependency/nbm:release-version">
                                    <xsl:text>/</xsl:text>
                                    <xsl:value-of select="nbm:run-dependency/nbm:release-version"/>
                                </xsl:if>
                                <xsl:if test="nbm:run-dependency/nbm:specification-version">
                                    <xsl:text> &gt; </xsl:text>
                                    <xsl:value-of select="nbm:run-dependency/nbm:specification-version"/>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:attribute>
                    </attribute>
                </xsl:if>
                <!-- XXX make this conditional so can use OIDE-M-B-V instead -->
                <attribute name="OpenIDE-Module-Implementation-Version" value="${{buildnumber}}"/>
            </manifest>
            <fileset dir="${{build.classes.dir}}"/>
        </jar>
    </target>
    
    <target name="reload" depends="jar">
        <nbinstaller module="netbeans/${{module.jar}}" action="reinstall"/>
    </target>

    <target name="netbeans" depends="init,jar">
        <taskdef name="genlist" classname="org.netbeans.nbbuild.MakeListOfNBM" classpath="${{nbroot}}/nbbuild/nbantext.jar"/>
        <genlist targetname="nbm" outputfiledir="netbeans"/>
    </target>
    
    <target name="nbm" depends="init,netbeans">
        <mkdir dir="build"/>
        <taskdef name="makenbm" classname="org.netbeans.nbbuild.MakeNBM" classpath="${{nbroot}}/nbbuild/nbantext.jar"/>
        <makenbm file="build/${{nbm}}"
                 topdir="."
                 module="netbeans/${{module.jar}}"
                 homepage="http://${{domain}}.${{homepage.base}}/"
                 distribution="http://${{dist.base}}/${{nbm}}"
                 needsrestart="${{nbm.needs.restart}}">
            <license file="${{license.file}}"/>
            <signature keystore="${{keystore}}" storepass="${{storepass}}" alias="${{nbm_alias}}"/>
        </makenbm>
    </target>

    <xsl:if test="/p:project/p:configuration/nbm:data/nbm:javadoc">
    
        <target name="javadoc" depends="init">
            <ant dir="${{nbroot}}/nbbuild/javadoctools" antfile="template.xml" target="javadoc">
                <property name="javadoc.base" location="."/>
                <property name="javadoc.packages">
                    <xsl:attribute name="value">
                        <xsl:call-template name="public.packages">
                            <xsl:with-param name="glob" select="''"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </property>
                <property name="javadoc.classpath" refid="cp"/>
            </ant>
        </target>

        <target name="javadoc-nb" depends="init,javadoc" if="netbeans.home">
            <nbbrowse file="javadoc/${{javadoc.name}}/index.html"/>
        </target>
        
    </xsl:if>

    <xsl:if test="/p:project/p:configuration/nbm:data/nbm:unit-tests">
    
        <target name="test-build" depends="init,jar">
            <mkdir dir="${{build.test.classes.dir}}"/>
            <javac srcdir="${{test.src.dir}}" destdir="${{build.test.classes.dir}}"
                   debug="true" deprecation="${{build.compile.deprecation}}"
                   source="1.4" includeantruntime="false">
                <classpath refid="test.cp"/>
            </javac>
            <copy todir="${{build.test.classes.dir}}">
                <fileset dir="${{test.src.dir}}">
                    <exclude name="**/*.java"/>
                </fileset>
            </copy>
        </target>

        <target name="compile-test-single" depends="init,jar">
            <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
            <mkdir dir="${{build.test.classes.dir}}"/>
            <!-- XXX consider forcing deprecation=true -->
            <!-- XXX consider deleting the .class file first to force a rebuild -->
            <javac srcdir="${{test.src.dir}}" destdir="${{build.test.classes.dir}}"
                   debug="true" deprecation="${{build.compile.deprecation}}"
                   source="1.4" includeantruntime="false" includes="${{javac.includes}}">
                <classpath refid="test.cp"/>
            </javac>
        </target>

        <target name="test" depends="init,test-build">
            <mkdir dir="${{build.test.results.dir}}"/>
            <junit showoutput="true" fork="true" failureproperty="tests.failed" errorproperty="tests.failed">
                <batchtest todir="${{build.test.results.dir}}">
                    <fileset dir="${{test.src.dir}}">
                        <!-- XXX could include only out-of-date tests... -->
                        <include name="**/*Test.java"/>
                    </fileset>
                </batchtest>
                <classpath refid="test.run.cp"/>
                <formatter type="brief" usefile="false"/>
            </junit>
            <fail if="tests.failed">Some tests failed; see details above.</fail>
        </target>

        <target name="test-single" depends="init,test-build">
            <fail unless="test.includes">Must set test.includes</fail>
            <mkdir dir="${{build.test.results.dir}}"/>
            <junit showoutput="true" fork="true" failureproperty="tests.failed" errorproperty="tests.failed">
                <batchtest todir="${{build.test.results.dir}}">
                    <fileset dir="${{test.src.dir}}" includes="${{test.includes}}"/>
                </batchtest>
                <classpath refid="test.run.cp"/>
                <formatter type="brief" usefile="false"/>
            </junit>
            <fail if="tests.failed">Some tests failed; see details above.</fail>
        </target>

        <target name="do-debug-test-single">
            <fail unless="test.class">Must set test.class</fail>
            <java fork="true" classname="junit.textui.TestRunner">
                <jvmarg value="-Xdebug"/>
                <jvmarg value="-Xnoagent"/>
                <jvmarg value="-Djava.compiler=none"/>
                <jvmarg value="-Xrunjdwp:transport=dt_socket,address=${{jpda.address}}"/>
                <classpath refid="test.run.cp"/>
                <arg line="${{test.class}}"/>
            </java>
        </target>

        <target name="debug-test-single" depends="init,test-build,do-debug-test-single"/>

        <target name="debug-test-single-nb" depends="init,test-build" if="netbeans.home">
            <fail unless="test.class">Must set test.class</fail>
            <nbjpdastart transport="dt_socket" addressproperty="jpda.address" name="${{test.class}}"/>
            <antcall target="do-debug-test-single"/>
        </target>
        
    </xsl:if>

    <target name="clean" depends="init">
        <delete dir="build"/>
        <delete dir="netbeans"/>
        <delete dir="Info"/>
        <xsl:if test="/p:project/p:configuration/nbm:data/nbm:javadoc">
            <delete dir="javadoc"/>
        </xsl:if>
    </target>

</project>

    </xsl:template>
    
    <xsl:template name="public.packages">
        <xsl:param name="glob"/>
        <xsl:param name="whenempty" select="''"/>
        <xsl:variable name="pkgs" select="/p:project/p:configuration/nbm:data/nbm:public-packages/nbm:package"/>
        <xsl:choose>
            <xsl:when test="count($pkgs) &gt; 0">
                <xsl:for-each select="$pkgs">
                    <xsl:if test="position() &gt; 1">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                    <xsl:value-of select="."/>
                    <xsl:value-of select="$glob"/>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$whenempty"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
