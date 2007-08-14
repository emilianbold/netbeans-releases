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
                xmlns:j2seproject3="http://www.netbeans.org/ns/j2se-project/3"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:jaxws="http://www.netbeans.org/ns/jax-ws/1"> 
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
        
        <project>

            
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
                        <mkdir dir="${{build.generated.dir}}/wsimport/binaries"/>
                    </xsl:if>
                    <taskdef name="wsimport" classname="com.sun.tools.ws.ant.WsImport">
                        <classpath path="${{libs.jaxws21.classpath}}"/>
                    </taskdef>
                </target>
            </xsl:if>
            
            <!-- wsimport-client targets - one for each jaxws client -->
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
                        <wsimport
                            fork="true"
                            xendorsed="true"
                            sourcedestdir="${{build.generated.dir}}/wsimport/client"
                            extension="true"
                            package="{$package_name}"
                            destdir="${{build.generated.dir}}/wsimport/binaries"
                            wsdl="${{basedir}}/xml-resources/web-service-references/{$wsname}/wsdl/{$wsdl_url}"
                            wsdlLocation="{$wsdl_url_actual}"
                            catalog="{$catalog}">
                            <xsl:if test="jaxws:binding">
                                <binding dir="xml-resources/web-service-references/{$wsname}/bindings">
                                    <xsl:attribute name="includes">
                                        <xsl:for-each select="jaxws:binding">
                                            <xsl:if test="position()!=1"><xsl:text>;</xsl:text></xsl:if>
                                            <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                        </xsl:for-each>
                                    </xsl:attribute>
                                </binding>
                            </xsl:if>
                            <jvmarg value="-Djava.endorsed.dirs=${{jaxws.endorsed.dir}}"/>
                        </wsimport>
                    </xsl:if>
                    <xsl:if test="not(jaxws:package-name/@forceReplace)">
                        <wsimport
                            fork="true"
                            xendorsed="true"
                            sourcedestdir="${{build.generated.dir}}/wsimport/client"
                            extension="true"
                            destdir="${{build.generated.dir}}/wsimport/binaries"
                            wsdl="${{basedir}}/xml-resources/web-service-references/{$wsname}/wsdl/{$wsdl_url}"
                            wsdlLocation="{$wsdl_url_actual}"
                            catalog="{$catalog}">
                            <xsl:if test="jaxws:binding">
                                <binding dir="xml-resources/web-service-references/{$wsname}/bindings">
                                    <xsl:attribute name="includes">
                                        <xsl:for-each select="jaxws:binding">
                                            <xsl:if test="position()!=1"><xsl:text>;</xsl:text></xsl:if>
                                            <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                        </xsl:for-each>
                                    </xsl:attribute>
                                </binding>
                            </xsl:if>
                            <jvmarg value="-Djava.endorsed.dirs=${{jaxws.endorsed.dir}}"/>
                        </wsimport>
                    </xsl:if>
                    <copy todir="${{build.classes.dir}}">
                        <fileset dir="${{build.generated.dir}}/wsimport/binaries" includes="**/*.xml"/>
                    </copy>
                </target>
                <target name="wsimport-client-clean-{$wsname}" depends="-init-project">
                    <delete dir="${{build.generated.dir}}/wsimport/client/{$package_path}"/>
                </target>
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
                
                <target name="wsimport-client-compile" depends="-pre-pre-compile">
                    <j2seproject3:depend srcdir="${{build.generated.dir}}/wsimport/client" classpath="${{libs.jaxws21.classpath}}:${{javac.classpath}}" destdir="${{build.classes.dir}}"/>
                    <j2seproject3:javac srcdir="${{build.generated.dir}}/wsimport/client" classpath="${{libs.jaxws21.classpath}}:${{javac.classpath}}" destdir="${{build.classes.dir}}" javac.compilerargs.jaxws="-Djava.endorsed.dirs='${{jaxws.endorsed.dir}}'"/>
                </target>
                
            </xsl:if>
            
        </project>
        
    </xsl:template>

</xsl:stylesheet>
