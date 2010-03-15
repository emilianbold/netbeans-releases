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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
                xmlns:ear="http://www.netbeans.org/ns/j2ee-earproject/1"
                xmlns:ear2="http://www.netbeans.org/ns/j2ee-earproject/2"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                xmlns:projdeps2="http://www.netbeans.org/ns/ant-project-references/2"
                xmlns:libs="http://www.netbeans.org/ns/ant-project-libraries/1"
                exclude-result-prefixes="xalan p ear projdeps projdeps2 libs">
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
  - cleanup

]]></xsl:comment>

        <xsl:variable name="name" select="/p:project/p:configuration/ear2:data/ear2:name"/>
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-impl">
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>
            <import file="ant-deploy.xml" />

            <fail message="Please build using Ant 1.7.1 or higher.">
                <condition>
                    <not>
                        <antversion atleast="1.7.1"/>
                    </not>
                </condition>
            </fail>

            <target name="default">
                <xsl:attribute name="depends">dist</xsl:attribute>
                <xsl:attribute name="description">Build whole project.</xsl:attribute>
            </target>

            <xsl:comment> 
    INITIALIZATION SECTION 
    </xsl:comment>

            <target name="pre-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="init-private">
                <xsl:attribute name="depends">pre-init</xsl:attribute>
                <property file="nbproject/private/private.properties"/>
            </target>

            <xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">
                <target name="-pre-init-libraries">
                    <property name="libraries.path">
                        <xsl:attribute name="location"><xsl:value-of select="/p:project/p:configuration/libs:libraries/libs:definitions"/></xsl:attribute>
                    </property>
                    <dirname property="libraries.dir.nativedirsep" file="${{libraries.path}}"/>
                    <!-- Do not want \ on Windows, since it would act as an escape char: -->
                    <pathconvert property="libraries.dir" dirsep="/">
                        <path path="${{libraries.dir.nativedirsep}}"/>
                    </pathconvert>
                    <basename property="libraries.basename" file="${{libraries.path}}" suffix=".properties"/>
                    <available property="private.properties.available" file="${{libraries.dir}}/${{libraries.basename}}-private.properties"/>
                </target>
                <target name="-init-private-libraries" depends="-pre-init-libraries" if="private.properties.available">
                    <loadproperties srcfile="${{libraries.dir}}/${{libraries.basename}}-private.properties" encoding="ISO-8859-1">
                        <filterchain>
                            <replacestring from="$${{base}}" to="${{libraries.dir}}"/>
                            <escapeunicode/>
                        </filterchain>
                    </loadproperties>
                </target>
                <target name="-init-libraries" depends="pre-init,init-private,-init-private-libraries">
                    <loadproperties srcfile="${{libraries.path}}" encoding="ISO-8859-1">
                        <filterchain>
                            <replacestring from="$${{base}}" to="${{libraries.dir}}"/>
                            <escapeunicode/>
                        </filterchain>
                    </loadproperties>
                </target>
            </xsl:if>

            <target name="init-userdir">
                <xsl:attribute name="depends">pre-init,init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if></xsl:attribute>
                <property name="user.properties.file" location="${{netbeans.user}}/build.properties"/>
            </target>

            <target name="init-user">
                <xsl:attribute name="depends">pre-init,init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,init-userdir</xsl:attribute>
                <property file="${{user.properties.file}}"/>
            </target>

            <target name="init-project">
                <xsl:attribute name="depends">pre-init,init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,init-userdir,init-user</xsl:attribute>
                <property file="nbproject/project.properties"/>
            </target>

            <target name="do-init">
                <xsl:attribute name="depends">pre-init,init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,init-userdir,init-user,init-project</xsl:attribute>
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
                <condition property="do.compile.jsps">
                    <istrue value="${{compile.jsps}}"/>
                </condition>
                <condition property="do.display.browser">
                    <and>
                        <istrue value="${{display.browser}}"/>
                        <!-- See issue 107504 -->
                        <isset property="client.module.uri"/>
                        <not>
                            <isset property="app.client"/>
                        </not>
                    </and>
                </condition>
                <available property="has.custom.manifest" file="${{meta.inf}}/MANIFEST.MF"/>
                
                <xsl:comment>
                    Variables needed to support directory deployment.
                </xsl:comment>
                <condition property="do.package.with.custom.manifest.not.directory.deploy">
                    <and>
                        <isset property="has.custom.manifest"/>
                        <isfalse value="${{directory.deployment.supported}}"/>
                    </and>
                </condition>
                <condition property="do.package.without.custom.manifest.not.directory.deploy">
                    <and>
                        <not>
                            <isset property="has.custom.manifest"/>
                        </not>
                        <isfalse value="${{directory.deployment.supported}}"/>
                    </and>
                </condition>
                <condition property="do.package.not.directory.deploy">
                    <isfalse value="${{directory.deployment.supported}}"/>
                </condition>
                <xsl:comment>End Variables needed to support directory deployment.</xsl:comment>

                <condition property="j2ee.appclient.mainclass.tool.param" value="-mainclass ${{main.class}}" else="">
                    <and>
                        <isset property="main.class"/>
                        <not>
                            <equals arg1="${{main.class}}" arg2="" trim="true"/>
                        </not>
                    </and>
                </condition>
                <condition property="j2ee.appclient.jvmoptions.param" value="${{j2ee.appclient.jvmoptions}}" else="">
                    <and>
                        <isset property="j2ee.appclient.jvmoptions"/>
                        <not>
                            <equals arg1="${{j2ee.appclient.jvmoptions}}" arg2="" trim="true"/>
                        </not>
                    </and>
                </condition>
                <condition property="application.args.param" value="${{application.args}}" else="">
                    <and>
                        <isset property="application.args"/>
                        <not>
                            <equals arg1="${{application.args}}" arg2="" trim="true"/>
                        </not>
                    </and>
                </condition>
                <condition property="can.debug.appclient">
                    <and>
                        <isset property="netbeans.home"/>
                        <isset property="app.client"/>
                    </and>
                </condition>
                <path id="endorsed.classpath.path" path="${{endorsed.classpath}}"/>
                <condition property="endorsed.classpath.cmd.line.arg" value="-Xbootclasspath/p:'${{toString:endorsed.classpath.path}}'" else="">
                    <length length="0" string="${{endorsed.classpath}}" when="greater"/>
                </condition>
            </target>

            <!-- COS feature - used in run-deploy -->
            <target name="-init-cos">
                <xsl:attribute name="depends">init</xsl:attribute>
                <condition>
                    <!--
                    Default value is stored to differentiate the case
                    when this hasn't been called at all.
                    -->
                    <xsl:attribute name="property">build.deploy.on.save</xsl:attribute>
                    <xsl:attribute name="else">false</xsl:attribute>
                    <istrue value="${{j2ee.deploy.on.save}}"/>
                </condition>         
            </target>
            
            <target name="post-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="init-check">
                <xsl:attribute name="depends">pre-init,init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,init-userdir,init-user,init-project,do-init</xsl:attribute>
                <!-- XXX XSLT 2.0 would make it possible to use a for-each here -->
                <!-- Note that if the properties were defined in project.xml that would be easy -->
                <!-- But required props should be defined by the AntBasedProjectType, not stored in each project -->
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="build.generated.dir">Must set build.generated.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>
                <fail unless="dist.jar">Must set dist.jar</fail>
                <!-- No j2ee.platform.classpath here as it is used only for app client runtime -->
            </target>

            <target name="-init-taskdefs">
                <fail unless="libs.CopyLibs.classpath">
The libs.CopyLibs.classpath property is not set up.
This property must point to 
org-netbeans-modules-java-j2seproject-copylibstask.jar file which is part
of NetBeans IDE installation and is usually located at 
&lt;netbeans_installation&gt;/java&lt;version&gt;/ant/extra folder.
Either open the project in the IDE and make sure CopyLibs library
exists or setup the property manually. For example like this:
 ant -Dlibs.CopyLibs.classpath=a/path/to/org-netbeans-modules-java-j2seproject-copylibstask.jar
                </fail>
                <taskdef resource="org/netbeans/modules/java/j2seproject/copylibstask/antlib.xml" classpath="${{libs.CopyLibs.classpath}}"/>
            </target>

            <target name="init">
                <xsl:attribute name="depends">pre-init,init-private,init-userdir,init-user,init-project,do-init,post-init,init-check,-init-taskdefs</xsl:attribute>
            </target>

            <xsl:comment>
    COMPILATION SECTION
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
            </target>

            <target name="pre-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="do-compile">
                <xsl:attribute name="depends">init,deps-jar,pre-pre-compile,pre-compile,-do-compile-deps</xsl:attribute>
                
                <copy todir="${{build.dir}}/META-INF">
                  <fileset dir="${{meta.inf}}"/>
                </copy>
            </target>

            <target name="-do-compile-deps">
                <xsl:attribute name="depends">init,deps-jar,pre-pre-compile,pre-compile</xsl:attribute>

                <xsl:for-each select="/p:project/p:configuration/ear2:data/ear2:web-module-additional-libraries/ear2:library[ear2:path-in-war]">
                    <xsl:variable name="copyto" select=" ear2:path-in-war"/>
                    <xsl:variable name="file" select=" ear2:file"/>
                    <copyfiles todir="${{build.dir}}/META-INF/lib">
                       <xsl:attribute name="todir"><xsl:value-of select="concat('${build.dir}/',$copyto)"/></xsl:attribute>
                       <xsl:attribute name="files"><xsl:value-of select="$file"/></xsl:attribute>
                    </copyfiles>
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

            <xsl:comment>
                DIST BUILDING SECTION
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

            <xsl:comment>
                TARGETS NEEDED TO SUPPORT DIRECTORY DEPLOYMENT
            </xsl:comment>

            <target name="-do-tmp-dist-without-manifest">
                <xsl:attribute name="depends">init,compile,pre-dist</xsl:attribute>
                <xsl:attribute name="if">do.package.without.custom.manifest.not.directory.deploy</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}">
                    <fileset dir="${{build.dir}}"/>
                </jar>
            </target>

            <target name="-do-tmp-dist-with-manifest">
                <xsl:attribute name="depends">init,compile,pre-dist</xsl:attribute>
                <xsl:attribute name="if">do.package.with.custom.manifest.not.directory.deploy</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}" manifest="${{meta.inf}}/MANIFEST.MF">
                    <fileset dir="${{build.dir}}"/>
                </jar>
            </target>

            <target name="-do-dist-directory-deploy" depends="init,compile,pre-dist,-do-tmp-dist-without-manifest,-do-tmp-dist-with-manifest"/>
            <target name="dist-directory-deploy">
                <xsl:attribute name="depends">init,compile,pre-dist,-do-dist-directory-deploy,post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution (JAR) - if directory deployment is not supported.</xsl:attribute>
            </target>
            <xsl:comment>
                END TARGETS NEEDED TO SUPPORT DIRECTORY DEPLOYMENT
            </xsl:comment>
            
            <target name="post-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="dist">
                <xsl:attribute name="depends">init,compile,pre-dist,do-dist-without-manifest,do-dist-with-manifest,post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution (JAR).</xsl:attribute>
            </target>

            <xsl:comment>
    EXECUTION SECTION
    </xsl:comment>
    <target name="run">
        <xsl:attribute name="depends">run-deploy,run-display-browser,run-ac</xsl:attribute>
        <xsl:attribute name="description">Deploy to server.</xsl:attribute>
    </target>
            
    <target name="pre-run-deploy">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>

    <target name="post-run-deploy">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>
    
    <target name="-pre-nbmodule-run-deploy">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> This target can be overriden by NetBeans modules. Don't override it directly, use -pre-run-deploy task instead. </xsl:comment>
    </target>
            
    <target name="-post-nbmodule-run-deploy">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> This target can be overriden by NetBeans modules. Don't override it directly, use -post-run-deploy task instead. </xsl:comment>
    </target>

    <target name="-run-deploy-am" unless="no.deps">
        <xsl:comment> Task to deploy to the Access Manager runtime. </xsl:comment>
        <xsl:call-template name="am.target">
	    <xsl:with-param name="targetname" select="'-run-deploy-am'"/>
        </xsl:call-template>
    </target>
            
    <target name="run-deploy">
        <xsl:attribute name="depends">-init-cos,dist-directory-deploy,pre-run-deploy,-pre-nbmodule-run-deploy,-run-deploy-nb,-init-deploy-ant,-deploy-ant,-run-deploy-am,-post-nbmodule-run-deploy,post-run-deploy</xsl:attribute>
    </target>

    <target name="-run-deploy-nb" if="netbeans.home">
        <nbdeploy debugmode="false" forceRedeploy="${{forceRedeploy}}" clientUrlPart="${{client.urlPart}}" clientModuleUri="${{client.module.uri}}"/>
    </target>
    
    <target name="-init-deploy-ant" unless="netbeans.home">
        <property name="deploy.ant.archive" value="${{dist.jar}}"/>
        <property name="deploy.ant.resource.dir" value="${{resource.dir}}"/>
        <property name="deploy.ant.enabled" value="true"/>
    </target>

    <target name="run-undeploy">
        <xsl:attribute name="depends">dist,-run-undeploy-nb,-init-deploy-ant,-undeploy-ant</xsl:attribute>
    </target>

    <target name="-run-undeploy-nb" if="netbeans.home">
        <fail message="Undeploy is not supported from within the IDE"/>
    </target>
    
    <target name="verify">
        <xsl:attribute name="depends">dist</xsl:attribute>
        <nbverify file="${{dist.jar}}"/>
    </target>
    
    <target name="run-display-browser">
        <xsl:attribute name="depends">run-deploy,-init-display-browser,-display-browser-nb,-display-browser-cl</xsl:attribute>
    </target>
    
    <target name="-init-display-browser" if="do.display.browser">
        <condition property="do.display.browser.nb">
            <isset property="netbeans.home"/>
        </condition>
        <condition property="do.display.browser.cl">
            <and>
                <isset property="deploy.ant.enabled"/>
                <isset property="deploy.ant.client.url"/>
            </and>
        </condition>
    </target>

    <target name="-display-browser-nb" if="do.display.browser.nb">
        <nbbrowse url="${{client.url}}"/>
    </target>

    <target name="-get-browser" if="do.display.browser.cl" unless="browser">
        <condition property="browser" value="rundll32">
            <os family="windows"/>
        </condition>
        <condition property="browser.args" value="url.dll,FileProtocolHandler" else="">
            <os family="windows"/>
        </condition>
        <condition property="browser" value="/usr/bin/open">
            <os family="mac"/>
        </condition>
        <property environment="env"/>
        <condition property="browser" value="${{env.BROWSER}}">
            <isset property="env.BROWSER"/>
        </condition>
        <condition property="browser" value="/usr/bin/firefox">
            <available file="/usr/bin/firefox"/>
        </condition>
        <condition property="browser" value="/usr/local/firefox/firefox">
            <available file="/usr/local/firefox/firefox"/>
        </condition>
        <condition property="browser" value="/usr/bin/mozilla">
            <available file="/usr/bin/mozilla"/>
        </condition>
        <condition property="browser" value="/usr/local/mozilla/mozilla">
            <available file="/usr/local/mozilla/mozilla"/>
        </condition>
        <condition property="browser" value="/usr/sfw/lib/firefox/firefox">
            <available file="/usr/sfw/lib/firefox/firefox"/>
        </condition>
        <condition property="browser" value="/opt/csw/bin/firefox">
            <available file="/opt/csw/bin/firefox"/>
        </condition>
        <condition property="browser" value="/usr/sfw/lib/mozilla/mozilla">
            <available file="/usr/sfw/lib/mozilla/mozilla"/>
        </condition>
        <condition property="browser" value="/opt/csw/bin/mozilla">
            <available file="/opt/csw/bin/mozilla"/>
        </condition>
    </target>

    <target name="-display-browser-cl" depends="-get-browser" if="do.display.browser.cl">
        <fail unless="browser">
            Browser not found, cannot launch the deployed application. Try to set the BROWSER environment variable.
        </fail>
        <property name="browse.url" value="${{deploy.ant.client.url}}${{client.urlPart}}"/>
        <echo>Launching ${browse.url}</echo>
        <exec executable="${{browser}}" spawn="true">
            <arg line="${{browser.args}} ${{browse.url}}"/>
        </exec>
    </target>
    
    <!-- application client execution -->
    <xsl:call-template name="run.target">
        <xsl:with-param name="id" select="'j2ee-module-car'"/>
        <xsl:with-param name="type" select="'j2ee_ear_archive'"/>
    </xsl:call-template>

    <xsl:comment>
    DEBUGGING SECTION
    </xsl:comment>
    <target name="debug">
        <xsl:attribute name="depends">run-debug,run-debug-appclient</xsl:attribute>
        <xsl:attribute name="description">Deploy to server.</xsl:attribute>
    </target>
    <target name="run-debug">
        <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
        <xsl:attribute name ="depends">dist</xsl:attribute>
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <xsl:attribute name="unless">app.client</xsl:attribute>
        <nbdeploy debugmode="true" clientUrlPart="${{client.urlPart}}" clientModuleUri="${{client.module.uri}}"/>
        <antcall target="connect-debugger"/>
        <antcall target="debug-display-browser"/>
    </target>

    <target name="connect-debugger" unless="is.debugged">
        <nbjpdaconnect name="${{name}}" host="${{jpda.host}}" address="${{jpda.address}}" transport="${{jpda.transport}}">
            <classpath>
                <path path="${{debug.classpath}}"/>
                <fileset dir="${{build.dir}}" includes="lib/*.jar"/>
            </classpath>
            <sourcepath>
                <path path="${{ear.docbase.dirs}}"/>
            </sourcepath>
            <xsl:if test="/p:project/p:configuration/ear2:data/ear2:explicit-platform">
            <bootclasspath>
                <path path="${{platform.bootcp}}"/>
            </bootclasspath>
            </xsl:if>
        </nbjpdaconnect>
    </target>
    <!-- fix for issue 119066 -->
    <target name="debug-display-browser" if="do.display.browser">
        <nbbrowse url="${{client.url}}"/>
    </target>

    <!-- application client debugging -->
    <xsl:call-template name="debug.target">
        <xsl:with-param name="id" select="'j2ee-module-car'"/>
        <xsl:with-param name="type" select="'j2ee_ear_archive'"/>
    </xsl:call-template>
    
            <xsl:comment>
    CLEANUP SECTION
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

            <target name="undeploy-clean">
                <xsl:attribute name="depends">init</xsl:attribute>
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                
                <nbundeploy failOnError="false" startServer="false"/>
            </target>
            
            <target name="post-clean">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="clean">
                <xsl:attribute name="depends">init,undeploy-clean,deps-clean,do-clean,post-clean</xsl:attribute>
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
                <!--
                If build.deploy.on.save is not set init-cos hasn't
                been called so we are running the old style build.
                -->
                <condition>
                    <xsl:attribute name="property">build.deploy.on.save</xsl:attribute>
                    <xsl:attribute name="value">false</xsl:attribute>
                    <not><isset property="build.deploy.on.save"/></not>
                </condition>
                <ant target="{$subtarget}" inheritall="false" antfile="${{project.{$subproj}}}/{$script}">                   
                    <property name="dist.ear.dir" location="${{build.dir}}"/>
                    <xsl:choose>
                        <xsl:when test="$subtarget = 'jar'">
                            <property name="deploy.on.save" value="false"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <property name="deploy.on.save" value="${{build.deploy.on.save}}"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </ant>
            </xsl:for-each>
            <xsl:variable name="references2" select="/p:project/p:configuration/projdeps2:references"/>
            <xsl:for-each select="$references2/projdeps2:reference[not($type) or projdeps2:artifact-type = $type]">
                <xsl:variable name="subtarget">
                    <xsl:choose>
                        <xsl:when test="$type">
                            <xsl:value-of select="projdeps2:target"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="projdeps2:clean-target"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="script" select="projdeps2:script"/>
                <!--
                If build.deploy.on.save is not set init-cos hasn't
                been called so we are running the old style build.
                -->
                <condition>
                    <xsl:attribute name="property">build.deploy.on.save</xsl:attribute>
                    <xsl:attribute name="value">false</xsl:attribute>
                    <not><isset property="build.deploy.on.save"/></not>
                </condition>
                <ant target="{$subtarget}" inheritall="false" antfile="{$script}">
                    <property name="dist.ear.dir" location="${{build.dir}}"/>
                    <xsl:for-each select="projdeps2:properties/projdeps2:property">
                        <property name="{@name}" value="{.}"/>
                        <xsl:choose>
                            <xsl:when test="$subtarget = 'jar'">
                                <property name="deploy.on.save" value="false"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <property name="deploy.on.save" value="${{build.deploy.on.save}}"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </ant>
            </xsl:for-each>
        </target>
    </xsl:template>

    
    <!-- Template to generate run target(s) for AC -->
    <xsl:template name="run.target">
        <xsl:param name="id"/>
        <xsl:param name="type"/>
        <target name="run-ac" depends="init" if="app.client">
            <antcall target="run-${{app.client}}"/>
        </target>
        <xsl:variable name="references" select="/p:project/p:configuration/projdeps:references"/>
        <xsl:variable name="name" select="/p:project/p:configuration/ear2:data/ear2:name"/>
        <xsl:for-each select="$references/projdeps:reference[not($type) or (projdeps:artifact-type = $type and projdeps:id = $id)]">
            <xsl:variable name="subprojname" select="projdeps:foreign-project"/>
            <xsl:call-template name="runTargets">
                <xsl:with-param name="subprojname" select="$subprojname"/>
                <xsl:with-param name="name" select="$name"/>
            </xsl:call-template>
        </xsl:for-each>
        <xsl:variable name="references2" select="/p:project/p:configuration/projdeps2:references"/>
        <xsl:for-each select="$references2/projdeps2:reference[not($type) or (projdeps2:artifact-type = $type and projdeps2:id = $id)]">
            <xsl:variable name="subprojname" select="projdeps2:foreign-project"/>
            <xsl:call-template name="runTargets">
                <xsl:with-param name="subprojname" select="$subprojname"/>
                <xsl:with-param name="name" select="$name"/>
            </xsl:call-template>
        </xsl:for-each>
        
        <!--
           Idea is to add new non-mandatory option to nbdeploy task. This
           option should be a replacement for asadmin deploy -retrieve local_dir
           command. See also http://www.netbeans.org/issues/show_bug.cgi?id=82929.
        -->
        <target name="-as-retrieve-option-workaround">
            <xsl:attribute name="if">j2ee.appclient.mainclass.args</xsl:attribute>
            <xsl:attribute name="unless">j2ee.clientName</xsl:attribute>
            <property name="client.jar" value="${{dist.dir}}/{$name}Client.jar"/>
            <sleep seconds="3"/>
            <copy file="${{wa.copy.client.jar.from}}/{$name}/{$name}Client.jar" todir="${{dist.dir}}" failonerror="false"/>                
            <copy todir="${{dist.dir}}/" flatten="true" failonerror="false">
                <fileset dir="${{wa.copy.client.jar.from}}/{$name}" includes="**/{$name}Client.jar"/>
            </copy>
            <copy todir="${{dist.dir}}/{$name}Client" flatten="true">
                <fileset dir="${{wa.copy.client.jar.from}}/{$name}" includes="**/*.*ar"/>
            </copy>
            <copy todir="${{dist.dir}}/{$name}Client" flatten="false" failonerror="false">
                <fileset dir="${{dist.dir}}/gfdeploy/{$name}" includes="**/*.jar"/>
            </copy>
        </target>
    </xsl:template>

    <xsl:template name="runTargets">
        <xsl:param name="subprojname"/>
        <xsl:param name="name"/>
        <target name="run-{$subprojname}" depends="-tool2-{$subprojname},-old-java-{$subprojname}"/>
        <target name="-old-java-{$subprojname}" if="j2ee.appclient.tool.args">
            <antcall target="-tool-{$subprojname}"/>
            <antcall target="-java-{$subprojname}"/>
        </target>
        <target name="-tool-{$subprojname}" unless="j2ee.clientName" if="j2ee.appclient.mainclass.args" depends="-as-retrieve-option-workaround">
            <java fork="true" classname="${{j2ee.appclient.tool.mainclass}}">
                <xsl:if test="/p:project/p:configuration/ear2:data/ear2:explicit-platform">
                    <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                </xsl:if>
                <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                <jvmarg line="${{j2ee.appclient.tool.jvmoptions}}"/>
                <jvmarg line="${{j2ee.appclient.jvmoptions.param}}"/>
                <arg line="${{j2ee.appclient.tool.args}}"/>
                <arg line="-client ${{client.jar}}"/>
                <arg line="${{j2ee.appclient.mainclass.tool.param}}"/>
                <arg line="${{application.args.param}}"/>
                <classpath>
                    <path path="${{j2ee.platform.classpath}}:${{j2ee.appclient.tool.runtime}}"/>
                </classpath>
                <syspropertyset>
                    <propertyref prefix="run-sys-prop."/>
                    <mapper type="glob" from="run-sys-prop.*" to="*"/>
                </syspropertyset>
            </java>
        </target>
        <target name="-tool2-{$subprojname}" unless="j2ee.appclient.tool.args" if="j2ee.appclient.mainclass.args" depends="run-deploy,-as-retrieve-option-workaround">
            <mkdir dir="${{dist.dir}}/{$name}Client"/>
            <copy todir="${{dist.dir}}/{$name}Client" flatten="true">
                <fileset dir="${{wa.copy.client.jar.from}}/{$name}" includes="**/*.*ar"/>
            </copy>
            <java fork="true" jar="${{client.jar}}">
                <xsl:attribute name="dir">${basedir}</xsl:attribute>
                <xsl:if test="/p:project/p:configuration/ear2:data/ear2:explicit-platform">
                    <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                </xsl:if>
                    <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                    <jvmarg line="${{j2ee.appclient.tool.jvmoptions}}${{client.jar}},arg=-name,arg={$subprojname}"/>
                    <arg line="${{application.args.param}}"/>
                    <syspropertyset>
                        <propertyref prefix="run-sys-prop."/>
                        <mapper type="glob" from="run-sys-prop.*" to="*"/>
                    </syspropertyset>
            </java>
        </target>
        <target name="-java-{$subprojname}" if="j2ee.clientName" unless="j2ee.appclient.mainclass.args">
            <java fork="true" classname="${{main.class}}">
                <xsl:if test="/p:project/p:configuration/ear2:data/ear2:explicit-platform">
                    <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                </xsl:if>
                <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                <jvmarg line="${{j2ee.appclient.tool.jvmoptions}}"/>
                <jvmarg line="-Dj2ee.clientName=${{app.client}}"/>
                <jvmarg line="${{j2ee.appclient.jvmoptions.param}}"/>
                <arg line="${{application.args.param}}"/>
                <classpath>
                    <path path="${{jar.content.additional}}:${{j2ee.platform.classpath}}:${{j2ee.appclient.tool.runtime}}"/>
                </classpath>
                <syspropertyset>
                    <propertyref prefix="run-sys-prop."/>
                    <mapper type="glob" from="run-sys-prop.*" to="*"/>
                </syspropertyset>
            </java>
        </target>
    </xsl:template>

    <xsl:template name="debug.target">
        <xsl:param name="id"/>
        <xsl:param name="type"/>
        <target name="-init-debug-args">
            <xsl:choose>
                <xsl:when test="/p:project/p:configuration/ear2:data/ear2:explicit-platform">
                    <exec executable="${{platform.java}}" outputproperty="version-output">
                        <arg value="-version"/>
                    </exec>
                </xsl:when>
                <xsl:otherwise>
                    <property name="version-output" value="java version &quot;${{ant.java.version}}"/>
                </xsl:otherwise>
            </xsl:choose>
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
        <target name="run-debug-appclient" depends="init,-init-debug-args" if="can.debug.appclient">
                <macrodef>
                    <xsl:attribute name="name">debug-appclient</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-earproject/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">mainclass</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <attribute>
                        <xsl:attribute name="name">args</xsl:attribute>
                        <xsl:attribute name="default">${application.args.param}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <parallel>
                            <java fork="true" classname="@{{mainclass}}">
                                <xsl:if test="/p:project/p:configuration/ear2:data/ear2:explicit-platform">
                                    <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                                    <bootclasspath>
                                        <path path="${{platform.bootcp}}"/>
                                    </bootclasspath>
                                </xsl:if>
                                <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                                <jvmarg line="${{j2ee.appclient.tool.jvmoptions}}"/>
                                <jvmarg line="${{debug-args-line}}"/>
                                <jvmarg value="-Xrunjdwp:transport=${{jpda.transport}},server=y,address=${{jpda.address}},suspend=y"/>
                                <jvmarg line="${{j2ee.appclient.jvmoptions.param}}"/>
                                <arg line="@{{args}}"/>
                                <classpath>
                                    <path path="${{j2ee.platform.classpath}}:${{j2ee.appclient.tool.runtime}}"/>
                                    <path path="@{{classpath}}"/>
                                </classpath>
                                <syspropertyset>
                                    <propertyref prefix="run-sys-prop."/>
                                    <mapper type="glob" from="run-sys-prop.*" to="*"/>
                                </syspropertyset>
                                <customize/>
                            </java>
                            <nbjpdaconnect name="${{name}}" host="${{jpda.host}}" address="${{jpda.address}}" transport="${{jpda.transport}}">
                                <classpath>
                                    <path path="${{j2ee.platform.classpath}}:${{j2ee.appclient.tool.runtime}}"/>
                                    <path path="@{{classpath}}"/>
                                </classpath>
                                <sourcepath>
                                    <path path="${{src.dir}}"/>
                                </sourcepath>
                            </nbjpdaconnect>
                        </parallel>
                    </sequential>
                </macrodef>
            <nbdeploy debugmode="false" clientUrlPart="${{client.urlPart}}" clientModuleUri="${{client.module.uri}}"/>
            <antcall target="debug-${{app.client}}"/>
        </target>
        <xsl:variable name="references" select="/p:project/p:configuration/projdeps:references"/>
        <xsl:variable name="name" select="/p:project/p:configuration/ear2:data/ear2:name"/>
        <xsl:for-each select="$references/projdeps:reference[not($type) or (projdeps:artifact-type = $type and projdeps:id = $id)]">
            <xsl:variable name="subprojname" select="projdeps:foreign-project"/>
            <target name="debug-{$subprojname}" depends="-debug-tool-{$subprojname},-debug-java-{$subprojname}"/>
            <target name="-debug-tool-{$subprojname}" unless="j2ee.clientName" if="j2ee.appclient.mainclass.args" depends="init,-as-retrieve-option-workaround">
                <ear2:debug-appclient mainclass="${{j2ee.appclient.tool.mainclass}}" args="-client ${{client.jar}} ${{j2ee.appclient.tool.args}} ${{j2ee.appclient.mainclass.tool.param}} ${{application.args.param}}"/>
            </target>
            <target name="-debug-java-{$subprojname}" if="j2ee.clientName" unless="j2ee.appclient.mainclass.args">
                <ear2:debug-appclient mainclass="${{main.class}}" args="${{application.args.param}}" classpath="${{jar.content.additional}}"/>
            </target>
        </xsl:for-each>
        <xsl:variable name="references2" select="/p:project/p:configuration/projdeps2:references"/>
        <xsl:for-each select="$references2/projdeps2:reference[not($type) or (projdeps2:artifact-type = $type and projdeps2:id = $id)]">
            <xsl:variable name="subprojname" select="projdeps2:foreign-project"/>
            <target name="debug-{$subprojname}" depends="-debug-tool-{$subprojname},-debug-java-{$subprojname}"/>
            <target name="-debug-tool-{$subprojname}" unless="j2ee.clientName" if="j2ee.appclient.mainclass.args" depends="init,-as-retrieve-option-workaround">
                <ear2:debug-appclient mainclass="${{j2ee.appclient.tool.mainclass}}" args="-client ${{client.jar}} ${{j2ee.appclient.tool.args}} ${{j2ee.appclient.mainclass.tool.param}} ${{application.args.param}}"/>
            </target>
            <target name="-debug-java-{$subprojname}" if="j2ee.clientName" unless="j2ee.appclient.mainclass.args">
                <ear2:debug-appclient mainclass="${{main.class}}" args="${{application.args.param}}" classpath="${{jar.content.additional}}"/>
            </target>
        </xsl:for-each>
    </xsl:template>
    
    <!---
    Access Manager deploy template to build subdependencies.
    @return an Ant target which invokes the Access Manager deployment
    for all known subprojects
    -->
    <xsl:template name="am.target">
        <xsl:variable name="references" select="/p:project/p:configuration/projdeps:references"/>
        <xsl:for-each select="$references/projdeps:reference[(projdeps:id='dist-ear') or (projdeps:id='j2ee-module-car')]">
            <xsl:variable name="subproj" select="projdeps:foreign-project"/>
            <xsl:variable name="script" select="projdeps:script"/>
            <ant target="-run-deploy-am" inheritall="false" antfile="${{project.{$subproj}}}/{$script}">
            </ant>
        </xsl:for-each>
        <xsl:variable name="references2" select="/p:project/p:configuration/projdeps2:references"/>
        <xsl:for-each select="$references2/projdeps2:reference[(projdeps2:id='dist-ear') or (projdeps2:id='j2ee-module-car')]">
            <xsl:variable name="script" select="projdeps2:script"/>
            <ant target="-run-deploy-am" inheritall="false" antfile="{$script}">
            </ant>
        </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>
