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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.
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
                        <mkdir dir="${{build.generated.dir}}/wsgen/binaries"/>
                        <mkdir dir="${{build.classes.dir.real}}"/>
                        <taskdef name="wsgen" classname="com.sun.tools.ws.ant.WsGen">
                            <classpath path="${{java.home}}/../lib/tools.jar:${{build.classes.dir.real}}:${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}"/>
                        </taskdef>
                    </target>
                </xsl:if>
                <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service">
                    <xsl:if test="not(jaxws:wsdl-url)">
                        <xsl:variable name="wsname" select="@name"/>
                        <xsl:variable name="seiclass" select="jaxws:implementation-class"/>                      
                        <target name="wsgen-{$wsname}" depends="wsgen-init">
                            <wsgen
                                fork="true"
                                xendorsed="true"
                                sourcedestdir="${{build.generated.dir}}/wsgen/service"
                                resourcedestdir="${{build.generated.dir}}/wsgen/service"
                                destdir="${{build.generated.dir}}/wsgen/binaries"
                                keep="true"
                                genwsdl="true"
                                sei="{$seiclass}"
                            >   
                                <classpath path="${{java.home}}/../lib/tools.jar:${{build.classes.dir.real}}:${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}"/>
                                <jvmarg value="-Djava.endorsed.dirs=${{jaxws.endorsed.dir}}"/>
                            </wsgen>
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
                        <webproject2:javac srcdir="${{build.generated.dir}}/wsgen/service" classpath="${{j2ee.platform.wsimport.classpath}}:${{javac.classpath}}" destdir="${{build.classes.dir.real}}" javac.compilerargs.jaxws="-Djava.endorsed.dirs='${{jaxws.endorsed.dir}}'"/>
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
                <xsl:variable name="wsdl_url_actual" select="jaxws:wsdl-url"/>
                <xsl:variable name="package_path" select = "translate($package_name,'.','/')"/>
                <xsl:variable name="catalog" select = "jaxws:catalog-file"/>
                <xsl:variable name="isJSR109">
                    <xsl:value-of select="/jaxws:jax-ws/jaxws:jsr109"/>
                </xsl:variable>
                <target name="wsimport-client-check-{$wsname}" depends="wsimport-init">
                    <condition property="wsimport-client-{$wsname}.notRequired">
                        <available file="${{build.generated.dir}}/wsimport/client/{$package_path}" type="dir"/>
                    </condition>
                </target>
                <target name="wsimport-client-{$wsname}" depends="wsimport-init,wsimport-client-check-{$wsname}" unless="wsimport-client-{$wsname}.notRequired">
                    <xsl:variable name="jaxws21_var" select="$jaxwsversion = 'jaxws21lib'"/>
                    <xsl:variable name="jsr109_var">
                        <xsl:choose>
                            <xsl:when test="$isJSR109 = 'false'">
                                <xsl:value-of select="false()" />    
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="true()" />  
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="forceReplace_var" select="jaxws:package-name/@forceReplace"/>
                    <xsl:variable name="isService_var" select="false()"/>         
                    <xsl:call-template name="invokeWsimport">
                        <xsl:with-param name="isService" select="$isService_var"/>          
                        <xsl:with-param name="forceReplace" select="$forceReplace_var"/>
                        <xsl:with-param name="isJSR109" select="$jsr109_var"/> 
                        <xsl:with-param name="isJaxws21" select="$jaxws21_var"/>
                        <xsl:with-param name="packageName" select="$package_name"/>                         
                        <xsl:with-param name="wsName" select="$wsname" />
                        <xsl:with-param name="wsdlUrl" select="$wsdl_url"/>
                        <xsl:with-param name="wsdlUrlActual" select="$wsdl_url_actual"/>
                        <xsl:with-param name="Catalog" select="$catalog"/>  
                    </xsl:call-template>                       
                    <copy todir="${{build.classes.dir.real}}">
                        <fileset dir="${{build.generated.dir}}/wsimport/binaries" includes="**/*.xml"/>
                    </copy>
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
                    <xsl:variable name="catalog" select = "jaxws:catalog-file"/>
                    <xsl:variable name="isJSR109">
                        <xsl:value-of select="/jaxws:jax-ws/jaxws:jsr109"/>
                    </xsl:variable>
                    <target name="wsimport-service-check-{$wsname}" depends="wsimport-init">
                        <condition property="wsimport-service-{$wsname}.notRequired">
                            <available file="${{build.generated.dir}}/wsimport/service/{$package_path}" type="dir"/>
                        </condition>
                    </target>
                    <target name="wsimport-service-{$wsname}" depends="wsimport-init,wsimport-service-check-{$wsname}" unless="wsimport-service-{$wsname}.notRequired">
                        <xsl:variable name="jaxws21_var" select="$jaxwsversion = 'jaxws21lib'"/>
                        <xsl:variable name="jsr109_var">
                            <xsl:choose>
                                <xsl:when test="$isJSR109 = 'false'">
                                    <xsl:value-of select="false()" />              
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="true()" />  
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="forceReplace_var" select="jaxws:package-name/@forceReplace" />
                        <xsl:variable name="isService_var" select="true()"/>
                        <xsl:call-template name="invokeWsimport">
                            <xsl:with-param name="isService" select="$isService_var"/>          
                            <xsl:with-param name="forceReplace" select="$forceReplace_var"/>
                            <xsl:with-param name="isJSR109" select="$jsr109_var"/> 
                            <xsl:with-param name="isJaxws21" select="$jaxws21_var"/>
                            <xsl:with-param name="packageName" select="$package_name"/>                         
                            <xsl:with-param name="wsName" select="$wsname" />
                            <xsl:with-param name="wsdlUrl" select="$wsdl_url"/>
                            <xsl:with-param name="Catalog" select="$catalog"/>  
                        </xsl:call-template>
                        <copy todir="${{build.web.dir.real}}/WEB-INF/wsdl/{$wsname}">
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
                    <webproject2:javac srcdir="${{build.generated.dir}}/wsimport/client" classpath="${{j2ee.platform.wsimport.classpath}}:${{javac.classpath}}" destdir="${{build.classes.dir.real}}" javac.compilerargs.jaxws="-Djava.endorsed.dirs='${{jaxws.endorsed.dir}}'"/>
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
                    <webproject2:javac srcdir="${{build.generated.dir}}/wsimport/service" classpath="${{j2ee.platform.wsimport.classpath}}:${{javac.classpath}}" destdir="${{build.classes.dir.real}}" javac.compilerargs.jaxws="-Djava.endorsed.dirs='${{jaxws.endorsed.dir}}'"/>
                </target>
            </xsl:if>
            <!-- END: wsimport-client-generate and wsimport-client-compile targets -->
        </project>        
    </xsl:template>
    
    <!-- invokeWsimport template -->
    <xsl:template name="invokeWsimport">
        <xsl:param name="isJaxws21" />
        <xsl:param name="forceReplace"/>
        <xsl:param name="packageName"/>
        <xsl:param name="isJSR109"/>
        <xsl:param name="isService" />
        <xsl:param name="wsName" />
        <xsl:param name="wsdlUrl"/>
        <xsl:param name="wsdlUrlActual"/>
        <xsl:param name="Catalog"/>
        <wsimport>
            <xsl:if test="$isJaxws21 or $isJSR109 = 'false'">
                <xsl:attribute name="xendorsed">true</xsl:attribute>  
            </xsl:if>
            <xsl:if test="$isJSR109 = 'false'">
                <xsl:attribute name="fork">true</xsl:attribute>  
            </xsl:if>
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
            <xsl:attribute name="verbose">true</xsl:attribute> 
            <xsl:attribute name="sourcedestdir">${build.generated.dir}/wsimport/<xsl:value-of select="$wsType"/></xsl:attribute>
            <xsl:attribute name="extension">true</xsl:attribute>
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
            <xsl:if test="$isService = 'false'">
                <xsl:attribute name="wsdlLocation"><xsl:value-of select="$wsdlUrlActual" /></xsl:attribute>
            </xsl:if> 
            <xsl:attribute name="catalog"><xsl:value-of select="$Catalog" /></xsl:attribute>
            
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
            <xsl:if test="$isJSR109 = 'false'">
                <jvmarg value="-Djava.endorsed.dirs=${{jaxws.endorsed.dir}}"/>
            </xsl:if>
        </wsimport>
    </xsl:template>
    <!-- END: invokeWsimport template -->
    
</xsl:stylesheet>
