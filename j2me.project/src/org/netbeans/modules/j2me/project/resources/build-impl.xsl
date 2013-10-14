<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : build-impl.xsl
    Created on : October 9, 2013, 11:00 AM
    Author     : tom
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:j2meproject1="http://www.netbeans.org/ns/j2me-embedded-project/1"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                xmlns:projdeps2="http://www.netbeans.org/ns/ant-project-references/2"
                xmlns:libs="http://www.netbeans.org/ns/ant-project-libraries/1"
                exclude-result-prefixes="xalan p projdeps projdeps2 j2meproject1 libs">
    <!-- XXX should use namespaces for NB in-VM tasks from ant/browsetask and debuggerjpda/ant (Ant 1.6.1 and higher only) -->
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

        <xsl:comment><![CDATA[
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***

For the purpose of easier reading the script
is divided into following sections:

  - initialization
  - compilation
  - jar
  - execution
  - debugging
  - javadoc
  - test compilation
  - test execution
  - test debugging
  - applet
  - cleanup

        ]]></xsl:comment>

        <xsl:variable name="name" select="/p:project/p:configuration/j2meproject1:data/j2meproject1:name"/>
        <!-- Synch with build-impl.xsl: -->
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-impl">
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>

            <fail message="Please build using Ant 1.8.0 or higher.">
                <condition>
                    <not>
                        <antversion atleast="1.8.0"/>
                    </not>
                </condition>
            </fail>

            <target name="default">
                <xsl:attribute name="depends">jar,javadoc</xsl:attribute>
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
                <property file="nbproject/private/config.properties"/>
                <property file="nbproject/private/configs/${{config}}.properties"/>
                <property file="nbproject/private/private.properties"/>
            </target>

            <xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">
                <target name="-pre-init-libraries">
                    <property name="libraries.path">
                        <xsl:attribute name="location">
                            <xsl:value-of select="/p:project/p:configuration/libs:libraries/libs:definitions"/>
                        </xsl:attribute>
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
                <target name="-init-libraries" depends="-pre-init,-init-private,-init-private-libraries">
                    <loadproperties srcfile="${{libraries.path}}" encoding="ISO-8859-1">
                        <filterchain>
                            <replacestring from="$${{base}}" to="${{libraries.dir}}"/>
                            <escapeunicode/>
                        </filterchain>
                    </loadproperties>
                </target>
            </xsl:if>

            <target name="-init-user">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if></xsl:attribute>
                <property file="${{user.properties.file}}"/>
                <xsl:comment> The two properties below are usually overridden </xsl:comment>
                <xsl:comment> by the active platform. Just a fallback. </xsl:comment>
                <property name="default.javac.source" value="1.3"/>
                <property name="default.javac.target" value="1.3"/>
            </target>

            <target name="-init-project">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user</xsl:attribute>
                <property file="nbproject/configs/${{config}}.properties"/>
                <property file="nbproject/project.properties"/>
            </target>

            <target name="-do-init">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-init-macrodef-property</xsl:attribute>

                <j2meproject1:property name="platform.home" value="platforms.${{platform.active}}.home"/>
                <j2meproject1:property name="platform.bootcp" value="platforms.${{platform.active}}.bootclasspath"/>

                <j2meproject1:property name="platform.sdk.home.tmp" value="platforms.${{platform.sdk}}.home"/>
                <condition property="platform.sdk.home" value="${java.home}">
                    <equals arg1="${{platform.sdk.home.tmp}}" arg2="$${{platforms.${{platform.sdk}}.home}}"/>
                </condition>
                <property name="platform.sdk.home" value="${{platform.sdk.home.tmp}}"/>
                <j2meproject1:property name="platform.javac.tmp" value="platforms.${{platform.sdk}}.javac"/>
                <condition property="platform.javac" value="${{platform.sdk.home}}/bin/javac">
                    <equals arg1="${{platform.javac.tmp}}" arg2="$${{platforms.${{platform.sdk}}.javac}}"/>
                </condition>
                <property name="platform.javac" value="${{platform.javac.tmp}}"/>
                <j2meproject1:property name="platform.javadoc.tmp" value="platforms.${{platform.sdk}}.javadoc"/>
                <condition property="platform.javadoc" value="${{platform.sdk.home}}/bin/javadoc">
                    <equals arg1="${{platform.javadoc.tmp}}" arg2="$${{platforms.${{platform.sdk}}.javadoc}}"/>
                </condition>
                <property name="platform.javadoc" value="${{platform.javadoc.tmp}}"/>
                <condition property="platform.invalid" value="true">
                    <or>
                        <contains string="${{platform.javac}}" substring="$${{platforms."/>
                        <contains string="${{platform.javadoc}}" substring="$${{platforms."/>
                    </or>
                </condition>
                <fail unless="platform.home">Must set platform.home</fail>
                <fail unless="platform.bootcp">Must set platform.bootcp</fail>
                <fail unless="platform.java">Must set platform.java</fail>
                <fail unless="platform.javac">Must set platform.javac</fail>
                <fail if="platform.invalid">
                    The Compile Platform is not correctly set up.
                    Your active compile platform is: ${platform.sdk}, but the corresponding property "platforms.${platform.sdk}.home" is not found in the project's properties files.
                    Either open the project in the IDE and setup the Platform with the same name or add it manually.
                    For example like this:
                    ant -Duser.properties.file=&lt;path_to_property_file&gt; jar (where you put the property "platforms.${platform.sdk}.home" in a .properties file)
                    or ant -Dplatforms.${platform.sdk}.home=&lt;path_to_JDK_home&gt; jar (where no properties file is used)
                </fail>
                <available file="${{manifest.file}}" property="manifest.available"/>
                <condition property="do.archive">
                    <not>
                        <istrue value="${{jar.archive.disabled}}"/>  <!-- Disables archive creation when archiving is overriden by an extension -->
                    </not>
                </condition>
                <condition property="do.mkdist">
                    <and>
                        <isset property="do.archive"/>
                        <isset property="libs.CopyLibs.classpath"/>
                        <not>
                            <istrue value="${{mkdist.disabled}}"/>
                        </not>
                    </and>
                </condition>
                <condition property="do.archive+manifest.available">
                    <and>
                        <isset property="manifest.available"/>
                        <istrue value="${{do.archive}}"/>
                    </and>
                </condition>

                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2meproject1:data/j2meproject1:source-roots"/>
                    <xsl:with-param name="propName">have.sources</xsl:with-param>
                </xsl:call-template>
                <condition property="netbeans.home+have.tests">
                    <and>
                        <isset property="netbeans.home"/>
                        <isset property="have.tests"/>
                    </and>
                </condition>
                <condition property="no.javadoc.preview">
                    <and>
                        <isset property="javadoc.preview"/>
                        <isfalse value="${{javadoc.preview}}"/>
                    </and>
                </condition>
                <property name="run.jvmargs" value=""/>
                <property name="run.jvmargs.ide" value=""/>
                <property name="javac.compilerargs" value=""/>
                <property name="work.dir" value="${{basedir}}"/>
                <condition property="no.deps">
                    <and>
                        <istrue value="${{no.dependencies}}"/>
                    </and>
                </condition>
                <property name="javac.debug" value="true"/>
                <property name="javadoc.preview" value="true"/>
                <property name="application.args" value=""/>
                <property name="source.encoding" value="${{file.encoding}}"/>
                <property name="runtime.encoding" value="${{source.encoding}}"/>
                <condition property="javadoc.encoding.used" value="${{javadoc.encoding}}">
                    <and>
                        <isset property="javadoc.encoding"/>
                        <not>
                            <equals arg1="${{javadoc.encoding}}" arg2=""/>
                        </not>
                    </and>
                </condition>
                <property name="javadoc.encoding.used" value="${{source.encoding}}"/>
                <property name="includes" value="**"/>
                <property name="excludes" value=""/>
                <property name="do.depend" value="false"/>
                <condition property="do.depend.true">
                    <istrue value="${{do.depend}}"/>
                </condition>
                <path id="endorsed.classpath.path" path="${{endorsed.classpath}}"/>
                <condition property="endorsed.classpath.cmd.line.arg" value="-Xbootclasspath/p:'${{toString:endorsed.classpath.path}}'" else="">
                    <and>
                        <isset property="endorsed.classpath"/>
                        <not>
                            <equals arg1="${{endorsed.classpath}}" arg2="" trim="true"/>
                        </not>
                    </and>
                </condition>
                <property name="jar.index" value="false"/>
                <property name="jar.index.metainf" value="${{jar.index}}"/>
                <property name="copylibs.rebase" value="true"/>
            </target>

            <target name="-post-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-init-check">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-do-init</xsl:attribute>
                <!-- XXX XSLT 2.0 would make it possible to use a for-each here -->
                <!-- Note that if the properties were defined in project.xml that would be easy -->
                <!-- But required props should be defined by the AntBasedProjectType, not stored in each project -->
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2meproject1:data/j2meproject1:source-roots"/>
                </xsl:call-template>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.dir">Must set build.classes.dir</fail>
                <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail>
                <fail unless="build.test.classes.dir">Must set build.test.classes.dir</fail>
                <fail unless="build.test.results.dir">Must set build.test.results.dir</fail>
                <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>
                <fail unless="dist.jar">Must set dist.jar</fail>
            </target>

            <target name="-init-macrodef-property">
                <macrodef>
                    <xsl:attribute name="name">property</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2me-embedded-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">name</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">value</xsl:attribute>
                    </attribute>
                    <sequential>
                        <property name="@{{name}}" value="${{@{{value}}}}"/>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-javac-with-processors" depends="-init-ap-cmdline-properties" if="ap.supported.internal">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2me-embedded-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2meproject1:data/j2meproject1:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
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
                        <xsl:attribute name="name">processorpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.processorpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">apgeneratedsrcdir</xsl:attribute>
                        <xsl:attribute name="default">${build.generated.sources.dir}/ap-source-output</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">debug</xsl:attribute>
                        <xsl:attribute name="default">${javac.debug}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">sourcepath</xsl:attribute>
                        <xsl:attribute name="default">${empty.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">gensrcdir</xsl:attribute>
                        <xsl:attribute name="default">${empty.dir}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property name="empty.dir" location="${{build.dir}}/empty"/><!-- #157692 -->
                        <mkdir dir="${{empty.dir}}"/>
                        <mkdir dir="@{{apgeneratedsrcdir}}"/>
                        <javac>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <xsl:attribute name="sourcepath">@{sourcepath}</xsl:attribute>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                            <xsl:attribute name="source">${javac.source}</xsl:attribute>
                            <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            <xsl:attribute name="includes">@{includes}</xsl:attribute>
                            <xsl:attribute name="excludes">@{excludes}</xsl:attribute>
                            <xsl:attribute name="fork">yes</xsl:attribute>
                            <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                            <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
                            <xsl:attribute name="includeantruntime">false</xsl:attribute>
                            <src>
                                <dirset dir="@{{gensrcdir}}" erroronmissingdir="false">
                                    <include name="*"/>
                                </dirset>
                            </src>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <compilerarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <compilerarg line="${{javac.compilerargs}}"/>
                            <compilerarg value="-processorpath" />
                            <compilerarg path="@{{processorpath}}:${{empty.dir}}" />
                            <compilerarg line="${{ap.processors.internal}}" />
                            <compilerarg line="${{annotation.processing.processor.options}}" />
                            <compilerarg value="-s" />
                            <compilerarg path="@{{apgeneratedsrcdir}}" />
                            <compilerarg line="${{ap.proc.none.internal}}" />
                            <customize/>
                        </javac>
                    </sequential>
                </macrodef>
            </target>
            <target name="-init-macrodef-javac-without-processors" depends="-init-ap-cmdline-properties" unless="ap.supported.internal">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2me-embedded-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2meproject1:data/j2meproject1:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
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
                        <xsl:attribute name="name">processorpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.processorpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">apgeneratedsrcdir</xsl:attribute>
                        <xsl:attribute name="default">${build.generated.sources.dir}/ap-source-output</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">debug</xsl:attribute>
                        <xsl:attribute name="default">${javac.debug}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">sourcepath</xsl:attribute>
                        <xsl:attribute name="default">${empty.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">gensrcdir</xsl:attribute>
                        <xsl:attribute name="default">${empty.dir}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property name="empty.dir" location="${{build.dir}}/empty"/><!-- #157692 -->
                        <mkdir dir="${{empty.dir}}"/>
                        <javac>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <xsl:attribute name="sourcepath">@{sourcepath}</xsl:attribute>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                            <xsl:attribute name="source">${javac.source}</xsl:attribute>
                            <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            <xsl:attribute name="includes">@{includes}</xsl:attribute>
                            <xsl:attribute name="excludes">@{excludes}</xsl:attribute>
                            <xsl:attribute name="fork">yes</xsl:attribute>
                            <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                            <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
                            <xsl:attribute name="includeantruntime">false</xsl:attribute>
                            <src>
                                <dirset dir="@{{gensrcdir}}" erroronmissingdir="false">
                                    <include name="*"/>
                                </dirset>
                            </src>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <compilerarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <compilerarg line="${{javac.compilerargs}}"/>
                            <customize/>
                        </javac>
                    </sequential>
                </macrodef>
            </target>
            <target name="-init-macrodef-javac" depends="-init-macrodef-javac-with-processors,-init-macrodef-javac-without-processors">
                <macrodef> <!-- #36033, #85707 -->
                    <xsl:attribute name="name">depend</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2me-embedded-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2meproject1:data/j2meproject1:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.classpath}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <depend>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="cache">${build.dir}/depcache</xsl:attribute>
                            <xsl:attribute name="includes">${includes}</xsl:attribute>
                            <xsl:attribute name="excludes">${excludes}</xsl:attribute>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                        </depend>
                    </sequential>
                </macrodef>
                <macrodef> <!-- #85707 -->
                    <xsl:attribute name="name">force-recompile</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2me-embedded-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <fail unless="javac.includes">Must set javac.includes</fail>
                        <!-- XXX one little flaw in this weird trick: does not work on folders. -->
                        <pathconvert>
                            <xsl:attribute name="property">javac.includes.binary</xsl:attribute>
                            <xsl:attribute name="pathsep">${line.separator}</xsl:attribute>
                            <path>
                                <filelist>
                                    <xsl:attribute name="dir">@{destdir}</xsl:attribute>
                                    <xsl:attribute name="files">${javac.includes}</xsl:attribute>
                                </filelist>
                            </path>
                            <globmapper>
                                <xsl:attribute name="from">*.java</xsl:attribute>
                                <xsl:attribute name="to">*.class</xsl:attribute>
                            </globmapper>
                        </pathconvert>
                        <tempfile property="javac.includesfile.binary" deleteonexit="true"/>
                        <echo message="${{javac.includes.binary}}" file="${{javac.includesfile.binary}}"/>
                        <delete>
                            <files includesfile="${{javac.includesfile.binary}}"/>
                        </delete>
                        <delete>
                            <fileset file="${{javac.includesfile.binary}}"/>  <!-- deleteonexit keeps the file during IDE run -->
                        </delete>
                    </sequential>
                </macrodef>
            </target>


            <target name="-init-macrodef-nbjpda" depends="-init-debug-args">
                <macrodef>
                    <xsl:attribute name="name">nbjpdastart</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2me-embedded-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">name</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">stopclassname</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <sequential>
                        <nbjpdastart transport="${{debug-transport}}" addressproperty="jpda.address" name="@{{name}}" stopclassname="@{{stopclassname}}">
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <bootclasspath>
                                <path path="${{platform.bootcp}}"/>
                            </bootclasspath>
                        </nbjpdastart>
                    </sequential>
                </macrodef>
                <macrodef>
                    <xsl:attribute name="name">nbjpdareload</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2me-embedded-project/1</xsl:attribute>
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

            <target name="-init-debug-args">                                                            
                <property name="debug-args-line" value="-Xdebug"/>
                <condition property="debug-transport-by-os" value="dt_shmem" else="dt_socket">
                    <os family="windows"/>
                </condition>
                <condition property="debug-transport" value="${{debug.transport}}" else="${{debug-transport-by-os}}">
                    <isset property="debug.transport"/>
                </condition>
            </target>

            <target name="-init-macrodef-debug" depends="-init-debug-args">
                <macrodef>
                    <xsl:attribute name="name">debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2me-embedded-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <!-- TODO -->
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-copylibs">
                <macrodef>
                    <xsl:attribute name="name">copylibs</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2me-embedded-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">manifest</xsl:attribute>
                        <xsl:attribute name="default">${manifest.file}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property location="${{build.classes.dir}}" name="build.classes.dir.resolved"/>
                        <pathconvert property="run.classpath.without.build.classes.dir">
                            <path path="${{run.classpath}}"/>
                            <map from="${{build.classes.dir.resolved}}" to=""/>
                        </pathconvert>
                        <pathconvert pathsep=" " property="jar.classpath">
                            <path path="${{run.classpath.without.build.classes.dir}}"/>
                            <chainedmapper>
                                <flattenmapper/>
                                <filtermapper>
                                    <replacestring from=" " to="%20"/>
                                </filtermapper>
                                <globmapper from="*" to="lib/*"/>
                            </chainedmapper>
                        </pathconvert>
                        <taskdef classname="org.netbeans.modules.java.j2seproject.copylibstask.CopyLibs" classpath="${{libs.CopyLibs.classpath}}" name="copylibs"/>
                        <copylibs rebase="${{copylibs.rebase}}" compress="${{jar.compress}}" jarfile="${{dist.jar}}" manifest="@{{manifest}}" runtimeclasspath="${{run.classpath.without.build.classes.dir}}" index="${{jar.index}}" indexMetaInf="${{jar.index.metainf}}" excludeFromCopy="${{copylibs.excludes}}">
                            <fileset dir="${{build.classes.dir}}" excludes="${{dist.archive.excludes}}"/>
                            <manifest>
                                <attribute name="Class-Path" value="${{jar.classpath}}"/>
                                <customize/>
                            </manifest>
                        </copylibs>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-presetdef-jar">
                <presetdef>
                    <xsl:attribute name="name">jar</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2me-embedded-project/1</xsl:attribute>
                    <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}" index="${{jar.index}}">
                        <j2meproject1:fileset dir="${{build.classes.dir}}" excludes="${{dist.archive.excludes}}"/>
                        <!-- XXX should have a property serving as the excludes list -->
                    </jar>
                </presetdef>
            </target>

            <target name="-init-ap-cmdline-properties">
                <property name="annotation.processing.enabled" value="true" />
                <property name="annotation.processing.processors.list" value="" />
                <property name="annotation.processing.processor.options" value="" />
                <property name="annotation.processing.run.all.processors" value="true" />
                <property name="javac.processorpath" value="${{javac.classpath}}" />
                <property name="javac.test.processorpath" value="${{javac.test.classpath}}"/>
                <condition property="ap.supported.internal" value="true">
                    <not>
                        <matches string="${{javac.source}}" pattern="1\.[0-5](\..*)?" />
                    </not>
                </condition>
            </target>
            <target name="-init-ap-cmdline-supported" depends="-init-ap-cmdline-properties" if="ap.supported.internal">
                <condition property="ap.processors.internal" value="-processor ${{annotation.processing.processors.list}}" else="">
                    <isfalse value="${{annotation.processing.run.all.processors}}" />
                </condition>
                <condition property="ap.proc.none.internal" value="-proc:none" else="">
                    <isfalse value="${{annotation.processing.enabled}}" />
                </condition>
            </target>
            <target name="-init-ap-cmdline" depends="-init-ap-cmdline-properties,-init-ap-cmdline-supported">
                <property name="ap.cmd.line.internal" value=""/>
            </target>

            <target name="init">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-do-init,-post-init,-init-check,-init-macrodef-property,-init-macrodef-javac,-init-macrodef-test,-init-macrodef-test-debug,-init-macrodef-nbjpda,-init-macrodef-debug,-init-macrodef-java,-init-presetdef-jar,-init-ap-cmdline</xsl:attribute>
            </target>

            <xsl:comment>
                ===================
                COMPILATION SECTION
                ===================
            </xsl:comment>

            <xsl:call-template name="deps.target">
                <xsl:with-param name="kind" select="'jar'"/>
                <xsl:with-param name="type" select="'jar'"/>
            </xsl:call-template>

            <target name="-verify-automatic-build">
                <xsl:attribute name="depends">init,-check-automatic-build,-clean-after-automatic-build</xsl:attribute>
            </target>

            <target name="-check-automatic-build">
                <xsl:attribute name="depends">init</xsl:attribute>
                <available file="${{build.classes.dir}}/.netbeans_automatic_build" property="netbeans.automatic.build"/>
            </target>

            <target name="-clean-after-automatic-build" depends="init" if="netbeans.automatic.build">
                <antcall target="clean" />
            </target>

            <target name="-pre-pre-compile">
                <xsl:attribute name="depends">init,deps-jar</xsl:attribute>
                <mkdir dir="${{build.classes.dir}}"/>
            </target>

            <target name="-pre-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-compile-depend" if="do.depend.true">
                <pathconvert property="build.generated.subdirs">
                    <dirset dir="${{build.generated.sources.dir}}" erroronmissingdir="false">
                        <include name="*"/>
                    </dirset>
                </pathconvert>
                <j2meproject1:depend>
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2meproject1:data/j2meproject1:source-roots"/>
                        </xsl:call-template>
                        <xsl:text>:${build.generated.subdirs}</xsl:text>
                    </xsl:attribute>
                </j2meproject1:depend>
            </target>

            <target name="-do-compile">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile,-compile-depend</xsl:attribute>
                <xsl:attribute name="if">have.sources</xsl:attribute>
                <j2meproject1:javac gensrcdir="${{build.generated.sources.dir}}"/>
                <copy todir="${{build.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2meproject1:data/j2meproject1:source-roots"/>
                        <!-- XXX should perhaps use ${includes} and ${excludes} -->
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                </copy>
            </target>

            <target name="-post-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="compile">
                <xsl:attribute name="depends">init,deps-jar,-verify-automatic-build,-pre-pre-compile,-pre-compile,-do-compile,-post-compile</xsl:attribute>
                <xsl:attribute name="description">Compile project.</xsl:attribute>
            </target>

            <target name="-pre-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-do-compile-single">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile</xsl:attribute>
                <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
                <j2meproject1:force-recompile/>
                <xsl:element name="j2meproject1:javac">
                    <xsl:attribute name="includes">${javac.includes}</xsl:attribute>
                    <xsl:attribute name="excludes"/>
                    <xsl:attribute name="sourcepath"> <!-- #115918 -->
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2meproject1:data/j2meproject1:source-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="gensrcdir">${build.generated.sources.dir}</xsl:attribute>
                </xsl:element>
            </target>

            <target name="-post-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="compile-single">
                <xsl:attribute name="depends">init,deps-jar,-verify-automatic-build,-pre-pre-compile,-pre-compile-single,-do-compile-single,-post-compile-single</xsl:attribute>
            </target>

            <xsl:comment>
                ====================
                JAR BUILDING SECTION
                ====================
            </xsl:comment>

            <target name="-pre-pre-jar">
                <xsl:attribute name="depends">init</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
            </target>

            <target name="-pre-jar">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-do-jar-create-manifest">
                <xsl:attribute name="depends">init</xsl:attribute>
                <xsl:attribute name="if">do.archive</xsl:attribute>
                <xsl:attribute name="unless">manifest.available</xsl:attribute>
                <tempfile destdir="${{build.dir}}" deleteonexit="true" property="tmp.manifest.file"/>
                <touch file="${{tmp.manifest.file}}" verbose="false"/>
            </target>

            <target name="-do-jar-copy-manifest">
                <xsl:attribute name="depends">init</xsl:attribute>
                <xsl:attribute name="if">do.archive+manifest.available</xsl:attribute>
                <tempfile destdir="${{build.dir}}" deleteonexit="true" property="tmp.manifest.file"/>
                <copy file="${{manifest.file}}" tofile="${{tmp.manifest.file}}"/>
            </target>


            <target name="-do-jar-copylibs">
                <xsl:attribute name="depends">init,-init-macrodef-copylibs,compile,-pre-pre-jar,-pre-jar,-do-jar-create-manifest,-do-jar-copy-manifest</xsl:attribute>
                <xsl:attribute name="if">do.mkdist</xsl:attribute>
                <j2meproject1:copylibs manifest="${{tmp.manifest.file}}"/>
            </target>

            <target name="-do-jar-jar">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar,-do-jar-create-manifest,-do-jar-copy-manifest</xsl:attribute>
                <xsl:attribute name="if">do.archive</xsl:attribute>
                <xsl:attribute name="unless">do.mkdist</xsl:attribute>
                <j2meproject1:jar manifest="${{tmp.manifest.file}}"/>
            </target>

            <target name="-do-jar-delete-manifest" >
                <xsl:attribute name="depends">-do-jar-copylibs</xsl:attribute>
                <xsl:attribute name="if">do.archive</xsl:attribute>
                <delete>
                    <fileset file="${{tmp.manifest.file}}"/>
                </delete>
            </target>


            <target name="-do-jar-without-libraries">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar,-do-jar-create-manifest,-do-jar-copy-manifest,-do-jar-jar,-do-jar-delete-manifest</xsl:attribute>
            </target>
            <target name="-do-jar-with-libraries">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar,-do-jar-create-manifest,-do-jar-copy-manifest,-do-jar-copylibs,-do-jar-delete-manifest</xsl:attribute>
            </target>

            <target name="-post-jar">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-do-jar">
                <xsl:attribute name="depends">init,compile,-pre-jar,-do-jar-without-libraries,-do-jar-with-libraries,-post-jar</xsl:attribute>
            </target>

            <target name="jar">
                <xsl:attribute name="depends">init,compile,-pre-jar,-do-jar,-post-jar</xsl:attribute>
                <xsl:attribute name="description">Build JAR.</xsl:attribute>
            </target>

            <xsl:comment>
                =================
                EXECUTION SECTION
                =================
            </xsl:comment>

            
            <xsl:comment>
                =================
                DEBUGGING SECTION
                =================
            </xsl:comment>

            <target name="-debug-start-debugger">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <j2meproject1:nbjpdastart name="${{debug.class}}"/>
            </target>
            

            <target name="-debug-start-debuggee">
                <xsl:attribute name="depends">init,compile</xsl:attribute>
                <j2meproject1:debug>
                    <customize>
                        <arg line="${{application.args}}"/>
                    </customize>
                </j2meproject1:debug>
            </target>

            <target name="debug">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-debug-start-debugger,-debug-start-debuggee</xsl:attribute>
                <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
            </target>

            <target name="-debug-start-debugger-stepinto">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <j2meproject1:nbjpdastart stopclassname="${{main.class}}"/>
            </target>

            <target name="debug-stepinto">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-debug-start-debugger-stepinto,-debug-start-debuggee</xsl:attribute>
            </target>            

            <target name="-pre-debug-fix">
                <xsl:attribute name="depends">init</xsl:attribute>
                <fail unless="fix.includes">Must set fix.includes</fail>
                <property name="javac.includes" value="${{fix.includes}}.java"/>
            </target>

            <target name="-do-debug-fix">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,compile-single</xsl:attribute>
                <j2meproject1:nbjpdareload/>
            </target>

            <target name="debug-fix">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,-do-debug-fix</xsl:attribute>
            </target>

            <xsl:comment>
                =================
                PROFILING SECTION
                =================
            </xsl:comment>

            <xsl:comment>
                ===============
                JAVADOC SECTION
                ===============
            </xsl:comment>

            <target name="-javadoc-build">
                <xsl:attribute name="depends">init</xsl:attribute>
                <xsl:attribute name="if">have.sources</xsl:attribute>
                <mkdir dir="${{dist.javadoc.dir}}"/>
                <condition property="javadoc.endorsed.classpath.cmd.line.arg" value="-J${{endorsed.classpath.cmd.line.arg}}" else="">
                    <and>
                        <isset property="endorsed.classpath.cmd.line.arg"/>
                        <not>
                            <equals arg1="${{endorsed.classpath.cmd.line.arg}}" arg2=""/>
                        </not>
                    </and>
                </condition>
                <!-- XXX do an up-to-date check first -->
                <javadoc>
                    <xsl:attribute name="destdir">${dist.javadoc.dir}</xsl:attribute>
                    <xsl:attribute name="source">${javac.source}</xsl:attribute>
                    <xsl:attribute name="notree">${javadoc.notree}</xsl:attribute>
                    <xsl:attribute name="use">${javadoc.use}</xsl:attribute>
                    <xsl:attribute name="nonavbar">${javadoc.nonavbar}</xsl:attribute>
                    <xsl:attribute name="noindex">${javadoc.noindex}</xsl:attribute>
                    <xsl:attribute name="splitindex">${javadoc.splitindex}</xsl:attribute>
                    <xsl:attribute name="author">${javadoc.author}</xsl:attribute>
                    <xsl:attribute name="version">${javadoc.version}</xsl:attribute>
                    <xsl:attribute name="windowtitle">${javadoc.windowtitle}</xsl:attribute>
                    <xsl:attribute name="private">${javadoc.private}</xsl:attribute>
                    <xsl:attribute name="additionalparam">${javadoc.additionalparam}</xsl:attribute>
                    <xsl:attribute name="failonerror">true</xsl:attribute> <!-- #47325 -->
                    <xsl:attribute name="useexternalfile">true</xsl:attribute> <!-- #57375, requires Ant >=1.6.5 -->
                    <xsl:attribute name="encoding">${javadoc.encoding.used}</xsl:attribute>
                    <xsl:attribute name="docencoding">UTF-8</xsl:attribute>
                    <xsl:attribute name="charset">UTF-8</xsl:attribute>
                    <xsl:attribute name="executable">${platform.javadoc}</xsl:attribute>
                    <classpath>
                        <path path="${{javac.classpath}}"/>
                    </classpath>                    
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2meproject1:data/j2meproject1:source-roots"/>
                        <xsl:with-param name="excludes" select="'*.java'"/>
                        <xsl:with-param name="includes2">**/*.java</xsl:with-param>
                    </xsl:call-template>
                    <fileset>
                        <xsl:attribute name="dir">${build.generated.sources.dir}</xsl:attribute>
                        <xsl:attribute name="erroronmissingdir">false</xsl:attribute>
                        <include name="**/*.java"/>
                        <exclude name="*.java"/>
                    </fileset>
                    <arg line="${{javadoc.endorsed.classpath.cmd.line.arg}}"/>
                </javadoc>
                <copy todir="${{dist.javadoc.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2meproject1:data/j2meproject1:source-roots"/>
                        <xsl:with-param name="includes2">**/doc-files/**</xsl:with-param>
                    </xsl:call-template>
                    <fileset>
                        <xsl:attribute name="dir">${build.generated.sources.dir}</xsl:attribute>
                        <xsl:attribute name="erroronmissingdir">false</xsl:attribute>
                        <include name="**/doc-files/**"/>
                    </fileset>
                </copy>
            </target>

            <target name="-javadoc-browse">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="unless">no.javadoc.preview</xsl:attribute>
                <xsl:attribute name="depends">init,-javadoc-build</xsl:attribute>
                <nbbrowse file="${{dist.javadoc.dir}}/index.html"/>
            </target>

            <target name="javadoc">
                <xsl:attribute name="depends">init,-javadoc-build,-javadoc-browse</xsl:attribute>
                <xsl:attribute name="description">Build Javadoc.</xsl:attribute>
            </target>

            <xsl:comment>
                ===============
                CLEANUP SECTION
                ===============
            </xsl:comment>

            <xsl:call-template name="deps.target">
                <xsl:with-param name="kind" select="'clean'"/>
            </xsl:call-template>

            <target name="-do-clean">
                <xsl:attribute name="depends">init</xsl:attribute>
                <delete dir="${{build.dir}}"/>
                <delete dir="${{dist.dir}}" followsymlinks="false" includeemptydirs="true"/> <!-- see issue 176851 -->
                <!-- XXX explicitly delete all build.* and dist.* dirs in case they are not subdirs -->
            </target>

            <target name="-post-clean">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="clean">
                <xsl:attribute name="depends">init,deps-clean,-do-clean,-post-clean</xsl:attribute>
                <xsl:attribute name="description">Clean build products.</xsl:attribute>
            </target>

            <target name="-check-call-dep">
                <property file="${{call.built.properties}}" prefix="already.built."/>
                <condition property="should.call.dep">
                    <and>
                        <not>
                            <isset property="already.built.${{call.subproject}}"/>
                        </not>
                        <available file="${{call.script}}"/>
                    </and>
                </condition>
            </target>
            <target name="-maybe-call-dep" depends="-check-call-dep" if="should.call.dep">
                <ant target="${{call.target}}" antfile="${{call.script}}" inheritall="false">
                    <propertyset>
                        <propertyref prefix="transfer."/>
                        <mapper type="glob" from="transfer.*" to="*"/>
                    </propertyset>
                </ant>
            </target>
        </project>
    </xsl:template>

    <!---
    Generic template to build subdependencies of a certain type.
    Feel free to copy into other modules.
    @param kind required end of name of target to generate
    @param type artifact-type from project.xml to filter on; optional, if not specified, uses
                all references, and looks for clean targets rather than build targets
    @return an Ant target which builds (or cleans) all known subprojects
    -->
    <xsl:template name="deps.target">
        <xsl:param name="kind"/>
        <xsl:param name="type"/>
        <target name="-deps-{$kind}-init" unless="built-{$kind}.properties">
            <property name="built-{$kind}.properties" location="${{build.dir}}/built-{$kind}.properties"/>
            <delete file="${{built-{$kind}.properties}}" quiet="true"/>
        </target>
        <target name="-warn-already-built-{$kind}" if="already.built.{$kind}.${{basedir}}">
            <echo level="warn" message="Cycle detected: {/p:project/p:configuration/j2meproject1:data/j2meproject1:name} was already built"/>
        </target>
        <target name="deps-{$kind}" depends="init,-deps-{$kind}-init">
            <xsl:attribute name="unless">no.deps</xsl:attribute>

            <mkdir dir="${{build.dir}}"/>
            <touch file="${{built-{$kind}.properties}}" verbose="false"/>
            <property file="${{built-{$kind}.properties}}" prefix="already.built.{$kind}."/>
            <antcall target="-warn-already-built-{$kind}"/>
            <propertyfile file="${{built-{$kind}.properties}}">
                <entry key="${{basedir}}" value=""/>
            </propertyfile>

            <xsl:variable name="references2" select="/p:project/p:configuration/projdeps2:references"/>
            <xsl:for-each select="$references2/projdeps2:reference[not($type) or projdeps2:artifact-type = $type]">
                <xsl:variable name="subproj" select="projdeps2:foreign-project"/>
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
                <xsl:choose>
                    <xsl:when test="projdeps2:properties">
                        <antcall target="-maybe-call-dep">
                            <param name="call.built.properties" value="${{built-{$kind}.properties}}"/>
                            <param name="call.subproject" location="${{project.{$subproj}}}"/>
                            <param name="call.script" location="{$script}"/>
                            <param name="call.target" value="{$subtarget}"/>
                            <param name="transfer.built-{$kind}.properties" value="${{built-{$kind}.properties}}"/>
                            <xsl:for-each select="projdeps2:properties/projdeps2:property">
                                <param name="transfer.{@name}" value="{.}"/>
                            </xsl:for-each>
                        </antcall>
                    </xsl:when>
                    <xsl:otherwise> <!-- XXX maybe just fold into former? projdeps2:properties/projdeps2:property select nothing? -->
                        <antcall target="-maybe-call-dep">
                            <param name="call.built.properties" value="${{built-{$kind}.properties}}"/>
                            <param name="call.subproject" location="${{project.{$subproj}}}"/>
                            <param name="call.script" location="{$script}"/>
                            <param name="call.target" value="{$subtarget}"/>
                            <param name="transfer.built-{$kind}.properties" value="${{built-{$kind}.properties}}"/>
                        </antcall>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>

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
                <antcall target="-maybe-call-dep">
                    <param name="call.built.properties" value="${{built-{$kind}.properties}}"/>
                    <param name="call.subproject" location="${{project.{$subproj}}}"/>
                    <param name="call.script" location="${{project.{$subproj}}}/{$script}"/>
                    <param name="call.target" value="{$subtarget}"/>
                    <param name="transfer.built-{$kind}.properties" value="${{built-{$kind}.properties}}"/>
                </antcall>
            </xsl:for-each>

        </target>
    </xsl:template>

    <xsl:template name="createRootAvailableTest">
        <xsl:param name="roots"/>
        <xsl:param name="propName"/>
        <xsl:element name="condition">
            <xsl:attribute name="property">
                <xsl:value-of select="$propName"/>
            </xsl:attribute>
            <or>
                <xsl:for-each select="$roots/j2meproject1:root">
                    <xsl:element name="available">
                        <xsl:attribute name="file">
                            <xsl:text>${</xsl:text>
                            <xsl:value-of select="@id"/>
                            <xsl:text>}</xsl:text>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:for-each>
            </or>
        </xsl:element>
    </xsl:template>

    <xsl:template name="createSourcePathValidityTest">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/j2meproject1:root">
            <xsl:element name="fail">
                <xsl:attribute name="unless">
                    <xsl:value-of select="@id"/>
                </xsl:attribute>
                <xsl:text>Must set </xsl:text>
                <xsl:value-of select="@id"/>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="createFilesets">
        <xsl:param name="roots"/>
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="includes2"/>
        <xsl:param name="excludes"/>
        <xsl:param name="condition"/>
        <xsl:for-each select="$roots/j2meproject1:root">
            <xsl:element name="fileset">
                <xsl:attribute name="dir">
                    <xsl:text>${</xsl:text>
                    <xsl:value-of select="@id"/>
                    <xsl:text>}</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="includes">
                    <xsl:value-of select="$includes"/>
                </xsl:attribute>
                <xsl:choose>
                    <xsl:when test="$excludes">
                        <xsl:attribute name="excludes">
                            <xsl:value-of select="$excludes"/>,${excludes}</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="excludes">${excludes}</xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="$includes2">
                    <filename name="{$includes2}"/>
                    <xsl:copy-of select="$condition"/>
                </xsl:if>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="createPackagesets">
        <xsl:param name="roots"/>
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="excludes"/>
        <xsl:for-each select="$roots/j2meproject1:root">
            <xsl:element name="packageset">
                <xsl:attribute name="dir">
                    <xsl:text>${</xsl:text>
                    <xsl:value-of select="@id"/>
                    <xsl:text>}</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="includes">
                    <xsl:value-of select="$includes"/>
                </xsl:attribute>
                <xsl:choose>
                    <xsl:when test="$excludes">
                        <xsl:attribute name="excludes">
                            <xsl:value-of select="$excludes"/>,${excludes}</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="excludes">${excludes}</xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="createPathElements">
        <xsl:param name="locations"/>
        <xsl:for-each select="$locations/j2meproject1:root">
            <xsl:element name="pathelement">
                <xsl:attribute name="location">
                    <xsl:text>${</xsl:text>
                    <xsl:value-of select="@id"/>
                    <xsl:text>}</xsl:text>
                </xsl:attribute>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="createPath">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/j2meproject1:root">
            <xsl:if test="position() != 1">
                <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:text>${</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>}</xsl:text>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
