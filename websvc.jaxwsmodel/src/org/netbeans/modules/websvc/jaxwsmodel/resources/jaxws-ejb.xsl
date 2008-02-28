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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
                xmlns:ejbjarproject2="http://www.netbeans.org/ns/j2ee-ejbjarproject/2"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:jaxws="http://www.netbeans.org/ns/jax-ws/1"> 
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:param name="jaxwsversion">jaxws21lib</xsl:param>
    <xsl:template match="/">
        
        <project>
            
            <xsl:comment>
                ===================
                JAX-WS WSGEN SECTION
                ===================
            </xsl:comment>
            
            <!-- WS from java - support for WSDL generation -->
            <xsl:if test="/jaxws:jax-ws/jaxws:services/jaxws:service">
                <xsl:if test="count(/jaxws:jax-ws/jaxws:services/jaxws:service[not(jaxws:wsdl-url)]) > 0">
                    <target name="wsgen-init" depends="init">
                        <mkdir dir="${{build.generated.dir}}/wsgen/service"/>
                        <mkdir dir="${{build.generated.dir}}/wsgen/service/resources/"/>
                        <mkdir dir="${{build.generated.dir}}/wsgen/binaries"/>
                        <taskdef name="wsgen" classname="com.sun.tools.ws.ant.WsGen">
                            <classpath path="${{j2ee.platform.wsgen.classpath}}"/>
                        </taskdef>
                    </target>
                    <target name="wsgen-compile">
                        <xsl:attribute name="depends">
                            <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service[not(jaxws:wsdl-url)]">
                                <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                                <xsl:variable name="wsname2">
                                    <xsl:value-of select="@name"/>
                                </xsl:variable>
                                <xsl:text>wsgen-</xsl:text><xsl:value-of select="@name"/>
                            </xsl:for-each>
                        </xsl:attribute>
                        <ejbjarproject2:javac srcdir="${{build.generated.dir}}/wsgen/service" classpath="${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}" destdir="${{classes.dir}}"/>
                    </target>
                </xsl:if>
            </xsl:if>
            <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service">
                <xsl:if test="not(jaxws:wsdl-url)">
                    <xsl:variable name="wsname" select="@name"/>
                    
                    <xsl:variable name="seiclass" select="jaxws:implementation-class"/>
                    <target name="wsgen-{$wsname}" depends="wsgen-init, -do-compile">                        
                        <xsl:choose>
                            <xsl:when test="$jaxwsversion = 'jaxws21lib'">
                                <wsgen
                                    xendorsed="true"
                                    fork="true"
                                    destdir="${{build.generated.dir}}/wsgen/binaries"
                                    sourcedestdir="${{build.generated.dir}}/wsgen/service"
                                    resourcedestdir="${{build.generated.dir}}/wsgen/service/resources/"
                                    keep="false"
                                    genwsdl="true"
                                    sei="{$seiclass}">
                                    <classpath path="${{java.home}}/../lib/tools.jar:${{classes.dir}}:${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}"/>
                                    <jvmarg value="-Djava.endorsed.dirs=${{jaxws.endorsed.dir}}"/>
                                </wsgen>
                            </xsl:when>
                            <xsl:otherwise>
                                <wsgen
                                    fork="true"
                                    destdir="${{build.generated.dir}}/wsgen/binaries"
                                    sourcedestdir="${{build.generated.dir}}/wsgen/service"
                                    resourcedestdir="${{build.generated.dir}}/wsgen/service/resources/"
                                    keep="false"
                                    genwsdl="true"
                                    sei="{$seiclass}">
                                    <classpath path="${{java.home}}/../lib/tools.jar:${{classes.dir}}:${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}"/>
                                    <jvmarg value="-Djava.endorsed.dirs=${{jaxws.endorsed.dir}}"/>
                                </wsgen>                            
                            </xsl:otherwise>
                        </xsl:choose>
                    </target>
                </xsl:if>
            </xsl:for-each>
            <!-- END WS from Java -->            
            
            <xsl:comment>
                ===================
                JAX-WS WSIMPORT SECTION
                ===================
            </xsl:comment>
            
            <!-- wsimport task initialization -->
            <xsl:if test="/*/*/*/jaxws:wsdl-url">
                <target name="wsimport-init" depends="init">
                    <xsl:if test="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                        <mkdir dir="${{build.generated.dir}}/wsimport/client"/>                        
                    </xsl:if>
                    <xsl:if test="/jaxws:jax-ws/jaxws:services/jaxws:service/jaxws:wsdl-url">
                        <mkdir dir="${{build.generated.dir}}/wsimport/service"/>
                    </xsl:if>
                    <mkdir dir="${{build.generated.dir}}/wsimport/binaries"/>
                    <mkdir dir="${{classes.dir}}"/>
                    <taskdef name="wsimport" classname="com.sun.tools.ws.ant.WsImport">
                        <classpath path="${{j2ee.platform.wsimport.classpath}}"/>
                    </taskdef>
                </target>
            </xsl:if>
            <xsl:for-each select="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                <xsl:variable name="wsname" select="@name"/>
                <xsl:variable name="package_name" select="jaxws:package-name"/>
                <xsl:variable name="wsdl_url" select="jaxws:local-wsdl-file"/>
                <xsl:variable name="wsdl_url_actual" select="jaxws:wsdl-url"/>
                <xsl:variable name="package_path" select = "translate($package_name,'.','/')"/>
                <xsl:variable name="catalog" select = "jaxws:catalog-file"/>
                <target name="wsimport-client-check-{$wsname}" depends="wsimport-init">
                    <condition property="wsimport-client-{$wsname}.notRequired">
                        <available file="${{build.generated.dir}}/wsimport/client/{$package_path}" type="dir"/>
                    </condition>
                </target>
                <target name="wsimport-client-{$wsname}" depends="wsimport-init,wsimport-client-check-{$wsname}" unless="wsimport-client-{$wsname}.notRequired">
                    <xsl:if test="jaxws:package-name/@forceReplace">
                        <xsl:choose>
                            <xsl:when test="$jaxwsversion = 'jaxws21lib'">
                                <wsimport
                                    xendorsed="true"
                                    sourcedestdir="${{build.generated.dir}}/wsimport/client"
                                    extension="true"
                                    package="{$package_name}"
                                    destdir="${{build.generated.dir}}/wsimport/binaries"
                                    wsdl="${{basedir}}/${{meta.inf}}/xml-resources/web-service-references/{$wsname}/wsdl/{$wsdl_url}"
                                    wsdlLocation="{$wsdl_url_actual}"
                                    catalog="{$catalog}">
                                    <xsl:if test="jaxws:wsimport-options">
                                        <xsl:for-each select="jaxws:wsimport-options/jaxws:wsimport-option">
                                            <xsl:variable name="wsoptionname" select="jaxws:wsimport-option-name"/>
                                            <xsl:variable name="wsoptionvalue" select="jaxws:wsimport-option-value"/>
                                            <xsl:variable name="wsoption">
                                                <xsl:text><xsl:value-of select="$wsoptionname"/></xsl:text>
                                            </xsl:variable>
                                            <xsl:attribute name="{$wsoption}"><xsl:value-of select="$wsoptionvalue"/></xsl:attribute>
                                        </xsl:for-each>
                                    </xsl:if>
                                    <xsl:if test="jaxws:binding">
                                        <binding dir="${{meta.inf}}/xml-resources/web-service-references/{$wsname}/bindings">
                                            <xsl:attribute name="includes">
                                                <xsl:for-each select="jaxws:binding">
                                                    <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                                                    <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                                </xsl:for-each>
                                            </xsl:attribute>
                                        </binding>
                                    </xsl:if>
                                </wsimport>
                            </xsl:when>
                            <xsl:otherwise>
                                <wsimport
                                    sourcedestdir="${{build.generated.dir}}/wsimport/client"
                                    extension="true"
                                    package="{$package_name}"
                                    destdir="${{build.generated.dir}}/wsimport/binaries"
                                    wsdl="${{basedir}}/${{meta.inf}}/xml-resources/web-service-references/{$wsname}/wsdl/{$wsdl_url}"
                                    wsdlLocation="{$wsdl_url_actual}"
                                    catalog="{$catalog}">
                                    <xsl:if test="jaxws:wsimport-options">
                                        <xsl:for-each select="jaxws:wsimport-options/jaxws:wsimport-option">
                                            <xsl:variable name="wsoptionname" select="jaxws:wsimport-option-name"/>
                                            <xsl:variable name="wsoptionvalue" select="jaxws:wsimport-option-value"/>
                                            <xsl:variable name="wsoption">
                                                <xsl:text><xsl:value-of select="$wsoptionname"/></xsl:text>
                                            </xsl:variable>
                                            <xsl:attribute name="{$wsoption}"><xsl:value-of select="$wsoptionvalue"/></xsl:attribute>
                                        </xsl:for-each>
                                    </xsl:if>
                                    <xsl:if test="jaxws:binding">
                                        <binding dir="${{meta.inf}}/xml-resources/web-service-references/{$wsname}/bindings">
                                            <xsl:attribute name="includes">
                                                <xsl:for-each select="jaxws:binding">
                                                    <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                                                    <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                                </xsl:for-each>
                                            </xsl:attribute>
                                        </binding>
                                    </xsl:if>
                                </wsimport>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                    <xsl:if test="not(jaxws:package-name/@forceReplace)">
                        <xsl:choose>
                            <xsl:when test="$jaxwsversion = 'jaxws21lib'">
                                <wsimport
                                    xendorsed="true"
                                    sourcedestdir="${{build.generated.dir}}/wsimport/client"
                                    extension="true"
                                    destdir="${{build.generated.dir}}/wsimport/binaries"
                                    wsdl="${{basedir}}/${{meta.inf}}/xml-resources/web-service-references/{$wsname}/wsdl/{$wsdl_url}"
                                    wsdlLocation="{$wsdl_url_actual}"
                                    catalog="{$catalog}">
                                    <xsl:if test="jaxws:wsimport-options">
                                        <xsl:for-each select="jaxws:wsimport-options/jaxws:wsimport-option">
                                            <xsl:variable name="wsoptionname" select="jaxws:wsimport-option-name"/>
                                            <xsl:variable name="wsoptionvalue" select="jaxws:wsimport-option-value"/>
                                            <xsl:variable name="wsoption">
                                                <xsl:text><xsl:value-of select="$wsoptionname"/></xsl:text>
                                            </xsl:variable>
                                            <xsl:attribute name="{$wsoption}"><xsl:value-of select="$wsoptionvalue"/></xsl:attribute>
                                        </xsl:for-each>
                                    </xsl:if>
                                    <xsl:if test="jaxws:binding">
                                        <binding dir="${{meta.inf}}/xml-resources/web-service-references/{$wsname}/bindings">
                                            <xsl:attribute name="includes">
                                                <xsl:for-each select="jaxws:binding">
                                                    <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                                                    <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                                </xsl:for-each>
                                            </xsl:attribute>
                                        </binding>
                                    </xsl:if>
                                </wsimport>
                            </xsl:when>
                            <xsl:otherwise>
                                <wsimport
                                    sourcedestdir="${{build.generated.dir}}/wsimport/client"
                                    extension="true"
                                    destdir="${{build.generated.dir}}/wsimport/binaries"
                                    wsdl="${{basedir}}/${{meta.inf}}/xml-resources/web-service-references/{$wsname}/wsdl/{$wsdl_url}"
                                    wsdlLocation="{$wsdl_url_actual}"
                                    catalog="{$catalog}">
                                    <xsl:if test="jaxws:wsimport-options">
                                        <xsl:for-each select="jaxws:wsimport-options/jaxws:wsimport-option">
                                            <xsl:variable name="wsoptionname" select="jaxws:wsimport-option-name"/>
                                            <xsl:variable name="wsoptionvalue" select="jaxws:wsimport-option-value"/>
                                            <xsl:variable name="wsoption">
                                                <xsl:text><xsl:value-of select="$wsoptionname"/></xsl:text>
                                            </xsl:variable>
                                            <xsl:attribute name="{$wsoption}"><xsl:value-of select="$wsoptionvalue"/></xsl:attribute>
                                        </xsl:for-each>
                                    </xsl:if>
                                    <xsl:if test="jaxws:binding">
                                        <binding dir="${{meta.inf}}/xml-resources/web-service-references/{$wsname}/bindings">
                                            <xsl:attribute name="includes">
                                                <xsl:for-each select="jaxws:binding">
                                                    <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                                                    <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                                </xsl:for-each>
                                            </xsl:attribute>
                                        </binding>
                                    </xsl:if>
                                </wsimport>
                            </xsl:otherwise> 
                        </xsl:choose>
                    </xsl:if>
                    <copy todir="${{classes.dir}}">
                        <fileset dir="${{build.generated.dir}}/wsimport/binaries" includes="**/*.xml"/>
                    </copy>
                </target>
                <target name="wsimport-client-clean-{$wsname}" depends="-init-project">
                    <delete dir="${{build.generated.dir}}/wsimport/client/{$package_path}"/>
                </target>
            </xsl:for-each>
            
            <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service">
                <xsl:if test="jaxws:wsdl-url">
                    <xsl:variable name="wsname" select="@name"/>
                    <xsl:variable name="package_name" select="jaxws:package-name"/>
                    <xsl:variable name="wsdl_url" select="jaxws:local-wsdl-file"/>
                    <xsl:variable name="package_path" select = "translate($package_name,'.','/')"/>
                    <xsl:variable name="catalog" select = "jaxws:catalog-file"/>
                    <target name="wsimport-service-check-{$wsname}" depends="wsimport-init">
                        <condition property="wsimport-service-{$wsname}.notRequired">
                            <available file="${{build.generated.dir}}/wsimport/service/{$package_path}" type="dir"/>
                        </condition>
                    </target>
                    <target name="wsimport-service-{$wsname}" depends="wsimport-init,wsimport-service-check-{$wsname}" unless="wsimport-service-{$wsname}.notRequired">
                        <xsl:if test="jaxws:package-name/@forceReplace">
                            <xsl:choose>
                                <xsl:when test="$jaxwsversion = 'jaxws21lib'">  
                                    <wsimport
                                        xendorsed="true"
                                        sourcedestdir="${{build.generated.dir}}/wsimport/service"
                                        extension="true"
                                        verbose="true"
                                        package="{$package_name}"
                                        destdir="${{build.generated.dir}}/wsimport/binaries"
                                        wsdl="${{basedir}}/${{meta.inf}}/xml-resources/web-services/{$wsname}/wsdl/{$wsdl_url}"
                                        catalog="{$catalog}">
                                        <xsl:if test="jaxws:wsimport-options">
                                            <xsl:for-each select="jaxws:wsimport-options/jaxws:wsimport-option">
                                                <xsl:variable name="wsoptionname" select="jaxws:wsimport-option-name"/>
                                                <xsl:variable name="wsoptionvalue" select="jaxws:wsimport-option-value"/>
                                                <xsl:variable name="wsoption">
                                                    <xsl:text><xsl:value-of select="$wsoptionname"/></xsl:text>
                                                </xsl:variable>
                                                <xsl:attribute name="{$wsoption}"><xsl:value-of select="$wsoptionvalue"/></xsl:attribute>
                                            </xsl:for-each>
                                        </xsl:if>
                                        <xsl:if test="jaxws:binding">
                                            <binding dir="${{meta.inf}}/xml-resources/web-services/{$wsname}/bindings">
                                                <xsl:attribute name="includes">
                                                    <xsl:for-each select="jaxws:binding">
                                                        <xsl:if test="position()!=1"><xsl:text>;</xsl:text></xsl:if>
                                                        <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                                    </xsl:for-each>
                                                </xsl:attribute>
                                            </binding>
                                        </xsl:if>
                                    </wsimport>
                                </xsl:when>
                                <xsl:otherwise>
                                    <wsimport
                                        sourcedestdir="${{build.generated.dir}}/wsimport/service"
                                        extension="true"
                                        verbose="true"
                                        package="{$package_name}"
                                        destdir="${{build.generated.dir}}/wsimport/binaries"
                                        wsdl="${{basedir}}/${{meta.inf}}/xml-resources/web-services/{$wsname}/wsdl/{$wsdl_url}"
                                        catalog="{$catalog}">
                                        <xsl:if test="jaxws:binding">
                                            <binding dir="${{meta.inf}}/xml-resources/web-services/{$wsname}/bindings">
                                                <xsl:attribute name="includes">
                                                    <xsl:for-each select="jaxws:binding">
                                                        <xsl:if test="position()!=1"><xsl:text>;</xsl:text></xsl:if>
                                                        <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                                    </xsl:for-each>
                                                </xsl:attribute>
                                            </binding>
                                        </xsl:if>
                                    </wsimport>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                        <xsl:if test="not(jaxws:package-name/@forceReplace)">
                            <xsl:choose>
                                <xsl:when test="$jaxwsversion = 'jaxws21lib'"> 
                                    <wsimport
                                        xendorsed="true"
                                        sourcedestdir="${{build.generated.dir}}/wsimport/service"
                                        extension="true"
                                        verbose="true"
                                        destdir="${{build.generated.dir}}/wsimport/binaries"
                                        wsdl="${{basedir}}/${{meta.inf}}/xml-resources/web-services/{$wsname}/wsdl/{$wsdl_url}"
                                        catalog="{$catalog}">
                                        <xsl:if test="jaxws:wsimport-options">
                                            <xsl:for-each select="jaxws:wsimport-options/jaxws:wsimport-option">
                                                <xsl:variable name="wsoptionname" select="jaxws:wsimport-option-name"/>
                                                <xsl:variable name="wsoptionvalue" select="jaxws:wsimport-option-value"/>
                                                <xsl:variable name="wsoption">
                                                    <xsl:text><xsl:value-of select="$wsoptionname"/></xsl:text>
                                                </xsl:variable>
                                                <xsl:attribute name="{$wsoption}"><xsl:value-of select="$wsoptionvalue"/></xsl:attribute>
                                            </xsl:for-each>
                                        </xsl:if>
                                        <xsl:if test="jaxws:binding">
                                            <binding dir="${{meta.inf}}/xml-resources/web-services/{$wsname}/bindings">
                                                <xsl:attribute name="includes">
                                                    <xsl:for-each select="jaxws:binding">
                                                        <xsl:if test="position()!=1"><xsl:text>;</xsl:text></xsl:if>
                                                        <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                                    </xsl:for-each>
                                                </xsl:attribute>
                                            </binding>
                                        </xsl:if>
                                    </wsimport>  
                                </xsl:when>
                                <xsl:otherwise>
                                    <wsimport
                                        sourcedestdir="${{build.generated.dir}}/wsimport/service"
                                        extension="true"
                                        verbose="true"
                                        destdir="${{build.generated.dir}}/wsimport/binaries"
                                        wsdl="${{basedir}}/${{meta.inf}}/xml-resources/web-services/{$wsname}/wsdl/{$wsdl_url}"
                                        catalog="{$catalog}">
                                        <xsl:if test="jaxws:binding">
                                            <binding dir="${{meta.inf}}/xml-resources/web-services/{$wsname}/bindings">
                                                <xsl:attribute name="includes">
                                                    <xsl:for-each select="jaxws:binding">
                                                        <xsl:if test="position()!=1"><xsl:text>;</xsl:text></xsl:if>
                                                        <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                                    </xsl:for-each>
                                                </xsl:attribute>
                                            </binding>
                                        </xsl:if>
                                    </wsimport>
                                </xsl:otherwise>
                            </xsl:choose>                          
                        </xsl:if>
                        <copy todir="${{basedir}}/${{meta.inf}}/wsdl/{$wsname}">
                            <fileset dir="${{basedir}}/${{meta.inf}}/xml-resources/web-services/{$wsname}/wsdl/" />
                        </copy> 
                    </target>
                    <target name="wsimport-service-clean-{$wsname}" depends="-init-project">
                        <delete dir="${{build.generated.dir}}/wsimport/service/{$package_path}"/>
                    </target>
                </xsl:if>
            </xsl:for-each>
            
            <!-- wsimport-client-generate and wsimport-client-compile targets -->
            <xsl:if test="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                <target name="wsimport-client-generate">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:text>wsimport-client-</xsl:text><xsl:value-of select="@name"/>
                        </xsl:for-each>
                    </xsl:attribute>
                </target>
                <target name="wsimport-client-compile" depends="wsimport-client-generate">
                    <ejbjarproject2:javac srcdir="${{build.generated.dir}}/wsimport/client" classpath="${{j2ee.platform.wsimport.classpath}}:${{javac.classpath}}" destdir="${{classes.dir}}"/>
                </target>
            </xsl:if>
            
            <xsl:if test="/jaxws:jax-ws/jaxws:services/jaxws:service/jaxws:wsdl-url">
                <target name="wsimport-service-generate">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:if test="jaxws:wsdl-url">
                                <xsl:text>wsimport-service-</xsl:text><xsl:value-of select="@name"/>
                            </xsl:if>
                            <xsl:if test="not(jaxws:wsdl-url)">
                                <xsl:text>wsimport-init</xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:attribute>
                </target>
                <target name="wsimport-service-compile" depends="wsimport-service-generate">
                    <ejbjarproject2:javac srcdir="${{build.generated.dir}}/wsimport/service" classpath="${{j2ee.platform.wsimport.classpath}}:${{javac.classpath}}" destdir="${{classes.dir}}"/>
                </target>
            </xsl:if>
            
        </project>
        
    </xsl:template>
    
</xsl:stylesheet>
