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
    xmlns:project="http://www.netbeans.org/ns/project/1"
    xmlns:j2meproject="http://www.netbeans.org/ns/j2me-project"
    xmlns:xalan="http://xml.apache.org/xslt"
    exclude-result-prefixes="xalan project j2meproject">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
    
        <!-- Annoyingly, the JAXP impl in JRE 1.4.2 seems to randomly reorder attrs. -->
        <!-- (I.e. the DOM tree gets them in an unspecified order?) -->
        <!-- As a workaround, use xsl:attribute for all but the first attr. -->
        <!-- This seems to produce them in the order you want. -->
        <!-- Tedious, but appears to do the job. -->
        <!-- Important for build.xml, which is very visible; not so much for build-impl.xml. -->

        <xsl:comment> You may freely edit this file. See commented blocks below for </xsl:comment>
        <xsl:comment> some examples of how to customize the build. </xsl:comment>
        <xsl:comment> (If you delete it and reopen the project it will be recreated.) </xsl:comment>
        
        <xsl:variable name="name" select="/project:project/project:configuration/j2meproject:data/j2meproject:name"/>
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}">
            <xsl:attribute name="default">jar</xsl:attribute>
            <xsl:attribute name="basedir">.</xsl:attribute>
            <description>Builds, tests, and runs the project <xsl:value-of select="/project:project/project:display-name"/>.</description>
            <import file="nbproject/build-impl.xml"/>

            <xsl:comment><![CDATA[

            There exist several targets which are by default empty and which can be
            used for execution of your tasks. These targets are usually executed
            before and after some main targets. They are:

            pre-init:                 called before initialization of project properties
            post-init:                called after initialization of project properties
            pre-preprocess:           called before text preprocessing of sources
            post-preprocess:          called after text preprocessing of sources
            pre-compile:              called before source compilation
            post-compile:             called after source compilation
            pre-obfuscate:            called before obfuscation 
            post-obfuscate:           called after obfuscation
            pre-preverify:            called before preverification
            post-preverify:           called after preverification
            pre-jar:                  called before jar building
            post-jar:                 called after jar building
            pre-build:                called before final distribution building
            post-build:               called after final distribution building
            pre-clean:                called before cleaning build products
            post-clean:               called after cleaning build products

            Example of pluging a my-special-task after the compilation could look like

            <target name="post-compile">
            <my-special-task>
            <fileset dir="${build.classes.dir}"/>
            </my-special-task>
            </target>

            For list of available properties check the imported
            nbproject/build-impl.xml file.

            Other way how to customize the build is by overriding existing main targets.
            The target of interest are:

            preprocess:               preprocessing
            extract-libs:             extraction of libraries and resources
            compile:                  compilation
            create-jad:               construction of jad and jar manifest source
            obfuscate:                obfuscation
            preverify:                preverification
            jar:                      jar archive building
            run:                      execution
            debug:                    execution in debug mode
            build:                    building of the final distribution
            javadoc:                  javadoc generation

            Example of overriding the target for project execution could look like

            <target name="run" depends="init,jar">
            <my-special-exec jadfile="${dist.dir}/${dist.jad}"/>
            </target>

            Be careful about correct dependencies when overriding original target. 
            Again, for list of available properties which you can use check the target 
            you are overriding in nbproject/build-impl.xml file.

            A special target for-all-configs can be used to run some specific targets for
            all project configurations in a sequence. File nbproject/build-impl.xml 
            already contains some "for-all" targets:
    
            jar-all
            javadoc-all
            clean-all
      
            Example of definition of target iterating over all project configurations:
    
            <target name="jar-all">
            <property name="target.to.call" value="jar"/>
            <antcall target="for-all-configs"/>
            </target>

            ]]></xsl:comment>

        </project>

    </xsl:template>
    
</xsl:stylesheet>
