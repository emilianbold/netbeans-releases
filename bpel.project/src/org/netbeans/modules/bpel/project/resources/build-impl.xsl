<?xml version="1.0" encoding="UTF-8"?>
<!--
  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

  Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

  Oracle and Java are registered trademarks of Oracle and/or its affiliates.
  Other names may be trademarks of their respective owners.

  The contents of this file are subject to the terms of either the GNU
  General Public License Version 2 only ("GPL") or the Common
  Development and Distribution License("CDDL") (collectively, the
  "License"). You may not use this file except in compliance with the
  License. You can obtain a copy of the License at
  http://www.netbeans.org/cddl-gplv2.html
  or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  specific language governing permissions and limitations under the
  License. When distributing the software, include this License Header
  Notice in each file and include the License file at
  nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
  particular file as subject to the "Classpath" exception as provided
  by Oracle in the GPL Version 2 section of the License file that
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
    xmlns:bpel="http://www.netbeans.org/ns/j2ee-bpelpro/1"
    xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
    exclude-result-prefixes="xalan p bpel projdeps">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

<xsl:comment>
    *** GENERATED FROM project.xml - DO NOT EDIT ***
    ***           EDIT ../build.xml INSTEAD      ***
</xsl:comment>

<xsl:variable name="name" select="/p:project/p:configuration/bpel:data/bpel:name"/>
<project name="{$name}-impl">
    <xsl:attribute name="default">default</xsl:attribute>
    <xsl:attribute name="basedir">..</xsl:attribute>
    
    <target name="default">
        <xsl:attribute name="depends">dist_se</xsl:attribute>
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
        <available file="${{src.dir}}/../retrieved" property="retrieved.exists"/>
    </target>
    
    <target name="post-init">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>
    
    <target name="init-check">
        <xsl:attribute name="depends">pre-init,init-private,init-userdir,init-user,init-project,do-init</xsl:attribute>
        <fail unless="src.dir">Must set src.dir</fail>
        <fail unless="build.dir">Must set build.dir</fail>
        <fail unless="dist.dir">Must set dist.dir</fail>
        <fail unless="dist.jar">Must set dist.jar</fail>
    </target>
    
    <target name="init-taskdefs" if="from.commandline">
        <taskdef name="validate-project" classname="org.netbeans.modules.bpel.project.anttasks.cli.CliValidateBpelProjectTask">
            <classpath refid="ant.project.classpath"/>
        </taskdef>
        
        <taskdef name="generate-catalog-xml" classname="org.netbeans.modules.bpel.project.anttasks.cli.CliGenerateCatalogTask">
            <classpath refid="ant.project.classpath"/>
        </taskdef>        
        
        <taskdef name="generate-jbi-xml" classname="org.netbeans.modules.bpel.project.anttasks.cli.CliGenerateJbiDescriptorTask">
            <classpath refid="ant.project.classpath"/>
        </taskdef>        
    </target>
    
    <target name="init">
        <xsl:attribute name="depends">pre-init,init-private,init-userdir,init-user,init-project,do-init,post-init,init-check,init-taskdefs</xsl:attribute>
    </target>
    
    <xsl:comment>
        DIST BUILDING SECTION
    </xsl:comment>
    
    <target name="pre-dist">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>
    
    <xsl:call-template name="deps.target">
        <xsl:with-param name="targetname" select="'deps-jar-dist'"/>
        <xsl:with-param name="type" select="'jar'"/>
    </xsl:call-template>
    
    <target name="do-dist">
        <xsl:attribute name="depends">init,pre-dist</xsl:attribute>
        <mkdir dir="${{build.dir}}"/>

        <validate-project buildDirectory="${{basedir}}/${{build.dir}}" sourceDirectory="${{basedir}}/${{src.dir}}" allowBuildWithError="${{allow.build.with.error}}" validation="${{validation}}"/>
        
        <copy todir="${{build.dir}}" preservelastmodified="true" >
            <fileset includes="**/*.bpel,**/*.wsdl,**/*.xsd, **/*.xsl, **/*.xslt, **/*.jar" dir="${{src.dir}}"/>
        </copy>
        
        <generate-catalog-xml buildDirectory="${{basedir}}/${{build.dir}}" sourceDirectory="${{basedir}}/${{src.dir}}"/>
        <generate-jbi-xml buildDirectory="${{basedir}}/${{build.dir}}" sourceDirectory="${{basedir}}/${{src.dir}}"/>
        
        <jar compress="${{jar.compress}}" jarfile="${{build.dir}}/SEDeployment.jar">
            <fileset includes="**/*.bpel,**/*.wsdl,**/*.xsd, **/*.xsl, **/*.xslt, **/*.jar" excludes="SEDeployment.jar" dir="${{basedir}}/${{build.dir}}"/>
            
            <fileset dir="${{basedir}}/${{build.dir}}">
                <include name="**/jbi.xml" />
                <include name="**/catalog.xml"/>
            </fileset>
        </jar>
    </target>
    
    <target name="post-dist">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>
    
    <target name="dist_se">
        <xsl:attribute name="depends">init,pre-dist,deps-jar-dist,do-dist,post-dist</xsl:attribute>
    </target>
    
    <xsl:comment>
        CLEANUP SECTION
    </xsl:comment>
    
    <target name="pre-clean">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>
    
    <xsl:call-template name="deps.target">
        <xsl:with-param name="targetname" select="'deps-clean'"/>
    </xsl:call-template>
    
    <target name="do-clean">
        <xsl:attribute name="depends">init,pre-clean</xsl:attribute>
        
        <delete dir="${{build.dir}}"/>
        <delete dir="${{dist.dir}}"/>
    </target>
    
    <target name="post-clean">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>

    <target name="clean">
        <xsl:attribute name="depends">init,pre-clean,deps-clean,do-clean,post-clean</xsl:attribute>
    </target>
</project>

    </xsl:template>

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
                <xsl:attribute name="antfile">${project.<xsl:value-of select="$subproj"/>}<xsl:value-of select="$scriptdirslash"/>/<xsl:value-of select="$scriptfile"/></xsl:attribute>
            </ant>
        </xsl:for-each>
    </target>
    </xsl:template>
</xsl:stylesheet>
