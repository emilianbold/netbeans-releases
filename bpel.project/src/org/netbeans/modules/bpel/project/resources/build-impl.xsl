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
    xmlns:p="http://www.netbeans.org/ns/project/1"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:bpel="http://www.netbeans.org/ns/j2ee-bpelpro/1"
    xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
    exclude-result-prefixes="xalan p bpel projdeps">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    
<!--*************************************************************************-->    
    <xsl:template match="/">

<xsl:comment>
    *** GENERATED FROM project.xml - DO NOT EDIT  ***
    ***         EDIT ../build.xml INSTEAD         ***
    
    For the purpose of easier reading the script
    is divided into following sections:
    
    - initialization
    - dist
    - cleanup
</xsl:comment>

<xsl:variable name="name" select="/p:project/p:configuration/bpel:data/bpel:name"/>
<project name="{$name}-impl">
    <xsl:attribute name="default">default</xsl:attribute>
    <xsl:attribute name="basedir">..</xsl:attribute>
    
    <target name="default">
        <xsl:attribute name="depends">dist_se</xsl:attribute>
        <xsl:attribute name="description">Build whole project.</xsl:attribute>
    </target>
    
    <xsl:comment> 
        ======================
        INITIALIZATION SECTION 
        ======================
    </xsl:comment>
    
    <target name="-pre-init">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>
    
    <target name="-init-private">
        <xsl:attribute name="depends">-pre-init</xsl:attribute>
        <property file="nbproject/private/private.properties"/>
    </target>
    
    <target name="-init-userdir">
        <xsl:attribute name="depends">-pre-init,-init-private</xsl:attribute>
        <property name="user.properties.file" location="${{netbeans.user}}/build.properties"/>
    </target>
    
    <target name="-init-user">
        <xsl:attribute name="depends">-pre-init,-init-private,-init-userdir</xsl:attribute>
        <property file="${{user.properties.file}}"/>
    </target>
    
    <target name="-init-project">
        <xsl:attribute name="depends">-pre-init,-init-private,-init-userdir,-init-user</xsl:attribute>
        <property file="nbproject/project.properties"/>
    </target>
    
    <target name="-do-init">
        <xsl:attribute name="depends">-pre-init,-init-private,-init-userdir,-init-user,-init-project</xsl:attribute>
        <available file="${{src.dir}}/../retrieved" property="retrieved.exists"/>
    </target>
    
    <target name="-post-init">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>
    
    <target name="-init-check">
        <xsl:attribute name="depends">-pre-init,-init-private,-init-userdir,-init-user,-init-project,-do-init</xsl:attribute>
        <fail unless="src.dir">Must set src.dir</fail>
        <fail unless="build.dir">Must set build.dir</fail>
        <fail unless="dist.dir">Must set dist.dir</fail>
        <fail unless="dist.jar">Must set dist.jar</fail>
    </target>
    
    <target name="-init-taskdefs" if="from.commandline">
        <!-- XXX: we need to seperate bpel model into standalone jars so that 
             we can use them in ant tasks. for now we need to add the jars of 
             this modules -->
        <path id="ant.task.classpath">
              <pathelement location="${{netbeans.home}}/../ide9/modules/ext/resolver-1.2.jar"/>            
              <pathelement location="${{netbeans.home}}/../ide9/modules/org-apache-xml-resolver.jar"/>
              <pathelement location="${{netbeans.home}}/../ide9/modules/org-netbeans-modules-project-ant.jar"/>
              <pathelement location="${{netbeans.home}}/../ide9/modules/org-netbeans-modules-projectapi.jar"/>
              <pathelement location="${{netbeans.home}}/../ide9/modules/org-netbeans-modules-xml-xam.jar"/>
              <pathelement location="${{netbeans.home}}/../ide9/modules/org-netbeans-modules-xml-schema-model.jar"/>
              <pathelement location="${{netbeans.home}}/../ide9/modules/org-netbeans-modules-xml-wsdl-model.jar"/>
              <pathelement location="${{netbeans.home}}/../ide9/modules/org-netbeans-modules-xml-retriever.jar"/>      
              <pathelement location="${{netbeans.home}}/../platform8/core/org-openide-filesystems.jar"/>
              <pathelement location="${{netbeans.home}}/../platform8/lib/org-openide-util.jar"/>
              <pathelement location="${{netbeans.home}}/../platform8/modules/org-openide-dialogs.jar"/>
              <pathelement location="${{netbeans.home}}/../platform8/modules/org-openide-loaders.jar"/>
              <pathelement location="${{netbeans.home}}/../platform8/modules/org-openide-nodes.jar"/>
              <pathelement location="${{netbeans.home}}/../platform8/modules/org-openide-text.jar"/>
              <pathelement location="${{netbeans.home}}/../platform8/modules/org-netbeans-modules-masterfs.jar"/>
              <pathelement location="${{netbeans.home}}/../platform8/modules/org-netbeans-modules-queries.jar"/>
              <pathelement location="${{netbeans.home}}/../soa2/modules/org-netbeans-modules-soa-validation.jar"/>
              <pathelement location="${{netbeans.home}}/../soa2/modules/org-netbeans-modules-soa-ui.jar"/>
              <pathelement location="${{netbeans.home}}/../soa2/modules/org-netbeans-modules-bpel-model.jar"/>
              <pathelement location="${{netbeans.home}}/../soa2/modules/org-netbeans-modules-bpel-project.jar"/>
              <pathelement location="${{netbeans.home}}/../soa2/modules/org-netbeans-modules-bpel-validation.jar"/>
              <pathelement location="${{netbeans.home}}/../soa2/ant/nblib/org-netbeans-modules-bpel-project.jar"/>
              <pathelement location="${{netbeans.home}}/../xml2/modules/ext/jxpath/jxpath1.1.jar"/>
              <pathelement location="${{netbeans.home}}/../xml2/modules/org-netbeans-modules-xml-search.jar"/>
              <pathelement location="${{netbeans.home}}/../xml2/modules/org-netbeans-modules-xml-xpath.jar"/>
              <pathelement location="${{netbeans.home}}/../xml2/modules/org-netbeans-modules-xml-xpath-ext.jar"/>
              <pathelement location="${{netbeans.home}}/../xml2/modules/org-netbeans-modules-xml-wsdl-extensions.jar"/>
        </path>
        
        <taskdef name="validate-project" classname="org.netbeans.modules.bpel.project.anttasks.cli.CliValidateBpelProjectTask">
            <classpath refid="ant.task.classpath"/>
        </taskdef>
        
        <taskdef name="generate-catalog-xml" classname="org.netbeans.modules.bpel.project.anttasks.cli.CliGenerateCatalogTask">
            <classpath refid="ant.task.classpath"/>
        </taskdef>        
        
        <taskdef name="generate-jbi-xml" classname="org.netbeans.modules.bpel.project.anttasks.cli.CliGenerateJbiDescriptorTask">
            <classpath refid="ant.task.classpath"/>
        </taskdef>        
    </target>
    
    <target name="init">
        <xsl:attribute name="depends">-pre-init,-init-private,-init-userdir,-init-user,-init-project,-do-init,-post-init,-init-check,-init-taskdefs</xsl:attribute>
    </target>
    
    <xsl:comment>
        =====================
        DIST BUILDING SECTION
        =====================
    </xsl:comment>
    
    <target name="-pre-dist">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>
    
    <xsl:call-template name="deps.target">
        <xsl:with-param name="targetname" select="'-deps-jar-dist'"/>
        <xsl:with-param name="type" select="'jar'"/>
    </xsl:call-template>
    
    <target name="-do-dist">
        <xsl:attribute name="depends">init,-pre-dist</xsl:attribute>
        <mkdir dir="${{build.dir}}"/>
        
        <xsl:comment>validation</xsl:comment>
        <validate-project buildDirectory="${{basedir}}/${{build.dir}}" sourceDirectory="${{basedir}}/${{src.dir}}" projectClassPath="${{javac.classpath}}" buildDependentProjectDir="${{basedir}}/${{build.dir}}/dependentProjectFiles" classpathRef="ant.task.classpath" allowBuildWithError="${{allow.build.with.error}}"/>
        
        <xsl:comment> copy all files from project source directory to build directory. </xsl:comment>
        <copy todir="${{build.dir}}" preservelastmodified="true" >
            <fileset includes="**/*.bpel,**/*.wsdl,**/*.xsd, **/*.xsl, **/*.xslt" dir="${{src.dir}}"/>
        </copy>
        
        <generate-catalog-xml buildDirectory="${{basedir}}/${{build.dir}}" sourceDirectory="${{basedir}}/${{src.dir}}" projectClassPath="${{javac.classpath}}" classpathRef="ant.task.classpath"/>
        <generate-jbi-xml buildDirectory="${{basedir}}/${{build.dir}}" sourceDirectory="${{basedir}}/${{src.dir}}" projectClassPath="${{javac.classpath}}" classpathRef="ant.task.classpath"/>
        
        <jar compress="${{jar.compress}}" jarfile="${{build.dir}}/SEDeployment.jar">
            <fileset includes="**/*.bpel,**/*.wsdl,**/*.xsd, **/*.xsl, **/*.xslt" dir="${{basedir}}/${{build.dir}}"/>
            
            <fileset dir="${{basedir}}/${{build.dir}}">
                <include name="**/jbi.xml" />
                <include name="**/catalog.xml"/>
            </fileset>
        </jar>
    </target>
    
    <target name="-post-dist">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>
    
    <target name="dist_se">
        <xsl:attribute name="depends">init,-pre-dist,-deps-jar-dist,-do-dist,-post-dist</xsl:attribute>
        <xsl:attribute name="description">Build distribution.</xsl:attribute>
    </target>
    
    <xsl:comment>
        ===============
        CLEANUP SECTION
        ===============
    </xsl:comment>
    
    <target name="-pre-clean">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>
    
    <xsl:call-template name="deps.target">
        <xsl:with-param name="targetname" select="'-deps-clean'"/>
    </xsl:call-template>
    
    <target name="-do-clean">
        <xsl:attribute name="depends">init,-pre-clean</xsl:attribute>
        
        <delete dir="${{build.dir}}"/>
        <delete dir="${{dist.dir}}"/>
    </target>
    
    <target name="-post-clean">
        <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
        <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
    </target>

    <target name="clean">
        <xsl:attribute name="depends">init,-pre-clean,-deps-clean,-do-clean,-post-clean</xsl:attribute>
        <xsl:attribute name="description">Clean build products.</xsl:attribute>
    </target>
</project>

    </xsl:template>

    
    <!---
    Generic template to build subdependencies of a certain type.
    Feel free to copy into other modules.
    @param targetname required name of target to generate
    @param type artifact-type from project.xml to filter on; optional, if not 
    specified, uses all references, and looks for clean targets rather than 
    build targets
    @return an Ant target which builds (or cleans) all known subprojects
    -->
<!--*************************************************************************-->    
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
<!--*************************************************************************-->

</xsl:stylesheet>
