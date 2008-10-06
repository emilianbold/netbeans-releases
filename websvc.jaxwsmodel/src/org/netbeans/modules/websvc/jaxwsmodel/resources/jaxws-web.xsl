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
                xmlns:webproject2="http://www.netbeans.org/ns/web-project/2"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:jaxws="http://www.netbeans.org/ns/jax-ws/1">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:param name="jaxwsversion">jaxws21lib</xsl:param>
    <xsl:template match="/">

        <project>

            <xsl:comment>
                ===================
                JAX-WS WSIMPORT SECTION
                ===================
            </xsl:comment>

            <!-- START: Invoke wsgen if web service is not JSR 109 and not from wsdl-->
            <xsl:if test="/jaxws:jax-ws/jaxws:services/jaxws:service">
                <xsl:if test="count(/jaxws:jax-ws/jaxws:services/jaxws:service[not(jaxws:wsdl-url)]) > 0">
                    <target name="wsgen-init" depends="init, -do-compile">
                        <mkdir dir="${{build.generated.dir}}/wsgen/service"/>
                        <mkdir dir="${{build.generated.dir}}/wsgen/service/resources/"/>
                        <mkdir dir="${{build.generated.dir}}/wsgen/binaries"/>
                        <mkdir dir="${{build.classes.dir}}"/>
                        <taskdef name="wsgen" classname="com.sun.tools.ws.ant.WsGen">
                            <classpath path="${{java.home}}/../lib/tools.jar:${{build.classes.dir}}:${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}"/>
                        </taskdef>
                    </target>
                </xsl:if>
                <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service">
                    <xsl:if test="not(jaxws:wsdl-url)">
                        <xsl:variable name="wsname" select="@name"/>
                        <xsl:variable name="seiclass" select="jaxws:implementation-class"/>
                        <target name="wsgen-{$wsname}" depends="wsgen-init">
                            <xsl:choose>
                                <xsl:when test="$jaxwsversion='jaxws21lib'">
                                    <wsgen
                                        sourcedestdir="${{build.generated.dir}}/wsgen/service"
                                        resourcedestdir="${{build.generated.dir}}/wsgen/service/resources/"
                                        destdir="${{build.generated.dir}}/wsgen/binaries"
                                        xendorsed = "true"
                                        keep="true"
                                        genwsdl="true"
                                        sei="{$seiclass}">
                                        <classpath path="${{java.home}}/../lib/tools.jar:${{build.classes.dir}}:${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}"/>
                                    </wsgen>
                                </xsl:when>
                                <xsl:otherwise>
                                    <wsgen
                                        sourcedestdir="${{build.generated.dir}}/wsgen/service"
                                        resourcedestdir="${{build.generated.dir}}/wsgen/service/resources/"
                                        destdir="${{build.generated.dir}}/wsgen/binaries"
                                        keep="true"
                                        genwsdl="true"
                                        sei="{$seiclass}">
                                        <classpath path="${{java.home}}/../lib/tools.jar:${{build.classes.dir}}:${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}"/>
                                    </wsgen>
                                </xsl:otherwise>
                            </xsl:choose>
                        </target>
                    </xsl:if>
                </xsl:for-each>
                <xsl:if test="count(/jaxws:jax-ws/jaxws:services/jaxws:service[not(jaxws:wsdl-url)]) > 0">
                    <target name="wsgen-service-compile">
                        <xsl:attribute name="depends">
                            <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service[not(jaxws:wsdl-url)]">
                                <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                                <xsl:text>wsgen-</xsl:text><xsl:value-of select="@name"/>
                            </xsl:for-each>
                        </xsl:attribute>
                        <webproject2:javac srcdir="${{build.generated.dir}}/wsgen/service" classpath="${{j2ee.platform.wsimport.classpath}}:${{javac.classpath}}" destdir="${{build.classes.dir}}"/>
                    </target>
                </xsl:if>
            </xsl:if>
            <!-- END: Invoke wsgen if web service is not JSR 109 -->


            <!-- wsimport task initialization -->
            <xsl:if test="/*/*/*/jaxws:wsdl-url">
                <xsl:variable name="isJSR109">
                    <xsl:value-of select="/jaxws:jax-ws/jaxws:jsr109"/>
                </xsl:variable>
                <target name="wsimport-init" depends="init">
                    <xsl:if test="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                        <mkdir dir="${{build.generated.dir}}/wsimport/client"/>
                    </xsl:if>
                    <xsl:if test="/jaxws:jax-ws/jaxws:services/jaxws:service/jaxws:wsdl-url">
                        <mkdir dir="${{build.generated.dir}}/wsimport/service"/>
                    </xsl:if>
                    <mkdir dir="${{build.generated.dir}}/wsimport/binaries"/>
                    <taskdef name="wsimport" classname="com.sun.tools.ws.ant.WsImport">
                        <classpath path="${{java.home}}/../lib/tools.jar:${{j2ee.platform.wsimport.classpath}}:${{javac.classpath}}"/>
                    </taskdef>
                    <condition property="conf-dir" value="${{conf.dir}}/" else="">
                        <isset property="conf.dir"/>
                    </condition>
                </target>
            </xsl:if>
            <!-- END: wsimport task initialization -->

            <!-- wsimport target for client -->
            <xsl:for-each select="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                <xsl:variable name="wsname" select="@name"/>
                <xsl:variable name="package_name" select="jaxws:package-name"/>
                <xsl:variable name="wsdl_url" select="jaxws:local-wsdl-file"/>
                <xsl:variable name="package_path" select = "translate($package_name,'.','/')"/>
                <xsl:variable name="catalog" select = "jaxws:catalog-file"/>
                <target name="wsimport-client-check-{$wsname}" depends="wsimport-init">
                    <condition property="wsimport-client-{$wsname}.notRequired">
                        <xsl:choose>
                            <xsl:when test="jaxws:package-name">
                                <available file="${{build.generated.dir}}/wsimport/client/{$package_path}/{$wsname}.java"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <available file="${{build.generated.dir}}/wsimport/client/dummy" type="dir"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </condition>
                </target>
                <target name="wsimport-client-{$wsname}" depends="wsimport-init,wsimport-client-check-{$wsname}" unless="wsimport-client-{$wsname}.notRequired">
                    <xsl:variable name="forceReplace_var" select="jaxws:package-name/@forceReplace"/>
                    <xsl:variable name="isService_var" select="false()"/>
                    <xsl:call-template name="invokeWsimport">
                        <xsl:with-param name="isService" select="$isService_var"/>
                        <xsl:with-param name="forceReplace" select="$forceReplace_var"/>
                        <xsl:with-param name="packageName" select="$package_name"/>
                        <xsl:with-param name="wsName" select="$wsname" />
                        <xsl:with-param name="wsdlUrl" select="$wsdl_url"/>
                        <xsl:with-param name="Catalog" select="$catalog"/>
                        <xsl:with-param name="wsimportoptions" select="jaxws:wsimport-options"/>
                    </xsl:call-template>
                </target>
                <target name="wsimport-client-clean-{$wsname}" depends="-init-project">
                    <delete dir="${{build.generated.dir}}/wsimport/client/{$package_path}"/>
                </target>
            </xsl:for-each>
            <!-- END: wsimport target for client -->

            <!-- wsimport target for service -->
            <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service">
                <xsl:if test="jaxws:wsdl-url">
                    <xsl:variable name="wsname" select="@name"/>
                    <xsl:variable name="package_name" select="jaxws:package-name"/>
                    <xsl:variable name="wsdl_url" select="jaxws:local-wsdl-file"/>
                    <xsl:variable name="package_path" select = "translate($package_name,'.','/')"/>
                    <xsl:variable name="service_name" select = "jaxws:service-name"/>
                    <xsl:variable name="catalog" select = "jaxws:catalog-file"/>
                    <target name="wsimport-service-check-{$wsname}" depends="wsimport-init">
                        <condition property="wsimport-service-{$wsname}.notRequired">
                            <available file="${{build.generated.dir}}/wsimport/service/{$package_path}/{$service_name}.java"/>
                        </condition>
                    </target>
                    <target name="wsimport-service-{$wsname}" depends="wsimport-init,wsimport-service-check-{$wsname}" unless="wsimport-service-{$wsname}.notRequired">
                        <xsl:variable name="forceReplace_var" select="jaxws:package-name/@forceReplace" />
                        <xsl:variable name="isService_var" select="true()"/>
                        <xsl:call-template name="invokeWsimport">
                            <xsl:with-param name="isService" select="$isService_var"/>
                            <xsl:with-param name="forceReplace" select="$forceReplace_var"/>
                            <xsl:with-param name="packageName" select="$package_name"/>
                            <xsl:with-param name="wsName" select="$wsname" />
                            <xsl:with-param name="wsdlUrl" select="$wsdl_url"/>
                            <xsl:with-param name="Catalog" select="$catalog"/>
                            <xsl:with-param name="wsimportoptions" select="jaxws:wsimport-options"/>
                        </xsl:call-template>
                        <copy todir="${{build.web.dir}}/WEB-INF/wsdl/{$wsname}">
                            <fileset dir="${{basedir}}/${{conf-dir}}xml-resources/web-services/{$wsname}/wsdl/" />
                        </copy>
                    </target>
                    <target name="wsimport-service-clean-{$wsname}" depends="-init-project">
                        <delete dir="${{build.generated.dir}}/wsimport/service/{$package_path}"/>
                    </target>
                </xsl:if>
            </xsl:for-each>
            <!-- wsimport target for service -->

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
                <target name="wsimport-client-compile" depends="-pre-pre-compile">
                    <webproject2:javac srcdir="${{build.generated.dir}}/wsimport/client" classpath="${{j2ee.platform.wsimport.classpath}}:${{javac.classpath}}" destdir="${{build.classes.dir}}"/>
                    <copy todir="${{build.classes.dir}}">
                        <fileset dir="${{build.generated.dir}}/wsimport/binaries" includes="**/*.xml"/>
                    </copy>
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
                <target name="wsimport-service-compile" depends="-pre-pre-compile">
                    <webproject2:javac srcdir="${{build.generated.dir}}/wsimport/service" classpath="${{j2ee.platform.wsimport.classpath}}:${{javac.classpath}}" destdir="${{build.classes.dir}}"/>
                </target>
            </xsl:if>
            <!-- END: wsimport-client-generate and wsimport-client-compile targets -->
        </project>
    </xsl:template>

    <!-- invokeWsimport template -->
    <xsl:template name="invokeWsimport">
        <xsl:param name="forceReplace"/>
        <xsl:param name="packageName"/>
        <xsl:param name="isService" />
        <xsl:param name="wsName" />
        <xsl:param name="wsdlUrl"/>
        <xsl:param name="Catalog"/>
        <xsl:param name="wsimportoptions"/>
        <wsimport>
            <xsl:if test="$forceReplace">
                <xsl:attribute name="package"><xsl:value-of select="$packageName"/></xsl:attribute>
            </xsl:if>
            <xsl:variable name="wsType">
                <xsl:choose>
                    <xsl:when test="$isService">
                        <xsl:text>service</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>client</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:attribute name="sourcedestdir">${build.generated.dir}/wsimport/<xsl:value-of select="$wsType"/></xsl:attribute>
            <xsl:attribute name="destdir">${build.generated.dir}/wsimport/binaries</xsl:attribute>
            <xsl:variable name="wsDir">
                <xsl:choose>
                    <xsl:when test="$isService">
                        <xsl:text>web-services</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>web-service-references</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:attribute name="wsdl">${basedir}/${conf-dir}xml-resources/<xsl:value-of select="$wsDir"/>/<xsl:value-of select="$wsName"/>/wsdl/<xsl:value-of select="$wsdlUrl"/></xsl:attribute>
            <xsl:attribute name="catalog"><xsl:value-of select="$Catalog" /></xsl:attribute>
            <xsl:if test="$wsimportoptions">
                <xsl:for-each select="$wsimportoptions/jaxws:wsimport-option">
                    <xsl:variable name="wsoptionname" select="jaxws:wsimport-option-name"/>
                    <xsl:variable name="wsoptionvalue" select="jaxws:wsimport-option-value"/>
                    <xsl:choose>
                        <xsl:when test="jaxws:jaxboption">
                            <xjcarg>
                                <xsl:variable name="wsoption">
                                    <xsl:text><xsl:value-of select="$wsoptionname"/></xsl:text>
                                </xsl:variable>
                                <xsl:attribute name="{$wsoption}">
                                    <xsl:value-of select="$wsoptionvalue"/>
                                </xsl:attribute>
                            </xjcarg>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="wsoption">
                                <xsl:text><xsl:value-of select="$wsoptionname"/></xsl:text>
                            </xsl:variable>
                            <xsl:attribute name="{$wsoption}">
                                <xsl:value-of select="$wsoptionvalue"/>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:if>
            <xsl:if test="jaxws:binding">
                <binding>
                    <xsl:attribute name="dir">${conf-dir}xml-resources/<xsl:value-of select="$wsDir"/>/<xsl:value-of select="$wsName"/>/bindings</xsl:attribute>
                    <xsl:attribute name="includes">
                        <xsl:for-each select="jaxws:binding">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                        </xsl:for-each>
                    </xsl:attribute>
                </binding>
            </xsl:if>
        </wsimport>
    </xsl:template>
    <!-- END: invokeWsimport template -->

</xsl:stylesheet>
