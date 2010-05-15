<?xml version="1.0" encoding="UTF-8"?>
<!--
The contents of this file are subject to the terms of the Common Development
and Distribution License (the License). You may not use this file except in
compliance with the License.

You can obtain a copy of the License at http://www.netbeans.org/cddl.html
or http://www.netbeans.org/cddl.txt.

When distributing Covered Code, include this CDDL Header Notice in each file
and include the License file at http://www.netbeans.org/cddl.txt.
If applicable, add the following below the CDDL Header, with the fields
enclosed by brackets [] replaced by your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:ejb="http://www.netbeans.org/ns/j2ee-ejbjarproject/1"
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

        <xsl:variable name="name" select="/p:project/p:configuration/ejb:data/ejb:name"/>
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
                <xsl:if test="/p:project/p:configuration/ejb:data/ejb:explicit-platform">
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
                <xsl:if test="/p:project/p:configuration/ejb:data/ejb:use-manifest">
                    <fail unless="manifest.file">Must set manifest.file</fail>
                </xsl:if>
                <condition property="no.javadoc.preview">
                    <isfalse value="${{javadoc.preview}}"/>
                </condition>
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
                <fail unless="build.generated.dir">Must set build.generated.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.dir">Must set build.classes.dir</fail>
<!-- //B20050104                 <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail> -->
<!-- //B20050104                 <fail unless="build.classes.excludes">Must set build.classes.excludes</fail> -->
                <fail unless="dist.jar">Must set dist.jar</fail>
            </target>
<!-- //B20050104 
            <target name="init-macrodef-javac">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-ejbjarproject/1</xsl:attribute>
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
                        <property name="javac.compilerargs" value=""/>
                        <javac>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:attribute name="source">${javac.source}</xsl:attribute>
                            <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/ejb:data/ejb:explicit-platform">
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
                            <xsl:if test="/p:project/p:configuration/ejb:data/ejb:explicit-platform">
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
                            <fileset includes="${{fix.classes}}" dir="@{{dir}}" >
                                <include name="${{fix.includes}}*.class"/>
                            </fileset>
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
                            <xsl:if test="/p:project/p:configuration/ejb:data/ejb:explicit-platform">
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
//B20050104 -->            
            <target name="init-taskdefs">
		<taskdef name="generate-jbi-descriptors" classname="com.sun.jbi.ui.devtool.anttasks.GenerateJBIDescriptors">
                        <classpath path="C:/darkhorse/nbbuild/netbeans/enterprisepack1/modules/org-netbeans-modules-compapp-projects-base.jar"/>
		</taskdef>
		<taskdef name="generate-deployment-path" classname="com.sun.jbi.ui.devtool.anttasks.DeployJBIArtifacts">
                        <classpath path="C:/darkhorse/nbbuild/netbeans/enterprisepack1/modules/org-netbeans-modules-compapp-projects-base.jar:${{netbeans.home}}\..\ide5\modules\autoload\ext\xerces-2.6.2.jar;${{netbeans.home}}\..\ide5\modules\autoload\ext\xml-commons-dom-ranges-1.0.b2.jar"/>
		</taskdef>
            </target>
            
            <target name="init">
<!-- //B20050104                <xsl:attribute name="depends">pre-init,init-private,init-userdir,init-user,init-project,do-init,post-init,init-check,init-macrodef-javac,init-macrodef-nbjpda,init-macrodef-debug,init-taskdefs</xsl:attribute> -->
                <xsl:attribute name="depends">pre-init,init-private,init-userdir,init-user,init-project,do-init,post-init,init-check,init-taskdefs</xsl:attribute>
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

            <xsl:if test="/p:project/p:configuration/ejb:data/ejb:web-services/ejb:web-service|/p:project/p:configuration/ejb:data/ejb:web-service-clients/ejb:web-service-client">
				<target name="wscompile-init">
					<taskdef name="wscompile" classname="com.sun.xml.rpc.tools.ant.Wscompile">
					  <classpath path="${{wscompile.classpath}}"/>
					</taskdef>
					<mkdir dir="${{build.classes.dir}}/META-INF/wsdl"/>
					<mkdir dir="${{build.generated.dir}}/wssrc"/>
				</target>
			</xsl:if>

            <xsl:for-each select="/p:project/p:configuration/ejb:data/ejb:web-services/ejb:web-service">
              <xsl:variable name="wsname">
                <xsl:value-of select="ejb:web-service-name"/>
              </xsl:variable>

              <target name="{$wsname}_wscompile" depends="wscompile-init">
                <wscompile
                   server="true"
                   fork="true"
                   keep="true"
                   base="${{build.generated.dir}}/wssrc"
                   xPrintStackTrace="true"
                   verbose="true"
                   nonClassDir="${{build.classes.dir}}/META-INF/wsdl"
                   classpath="${{wscompile.classpath}}:${{build.classes.dir}}"
                   mapping="${{build.classes.dir}}/META-INF/wsdl/${{{$wsname}.mapping}}"
                   config="${{src.dir}}/${{{$wsname}.config.name}}">
                   <!-- HTTPProxy="${http.proxyHost}:${http.proxyPort}" -->
                </wscompile>
              </target>
            </xsl:for-each>

			<xsl:for-each select="/p:project/p:configuration/ejb:data/ejb:web-service-clients/ejb:web-service-client">
				<xsl:variable name="wsclientname">
					<xsl:value-of select="ejb:web-service-client-name"/>
				</xsl:variable>

				<target name="{$wsclientname}_client_wscompile" depends="wscompile-init">
					<copy file="${{web.docbase.dir}}/WEB-INF/wsdl/{$wsclientname}-config.xml"
						tofile="${{build.generated.dir}}/wssrc/wsdl/{$wsclientname}-config.xml" filtering="on">
						<filterset>
							<!-- replace token with reference to WSDL file in source tree, not build tree, since the
							     the file probably has not have been copied to the build tree yet. -->
							<filter token="CONFIG_ABSOLUTE_PATH" value="${{basedir}}/${{web.docbase.dir}}/WEB-INF/wsdl"/>
						</filterset>
					</copy>
					<wscompile
						xPrintStackTrace="true" verbose="true"
						fork="true" keep="true" import="true" features="norpcstructures"
						base="${{build.classes.dir}}"
						sourceBase="${{build.generated.dir}}/wssrc"
						classpath="${{wscompile.classpath}}"
						mapping="${{build.web.dir}}/WEB-INF/wsdl/{$wsclientname}-mapping.xml"
						config="${{build.generated.dir}}/wssrc/wsdl/{$wsclientname}-config.xml">
					</wscompile>
				</target>
			</xsl:for-each>

            <target name="pre-pre-compile">
                <xsl:attribute name="depends">init,deps-jar</xsl:attribute>
                <mkdir dir="${{build.classes.dir}}"/>
            </target>

            <target name="pre-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="library-inclusion-in-archive" depends="compile">
                <xsl:for-each select="//ejb:included-library">
                    <xsl:variable name="included.prop.name">
                        <xsl:value-of select="."/>
                    </xsl:variable>
                    <unjar dest="${{build.classes.dir}}">
                        <xsl:attribute name="src">${<xsl:value-of select="$included.prop.name"/>}</xsl:attribute>
                    </unjar>
                </xsl:for-each>   
            </target> 
            
            <target name="library-inclusion-in-manifest" depends="compile">
                <xsl:for-each select="//ejb:included-library">
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
                     <copy todir="${{build.classes.dir}}">
                        <xsl:attribute name="file">${<xsl:value-of select="$included.prop.name"/>}</xsl:attribute>
                     </copy>
                </xsl:for-each>   
                <manifest file="${{build.classes.dir}}/META-INF/MANIFEST.MF" mode="update">
                    <attribute>
                        <xsl:attribute name="name">Class-Path</xsl:attribute>
                        <xsl:attribute name="value">
                            <xsl:for-each select="//ejb:included-library">
                                <xsl:variable name="base.prop.name">
                                    <xsl:value-of select="concat('${included.lib.', ., '}')"/>
                                </xsl:variable>
                                <xsl:if test="position()>1">,</xsl:if>
                                <xsl:value-of select="$base.prop.name"/>
                            </xsl:for-each>  
                        </xsl:attribute>
                     </attribute>
                </manifest>
            </target>
            
            <target name="do-compile">
                <xsl:attribute name="depends">init,deps-jar,pre-pre-compile,pre-compile</xsl:attribute>
                <xsl:if test="/p:project/p:configuration/ejb:data/ejb:web-services/ejb:web-service">
                    <xsl:comment>For web services, refresh the Tie and SerializerRegistry classes</xsl:comment> 
                    <delete> 
                      <fileset dir="${{build.classes.dir}}" includes="**/*_Tie.* **/*_SerializerRegistry.*"/>
                    </delete>
                </xsl:if>
<!-- 
                //B20050104                
                <ejbproject:javac xmlns:ejbproject="http://www.netbeans.org/ns/j2ee-ejbjarproject/1"/>
                <copy todir="${{build.classes.dir}}">
                    <fileset dir="${{src.dir}}" excludes="${{build.classes.excludes}}"/>
                    <fileset dir="${{meta.inf}}" includes="**/*.dbschema"/>
                </copy>
                <copy todir="${{build.classes.dir}}/META-INF">
                  <fileset dir="${{meta.inf}}" excludes="**/*.dbschema"/> 
                </copy>
                <xsl:if test="/p:project/p:configuration/ejb:data/ejb:web-services/ejb:web-service">
                    <xsl:comment>For web services, refresh ejb-jar.xml and sun-ejb-jar.xml</xsl:comment>  
                    <copy todir="${{build.dir}}" overwrite="true"> 
                      <fileset includes="META-INF/ejb-jar.xml META-INF/sun-ejb-jar.xml" dir="${{meta.inf}}"/>
                    </copy>
                 </xsl:if>
-->                 
            </target>

            <target name="post-compile">
                <xsl:if test="/p:project/p:configuration/ejb:data/ejb:web-services/ejb:web-service">
					<xsl:attribute name="depends">
						<xsl:for-each select="/p:project/p:configuration/ejb:data/ejb:web-services/ejb:web-service">
							<xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
							<xsl:variable name="wsname2">
								<xsl:value-of select="ejb:web-service-name"/>
							</xsl:variable>
							<xsl:value-of select="ejb:web-service-name"/><xsl:text>_wscompile</xsl:text>
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
                <ejbproject:javac xmlns:ejbproject="http://www.netbeans.org/ns/j2ee-ejbjarproject/1">
                    <customize>
                        <include name="${{javac.includes}}"/>
                    </customize>
                </ejbproject:javac>
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
                <mkdir dir="${{build.dir}}"/>
            </target>

            <target name="dist_se">
                <xsl:attribute name="depends">init,pre-dist</xsl:attribute>
                <jar compress="${{jar.compress}}" jarfile="${{build.dir}}/SEDeployment.jar">
                    <fileset includes="**/*.bpel,**/*.wsdl,**/*.xsd" dir="${{src.dir}}"/>
                    <fileset dir="${{basedir}}/${{src.dir}}">
                        <include name="portmap.xml" />
                    </fileset>
                </jar>
             </target>

            <target name="do-dist">
                <xsl:attribute name="depends">init,pre-dist, dist_se</xsl:attribute>
            </target>
            <target name="post-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
                <!--
                <generate-deployment-path
                    jarFileClasspath="C:/darkhorse/nbbuild/netbeans/enterprisepack1/modules/org-netbeans-modules-compapp-projects-base.jar"
                    privatePropertiesFileLocation="${{basedir}}/nbproject/private/private.properties"
                    netBeansUserPropertyValue="${{netbeans.user}}">
                </generate-deployment-path>
                -->
            </target>

            <target name="dist">
                <xsl:attribute name="depends">init,pre-dist,do-dist,post-dist</xsl:attribute>
<!--
                <xsl:attribute name="depends">init,compile,pre-dist,do-dist,post-dist,library-inclusion-in-manifest</xsl:attribute>
-->                
                <xsl:attribute name="description">Build distribution (JAR).</xsl:attribute>
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
        <xsl:attribute name="depends">init,init-deploy,compile,library-inclusion-in-archive</xsl:attribute>

        <generate-deployment-path
            jarFileClasspath="C:/darkhorse/nbbuild/netbeans/enterprisepack1/modules/org-netbeans-modules-compapp-projects-base.jar"
            privatePropertiesFileLocation="${{basedir}}/nbproject/private/private.properties"
            netBeansUserPropertyValue="${{netbeans.user}}">
        </generate-deployment-path>
        
        <!--
        <exec dir="${{basedir}}" executable="C:\Alaska\root\jbi\runtime\Sun\AppServer\bin\asant.bat">
            <arg value="-f"/>
            <arg value="C:\Alaska\root\jbi\runtime\Sun\AppServer\bin\jbi_admin.xml"/>
            <arg value="-Djbi.host=localhost" />
            <arg value="-Djbi.port=5555" />
            <arg value="-Djbi.deploymentId=MyGUID" />
            <arg value="undeploy"/>
        </exec>
        
        
        <exec dir="${{basedir}}" executable="C:\Alaska\root\jbi\runtime\Sun\AppServer\bin\asant.bat">
            <arg value="-f"/>
            <arg value="C:\Alaska\root\jbi\runtime\Sun\AppServer\bin\jbi_admin.xml"/>
            <arg value="-Djbi.host=localhost" />
            <arg value="-Djbi.port=5555" />
            <arg value="-Djbi.deploymentJar=${{basedir}}/${{dist.jar}}" />
            <arg value="deploy"/>
        </exec>
        -->
        <exec dir="${{basedir}}" executable="${{com.sun.jbi.ui.devtool.appserver.instance.location}}/bin/asant.bat">
            <arg value="-f"/>
            <arg value="${{com.sun.jbi.ui.devtool.appserver.instance.location}}/bin/jbi_admin.xml"/>
            <arg value="-Djbi.host=${{com.sun.jbi.ui.devtool.appserver.instance.hostName}}" />
            <arg value="-Djbi.port=${{com.sun.jbi.management.JmxPort}}" />
            <arg value="-Djbi.deploymentId=${{com.sun.jbi.ui.devtool.jbi.guid.assembly-unit}}" />
            <arg value="undeploy"/>
        </exec>
        
        
        <exec dir="${{basedir}}" executable="${{com.sun.jbi.ui.devtool.appserver.instance.location}}/bin/asant.bat">
            <arg value="-f"/>
            <arg value="${{com.sun.jbi.ui.devtool.appserver.instance.location}}/bin/jbi_admin.xml"/>
            <arg value="-Djbi.host=${{com.sun.jbi.ui.devtool.appserver.instance.hostName}}" />
            <arg value="-Djbi.port=${{com.sun.jbi.management.JmxPort}}" />
            <arg value="-Djbi.deploymentJar=${{basedir}}/${{dist.jar}}" />
            <arg value="deploy"/>
        </exec>
    </target>
 
    <xsl:comment>
    =================
    DEBUGGING SECTION
    =================
    </xsl:comment>
    <target name="debug">
        <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
        <xsl:attribute name ="depends">init,compile</xsl:attribute>
        <xsl:attribute name="if">netbeans.home</xsl:attribute>
        <nbdeploy debugmode="true" clientUrlPart="${{client.urlPart}}"/>
        <nbjpdaconnect name="${{name}}" host="${{jpda.host}}" address="${{jpda.address}}" transport="${{jpda.transport}}">
            <classpath>
                <path path="${{debug.classpath}}"/>
            </classpath>
            <sourcepath>
                <path path="${{web.docbase.dir}}"/>
            </sourcepath>
            <xsl:if test="/p:project/p:configuration/ejb:data/ejb:explicit-platform">
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
                    <xsl:if test="/p:project/p:configuration/ejb:data/ejb:explicit-platform">
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
            <xsl:attribute name="unless">${no.dependencies}</xsl:attribute>
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
