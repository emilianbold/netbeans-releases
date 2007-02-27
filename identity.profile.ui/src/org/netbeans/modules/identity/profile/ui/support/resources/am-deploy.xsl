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
Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.
-->
<!--
XXX should not have changed /1 to /2 for URI of *all* macrodefs; only the ones
that actually changed semantically as a result of supporting multiple compilation
units. E.g. <webproject1:property/> did not change at all, whereas
<webproject1:javac/> did. Need to only update URIs where necessary; otherwise we
cause gratuitous incompatibilities for people overriding macrodef targets. Also
we will need to have an upgrade guide that enumerates all build script incompatibilities
introduced by support for multiple source roots. -jglick
-->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:p="http://www.netbeans.org/ns/project/1"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:webproject1="http://www.netbeans.org/ns/web-project/1"
    xmlns:webproject2="http://www.netbeans.org/ns/web-project/2"
    xmlns:webproject3="http://www.netbeans.org/ns/web-project/3"
    xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
    xmlns:jaxws="http://www.netbeans.org/ns/jax-ws/1"
    exclude-result-prefixes="xalan p projdeps">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
   
    <xsl:template match="/">

        <xsl:comment><![CDATA[
        *** GENERATED - DO NOT EDIT  ***

        For the purpose of easier reading the script
        is divided into following sections:
        - initialization
        - execution
        ]]></xsl:comment>

        <project>
            <xsl:attribute name="default">-am-deploy</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>
            
            <target name="-pre-init-am">
                <subant>
                    <xsl:attribute name="target">-am-classpath-setup</xsl:attribute>
                    <xsl:attribute name="antfile">nbproject/am-deploy.xml</xsl:attribute>
                    <xsl:attribute name="buildpath">${basedir}</xsl:attribute>
                </subant>
                <property file="nbproject/private/private.properties_am"/>
                <delete file="nbproject/private/private.properties_am"/>
            </target>
            
            <target name="-run-deploy-am">
                <xsl:attribute name="depends">-am-deploy</xsl:attribute>
            </target>

            <xsl:comment>
                ======================
                INITIALIZATION SECTION
                ======================
            </xsl:comment>

            <target name="-am-init">
                <xsl:comment> Initialize properties here. </xsl:comment>
                <echo message="am-init:"/>
                <property file="nbproject/private/private.properties"/>
                <condition property="user.properties.file" value="${{netbeans.user}}/build.properties">
                    <not>
                        <isset property="user.properties.file"/>
                    </not>
                </condition>
                <property file="${{user.properties.file}}"/>
                <property file="${{deploy.ant.properties.file}}"/>
                <fail unless="user.properties.file">Must set user properties file</fail>
                <fail unless="deploy.ant.properties.file">Must set ant deploy properties</fail>
                <property file="nbproject/project.properties"/>
                <fail unless="sjsas.root">Must set Sun app server root</fail>
                <property name="am.config.file" value="${{sjsas.root}}/addons/amserver/AMConfig.properties"/>
                <condition property="amconf.dir" value="${{conf.dir}}" else="${{meta.inf}}">
                    <isset property="conf.dir"/>
                </condition>
                <property name="am.config.xml.dir" value="${{basedir}}/${{amconf.dir}}"/>
            </target>
            
            <target name="-am-task-init" unless="netbeans.home">
                <xsl:attribute name="depends">-am-init</xsl:attribute>
                <echo message="am-task-init:"/>
                <taskdef>
                    <xsl:attribute name="name">amdeploy</xsl:attribute>
                    <xsl:attribute name="classname">org.netbeans.modules.identity.ant.AMDeploy</xsl:attribute>
                    <classpath>
                        <xsl:attribute name="path">${libs.IdentityAntTasks.classpath};${libs.jaxb20.classpath}</xsl:attribute>
                    </classpath>
                </taskdef>
                
                <taskdef>
                    <xsl:attribute name="name">amclasspathsetup</xsl:attribute>
                    <xsl:attribute name="classname">org.netbeans.modules.identity.ant.AMClassPathSetup</xsl:attribute>
                    <classpath>
                        <xsl:attribute name="path">${libs.IdentityAntTasks.classpath}</xsl:attribute>
                    </classpath>
                </taskdef>
            </target>

            <xsl:comment>
                ======================
                EXECUTION SECTION
                ======================
            </xsl:comment>

            <target name="-am-deploy" if="libs.IdentityAntTasks.classpath">
                <xsl:attribute name="depends">-am-task-init</xsl:attribute>
                <xsl:attribute name="description">Deploy to Access Manager.</xsl:attribute>
                <echo message="am-deploy:"/>          
                <amdeploy>
                    <xsl:attribute name="amconfigfile">${am.config.file}</xsl:attribute>
                    <xsl:attribute name="amconfigxmldir">${am.config.xml.dir}</xsl:attribute>
                </amdeploy>
            </target>
            
            <target name="-am-classpath-setup" if="libs.IdentityAntTasks.classpath">
                <xsl:attribute name="depends">-am-task-init</xsl:attribute>
                <xsl:attribute name="description">Set up Access Manager classpath</xsl:attribute>
                <echo message="am-classpath-setup:"/>          
                <amclasspathsetup>
                    <xsl:attribute name="propertiesfile">${basedir}/nbproject/private/private.properties</xsl:attribute>
                    <xsl:attribute name="asroot">${sjsas.root}</xsl:attribute>
                </amclasspathsetup>
            </target>
        </project>
    </xsl:template>
</xsl:stylesheet>
