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
                xmlns:j2seproject1="http://www.netbeans.org/ns/j2se-project/1"
                xmlns:j2seproject="http://www.netbeans.org/ns/j2se-project/2"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                exclude-result-prefixes="xalan p j2se projdeps">
<!-- XXX should use namespaces for NB in-VM tasks from ant/browsetask and profilerjpda/ant (Ant 1.6.1 and higher only) -->
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

        <xsl:comment><![CDATA[
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***

For the purpose of easier reading the script
is divided into following sections:

  - initialization
  - management

]]></xsl:comment>
         <xsl:variable name="name">
          <xsl:choose>
            <!-- 4.1 -->
            <xsl:when test="/p:project/p:configuration/j2seproject:data/j2seproject:name">
                <xsl:value-of select="/p:project/p:configuration/j2seproject:data/j2seproject:name"/>
            </xsl:when>
            <!-- 4.0 -->
            <xsl:when test="/p:project/p:configuration/j2seproject1:data/j2seproject1:name">
                <xsl:value-of select="/p:project/p:configuration/j2seproject1:data/j2seproject1:name"/>
            </xsl:when>
         </xsl:choose>
        </xsl:variable>
        <!-- Synch with build-impl.xsl: -->
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-management-impl">
            <xsl:attribute name="default">run-management</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>

            <target name="default">
                <xsl:attribute name="depends">run-management</xsl:attribute>
                <xsl:attribute name="description">Build the project and enable management.</xsl:attribute>
            </target>

            <xsl:comment> 
    ======================
    INITIALIZATION SECTION 
    ======================
    </xsl:comment>

            <target name="-mgt-pre-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-mgt-init-private">
                <xsl:attribute name="depends">-mgt-pre-init</xsl:attribute>
                <property file="nbproject/private/private.properties"/>
            </target>

            <target name="-mgt-init-user">
                <xsl:attribute name="depends">-mgt-pre-init,-mgt-init-private</xsl:attribute>
                <property file="${{user.properties.file}}"/>
                <xsl:comment> The two properties below are usually overridden </xsl:comment>
                <xsl:comment> by the active platform. Just a fallback. </xsl:comment>
                <property name="default.javac.source" value="1.4"/>
                <property name="default.javac.target" value="1.4"/>
            </target>

            <target name="-mgt-init-project">
                <xsl:attribute name="depends">-mgt-pre-init,-mgt-init-private,-mgt-init-user</xsl:attribute>
                <property file="nbproject/project.properties"/>
            </target>

            <target name="-mgt-do-init">
                <xsl:attribute name="depends">-mgt-pre-init,-mgt-init-private,-mgt-init-user,-mgt-init-project,-mgt-init-macrodef-property</xsl:attribute>
                <xsl:if test="/p:project/p:configuration/j2seproject:data/j2seproject:explicit-platform">
                    <j2seproject:property name="platform.home" value="platforms.${{platform.active}}.home"/>
                    <j2seproject:property name="platform.bootcp" value="platforms.${{platform.active}}.bootclasspath"/>
                    <j2seproject:property name="platform.compiler" value="platforms.${{platform.active}}.compile"/>
                    <j2seproject:property name="platform.javac.tmp" value="platforms.${{platform.active}}.javac"/>
                    <condition property="platform.javac" value="${{platform.home}}/bin/javac">
                        <equals arg1="${{platform.javac.tmp}}" arg2="$${{platforms.${{platform.active}}.javac}}"/>
                    </condition>
                    <property name="platform.javac" value="${{platform.javac.tmp}}"/>
                    <j2seproject:property name="platform.java.tmp" value="platforms.${{platform.active}}.java"/>
                    <condition property="platform.java" value="${{platform.home}}/bin/java">
                        <equals arg1="${{platform.java.tmp}}" arg2="$${{platforms.${{platform.active}}.java}}"/>
                    </condition>
                    <property name="platform.java" value="${{platform.java.tmp}}"/>
                    <condition property="platform.invalid" value="true">
                        <or>
                            <contains string="${{platform.javac}}" substring="$${{platforms."/>
                            <contains string="${{platform.java}}" substring="$${{platforms."/>
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
                <available property="have.tests" file="${{test.src.dir}}"/>
                <condition property="netbeans.home+have.tests">
                    <and>
                        <isset property="netbeans.home"/>
                        <isset property="have.tests"/>
                    </and>
                </condition>
                <condition property="no.javadoc.preview">
                    <isfalse value="${{javadoc.preview}}"/>
                </condition>
                <property name="run.jvmargs" value=""/>
                <property name="javac.compilerargs" value=""/>
                <property name="work.dir" value="${{basedir}}"/>
                <condition property="no.deps">
                    <and>
                        <istrue value="${{no.dependencies}}"/>
                    </and>
                </condition>
            </target>

            <target name="-mgt-post-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-mgt-init-check">
                <xsl:attribute name="depends">-mgt-pre-init,-mgt-init-private,-mgt-init-user,-mgt-init-project,-mgt-do-init</xsl:attribute>
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
            </target>

            <target name="-mgt-init-macrodef-property">
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

            <target name="init">
                <xsl:attribute name="depends">-mgt-pre-init,-mgt-init-private,-mgt-init-user,-mgt-init-project,-mgt-do-init,-mgt-post-init,-mgt-init-check,-mgt-init-macrodef-property</xsl:attribute>
            </target>

            <xsl:comment>
    ==================
    MANAGEMENT SECTION
    ==================
    </xsl:comment>
        <target name="-init-macrodef-management">
          <macrodef name="management">
            <attribute name="classname" default="${{main.class}}"/>
            <element name="customize" optional="true"/>
            <sequential>
                <java fork="true" classname="@{{classname}}" dir="${{work.dir}}">
                    <jvmarg line="${{management.jvmargs}} ${{run.jvmargs}}"/>
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
        
        <target name="-connect-jconsole" depends="init">
         <echo message="jconsole ${{jconsole.settings.vmoptions}} -interval=${{jconsole.settings.polling}} ${{jconsole.settings.notile}} ${{jconsole.managed.process.url}}"/>
        <java fork="true" classname="sun.tools.jconsole.JConsole">
            <jvmarg line="${{jconsole.settings.vmoptions}}"/>
            <arg line="-interval=${{jconsole.settings.polling}} ${{jconsole.settings.notile}} ${{jconsole.managed.process.url}}"/>
            <classpath>
                <path path="${{run.classpath}}:${{jdk.home}}/lib/jconsole.jar:${{jdk.home}}/lib/tools.jar"/>
            </classpath>          
        </java>
</target>
            <target name="run-management" >
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-init-macrodef-management</xsl:attribute>
                <xsl:attribute name="description">Enable local mgt for a project in the IDE.</xsl:attribute>
                <echo message="${{connecting.jconsole.msg}} ${{jconsole.managed.process.url}}"/>
                 <management>
                  <customize>
                    <arg line="${{application.args}}"/>
                  </customize>
                </management>
            </target>

            <target name="run-lcl-mgt-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <xsl:attribute name="description">Manage a selected class in the IDE.</xsl:attribute>
                <fail unless="manage.class">Must select one file in the IDE or set manage.class</fail>
                <xsl:comment>
                    TODO
                </xsl:comment>
            </target>
      <xsl:comment>
    ========================
    MANAGEMENT DEBUG SECTION
    ========================
    </xsl:comment>
       <target name="-init-macrodef-management-debug">
        <macrodef name="debug-management">
            <attribute name="classname" default="${{main.class}}"/>
            <attribute name="classpath" default="${{debug.classpath}}"/>
            <attribute name="args" default="${{application.args}}"/>
            <sequential>
                <java fork="true" classname="@{{classname}}" dir="${{work.dir}}">
                    <jvmarg value="-Xdebug"/>
                    <jvmarg value="-Xnoagent"/>
                    <jvmarg value="-Djava.compiler=none"/>
                    <jvmarg value="-Xrunjdwp:transport=dt_socket,address=${{jpda.address}}"/>
                    <jvmarg line="${{management.jvmargs}} ${{run.jvmargs}}"/>
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
    <target name="-debug-start-debugger" if="netbeans.home" depends="init">
        <j2seproject1:nbjpdastart name="${{debug.class}}"/>
    </target>
    <target name="-debug-start-managed-debuggee"> 
        <xsl:attribute name="depends">init,compile,-init-macrodef-management-debug</xsl:attribute>
        <debug-management/>
    </target>
    <target name="debug-management">
     <xsl:attribute name="if">netbeans.home</xsl:attribute>
     <xsl:attribute name="depends">init,compile,-debug-start-debugger,-debug-start-managed-debuggee</xsl:attribute>
     <xsl:attribute name="description">Debug project in IDE with Local mgt enabled.</xsl:attribute>
    </target>
  </project>
  </xsl:template>
</xsl:stylesheet>
