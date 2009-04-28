<?xml version="1.0" encoding="UTF-8"?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

 The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:j2seproject1="http://www.netbeans.org/ns/j2se-project/1"
                xmlns:j2seproject="http://www.netbeans.org/ns/j2se-project/3"
                xmlns:j2seproject2="http://www.netbeans.org/ns/j2se-project/3"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                exclude-result-prefixes="xalan p j2se projdeps">
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
            <!-- 5.0 -->
            <xsl:when test="/p:project/p:configuration/j2seproject:data/j2seproject:name">
                <xsl:value-of select="/p:project/p:configuration/j2seproject:data/j2seproject:name"/>
            </xsl:when>
            <!-- 4.1 -->
            <xsl:when test="/p:project/p:configuration/j2seproject2:data/j2seproject2:name">
                <xsl:value-of select="/p:project/p:configuration/j2seproject2:data/j2seproject2:name"/>
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
        <target name="-mgt-init-platform">
            <condition property="platform.home" value="${{jdk.home}}">
                <isfalse value="${{platform.home}}"/>
            </condition> 
            <condition property="platform.java" value="${{jdk.home}}/bin/java">
                <isfalse value="${{platform.java}}"/>
            </condition>  
        </target>
    
        <target name="-init-macrodef-management">
          <macrodef name="management">
            <attribute name="classname" default="${{main.class}}"/>
            <element name="customize" optional="true"/>
            <sequential>
                <java fork="true" classname="@{{classname}}" dir="${{work.dir}}" jvm="${{platform.java}}">
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
        
        <target name="-connect-jconsole">
            <xsl:attribute name="depends">init,-mgt-init-platform</xsl:attribute>
         <echo message="jconsole ${{jconsole.settings.vmoptions}} -interval=${{jconsole.settings.polling}} ${{jconsole.settings.notile}} ${{jconsole.managed.process.url}}"/>
        <java fork="true" classname="sun.tools.jconsole.JConsole" jvm="${{platform.java}}">
            <jvmarg line="${{jconsole.settings.vmoptions}}"/>
            <arg line="-interval=${{jconsole.settings.polling}} ${{jconsole.settings.notile}} ${{jconsole.managed.process.url}}"/>
            <classpath>
                <path path="${{run.classpath}}:${{platform.home}}/lib/jconsole.jar:${{platform.home}}/lib/tools.jar"/>
            </classpath>          
        </java>
</target>
            <target name="run-management" >
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-mgt-init-platform,-init-macrodef-management</xsl:attribute>
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
       <target name="-init-debug-args">
           <exec executable="${{platform.java}}" outputproperty="version-output">
               <arg value="-version"/>
           </exec>
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

       <target name="-init-macrodef-management-debug" depends="-init-debug-args">
        <macrodef name="debug-management">
            <attribute name="classname" default="${{main.class}}"/>
            <attribute name="classpath" default="${{debug.classpath}}"/>
            <attribute name="args" default="${{application.args}}"/>
            <sequential>
                <java fork="true" classname="@{{classname}}" dir="${{work.dir}}" jvm="${{platform.java}}">
                    <jvmarg line="${{debug-args-line}}"/>
                    <jvmarg value="-Xrunjdwp:transport=${{debug-transport}},address=${{jpda.address}}"/>
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
        <xsl:attribute name="depends">init,compile,-mgt-init-platform,-init-macrodef-management-debug</xsl:attribute>
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
