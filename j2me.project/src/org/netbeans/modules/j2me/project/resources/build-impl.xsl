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
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4" cdata-section-elements="script"/>
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

            <target name="-check-platform-home" depends="-pre-init,-init-private">
                <condition property="has.platform.home">
                    <and>
                        <isset property="platform.home"/>
                        <length string="${{platform.home}}" when="gt" length="0" trim="true"/>
                        <available file="${{platform.home}}"/>
                    </and>
                </condition>
            </target>

            <target name="-init-platform-home" depends="-pre-init,-init-private,-check-platform-home" unless="has.platform.home">
                <loadproperties srcFile="nbproject/project.properties">
                  <filterchain>
                    <containsregex pattern="^platform.active="/>
                  </filterchain>
                </loadproperties>
                <loadproperties srcFile="${{user.properties.file}}">
                  <filterchain>
                    <containsregex pattern="^platforms\.${{platform.active}}\.home="/>
                    <replaceregex pattern="^platforms\.${{platform.active}}\." replace="platform."/>
                  </filterchain>
                </loadproperties>
                <echo message="Missing platform.home property, defined as ${{platform.home}}" level="warning"/>
            </target>

            <target name="-init-user">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-platform-home<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if></xsl:attribute>
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
                <j2meproject1:property name="platform.sdk.home.tmp" value="platforms.${{platform.sdk}}.home"/>
                <condition property="platform.sdk.home" value="${{jdk.home}}">
                    <equals arg1="${{platform.sdk.home.tmp}}" arg2="$${{platforms.${{platform.sdk}}.home}}"/>
                </condition>
                <property name="platform.sdk.home" value="${{platform.sdk.home.tmp}}"/>
                <j2meproject1:property name="platform.java.tmp" value="platforms.${{platform.sdk}}.java"/>
                <condition property="platform.java" value="${{platform.sdk.home}}/bin/java">
                    <equals arg1="${{platform.java.tmp}}" arg2="$${{platforms.${{platform.sdk}}.java}}"/>
                </condition>
                <property name="platform.java" value="${{platform.java.tmp}}"/>

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
                <fail unless="platform.javadoc">Must set platform.javadoc</fail>
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
                            <xsl:attribute name="bootclasspath">${platform.bootcp}</xsl:attribute>
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
                            <xsl:attribute name="bootclasspath">${platform.bootcp}</xsl:attribute>
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

            <target name="-init-presetdef-jar">
                <presetdef>
                    <xsl:attribute name="name">jar</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2me-embedded-project/1</xsl:attribute>
                    <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}" index="${{jar.index}}">
                        <j2meproject1:fileset dir="${{build.fatjar.dir}}" excludes="${{dist.archive.excludes}}"/>
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

            <target name="-init-javame">
                <xsl:attribute name="depends">-init-user,-init-project,-init-passwords</xsl:attribute>
                <fail unless="libs.j2me_common_ant.classpath">Classpath to Java ME Common Ant library (libs.j2me_common_ant.classpath) is not set. For example: location of javame/modules/org-netbeans-j2me-common-ant.jar file in the IDE installation directory.</fail>
                <taskdef resource="org/netbeans/modules/j2me/common/ant/defs.properties">
                    <classpath>
                        <pathelement path="${{libs.j2me_common_ant.classpath}}"/>
                    </classpath>
                </taskdef>
                <condition property="contains.manifest.configuration">
                    <matches pattern="MicroEdition-Configuration" string="${{manifest.others}}"/>
                </condition>
                <condition property="contains.manifest.profile">
                    <matches pattern="MicroEdition-Profile" string="${{manifest.others}}"/>
                </condition>
            </target>

            <target name="init">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-do-init,-post-init,-init-check,-init-macrodef-property,-init-macrodef-javac,-init-presetdef-jar,-init-ap-cmdline,-init-javame</xsl:attribute>
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

            <target name="-do-jar-jar">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar,-do-jar-create-manifest,-do-jar-copy-manifest,-do-jar-extract-libs</xsl:attribute>
                <xsl:attribute name="if">do.archive</xsl:attribute>
                <j2meproject1:jar manifest="${{tmp.manifest.file}}"/>
            </target>

            <target name="-do-jar-extract-libs">
                <xsl:attribute name="description">Extracts all bundled libraries.</xsl:attribute>
                <mkdir dir="${{build.fatjar.dir}}"/>
                <nb-extract dir="${{build.fatjar.dir}}" excludeManifest="true" classpath="${{javac.classpath}}" excludeclasspath="${{extra.classpath}}"/>
                <copy todir="${{build.fatjar.dir}}" >
                    <fileset dir="${{build.classes.dir}}"/>
                </copy>
            </target>

            <target name="-do-jar-copyliblets">
                <property location="${{build.classes.dir}}" name="build.classes.dir.resolved"/>
                <pathconvert property="run.classpath.without.build.classes.dir">
                    <path path="${{run.classpath}}"/>
                    <map from="${{build.classes.dir.resolved}}" to=""/>
                </pathconvert>
                <nb-copyliblets runtimeclasspath="${{run.classpath.without.build.classes.dir}}"/>
            </target>

            <target name="-do-jar-delete-manifest" >
                <xsl:attribute name="if">do.archive</xsl:attribute>
                <delete>
                    <fileset file="${{tmp.manifest.file}}"/>
                </delete>
            </target>

            <target name="-do-jar-with-libraries">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar,-do-jar-create-manifest,-do-jar-copy-manifest,-do-jar-jar,-do-jar-copyliblets,-do-jar-delete-manifest</xsl:attribute>
            </target>

            <target name="-post-jar">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-do-jar">
                <xsl:attribute name="depends">init,compile,-do-jar-update-manifest,-pre-jar,-do-jar-with-libraries,-post-jar</xsl:attribute>
            </target>

            <target name="jar">
                <xsl:attribute name="depends">init,compile,obfuscate,-pre-jar,-do-jar,-post-jar,create-jad</xsl:attribute>
                <xsl:attribute name="description">Build JAR.</xsl:attribute>
            </target>

            <target name="create-jad">
                <xsl:attribute name="description">Creates JAD file.</xsl:attribute>
                <fail unless="dist.jad">Must set dist.jad</fail>
                <echo file="${{dist.dir}}/${{dist.jad}}" message="${{manifest.others}}" encoding="UTF-8"/>
                <antcall inheritall="true" inheritrefs="true" target="-add-midlets"/>
                <antcall inheritall="true" inheritrefs="true" target="-add-optional-attributes"/>
                <antcall target="-add-configuration" inheritall="true" inheritrefs="true"/>
                <antcall target="-add-profile" inheritall="true" inheritrefs="true"/>
                <condition property="jad.jarurl" value="${{deployment.jarurl}}">
                    <istrue value="${{deployment.override.jarurl}}"/>
                </condition>
                <property name="jad.jarurl" value="${{dist.jar.file}}"/>
                <nb-jad jadfile="${{dist.dir}}/${{dist.jad}}" jarfile="${{dist.jar}}" url="${{jad.jarurl}}" sign="${{sign.enabled}}" keystore="${{sign.keystore}}" keystorepassword="${{sign.keystore.password}}" alias="${{sign.alias}}" aliaspassword="${{sign.alias.password}}" encoding="UTF-8"/>
                <antcall inheritall="true" inheritrefs="true" target="-add-liblets"/>
                <antcall inheritall="true" inheritrefs="true" target="-add-services"/>
            </target>

            <target name="-add-midlets">
                <xsl:attribute name="unless">manifest.is.liblet</xsl:attribute>
                <echo append="true" encoding="UTF-8" file="${{dist.dir}}/${{dist.jad}}" message="${{manifest.midlets}}"/>
            </target>

            <target name="-add-optional-attributes">
                <xsl:attribute name="depends">-add-apipermissions,-add-pushregistry,-add-jad-extra</xsl:attribute>
            </target>

            <target name="-add-apipermissions">
                <xsl:attribute name="if">manifest.apipermissions</xsl:attribute>
                <echo append="true" encoding="UTF-8" file="${{dist.dir}}/${{dist.jad}}" message="${{manifest.apipermissions}}"/>
                <echo append="true" encoding="UTF-8" file="${{dist.dir}}/${{dist.jad}}" message="${{manifest.apipermissions.classes}}"/>
            </target>

            <target name="-add-pushregistry">
                <xsl:attribute name="if">manifest.pushregistry</xsl:attribute>
                <echo append="true" encoding="UTF-8" file="${{dist.dir}}/${{dist.jad}}" message="${{manifest.pushregistry}}"/>
            </target>

            <target name="-add-jad-extra">
                <xsl:attribute name="if">manifest.jad</xsl:attribute>
                <echo append="true" encoding="UTF-8" file="${{dist.dir}}/${{dist.jad}}" message="${{manifest.jad}}"/>
            </target>

            <target name="-add-configuration" unless="contains.manifest.configuration">
                <echo file="${{dist.dir}}/${{dist.jad}}" message="MicroEdition-Configuration: ${{platform.configuration}}" append="true" encoding="UTF-8"/>
                <echo file="${{dist.dir}}/${{dist.jad}}" message="${{line.separator}}" append="true"/>
            </target>

            <target name="-add-profile" unless="contains.manifest.profile">
                <echo file="${{dist.dir}}/${{dist.jad}}" message="MicroEdition-Profile: ${{platform.profile}}" append="true" encoding="UTF-8"/>
                <echo file="${{dist.dir}}/${{dist.jad}}" message="${{line.separator}}" append="true"/>
            </target>

            <target name="-add-liblets">
                <script language="javascript"><![CDATA[
                    function isTrue(prop) {
                        return prop != null &&
                        (prop.toLowerCase() == "true" || prop.toLowerCase() == "yes" || prop.toLowerCase() == "on");
                    }
                    function addLibletProp(id, prop, val) {
                        var src = new String(project.getBaseDir().getAbsolutePath() +
                            "/" + new String(project.getProperty("dist.dir")) +
                            "/" + new String(project.getProperty("dist.jad")));
                        var srf = new java.io.File(src);
                        var echo = project.createTask("echo");
                        echo.setAppend(true);
                        echo.setEncoding("UTF-8");
                        echo.setFile(srf);
                        if(echo != null) {
                            echo.setMessage(prop + id + ": " + val + "\n");
                        }
                        echo.perform();
                    }

                    var liblet = project.getProperty("manifest.is.liblet");
                    var packaging = "MIDlet";
                    if (isTrue(liblet)) {
                        packaging = "LIBlet";
                    }
                    var libletId = 0;
                    var libletCount = 0;
                    while (true) {
                        var libletDep = project.getProperty("liblets." + libletId + ".dependency");
                        if (libletDep == null) {
                            break;
                        }
                        var extractLiblet = project.getProperty("liblets." + libletId + ".extract");
                        if (!isTrue(extractLiblet)) {
                            addLibletProp(libletCount + 1, packaging + "-Dependency-", libletDep);

                            var libletUrl = project.getProperty("liblets." + libletId + ".url");
                            if (libletUrl != null && libletUrl.length() > 0) {
                                addLibletProp(libletCount + 1, packaging + "-Dependency-JAD-URL-", libletUrl);
                            }
                            libletCount++;
                        }
                        libletId++;
                    }
                    ]]></script>
            </target>

            <target name="-add-services">
                <xsl:attribute name="if">manifest.is.liblet</xsl:attribute>
                <script language="javascript"><![CDATA[
                    function isTrue(prop) {
                        return prop != null &&
                        (prop.toLowerCase() == "true" || prop.toLowerCase() == "yes" || prop.toLowerCase() == "on");
                    }
                    var liblet = project.getProperty("manifest.is.liblet");
                    if (isTrue(liblet)) {
                        var services = "";
                        var classesDir = project.getProperty("build.classes.dir");
                        var classesDirF = project.resolveFile(classesDir);
                        var servicesDirF = new java.io.File(classesDirF + java.io.File.separator + "META-INF" + java.io.File.separator + "services");
                        if (servicesDirF != null && servicesDirF.exists()) {
                            var servicesArray = servicesDirF.listFiles();
                            if (servicesArray != null && servicesArray.length > 0) {
                                for (var i = 0; i < servicesArray.length; i++) {
                                    services += servicesArray[i].getName();
                                    if (i < servicesArray.length - 1) {
                                        services += ",";
                                    }
                                }
                                var src = new String(project.getBaseDir().getAbsolutePath() +
                                    "/" + new String(project.getProperty("dist.dir")) +
                                    "/" + new String(project.getProperty("dist.jad")));
                                var srf = new java.io.File(src);
                                var echo = project.createTask("echo");
                                echo.setAppend(true);
                                echo.setEncoding("UTF-8");
                                echo.setFile(srf);
                                if (echo != null) {
                                    echo.setMessage("LIBlet-Services: " + services);
                                }
                                echo.perform();
                            }
                        }
                    }
                    ]]></script>
            </target>

            <target name="-do-jar-update-manifest" depends="-do-jar-create-manifest,-do-jar-copy-manifest">
                <manifest file="${{tmp.manifest.file}}" mode="update">
                    <attribute name="MicroEdition-Profile" value="${{platform.profile}}"/>
                    <attribute name="MicroEdition-Configuration" value="${{platform.configuration}}"/>
                </manifest>
                <script>
		<xsl:attribute name="language">javascript</xsl:attribute><![CDATA[
                function isTrue(prop) {
                    return prop != null &&
                    (prop.toLowerCase() == "true" || prop.toLowerCase() == "yes" || prop.toLowerCase() == "on");
                }
                function updateManifest(entries) {
                    var src = new String(project.getProperty("tmp.manifest.file"));
                    var srf = new java.io.File(src);
                    var manifest = project.createTask("manifest");
                    var mode = new org.apache.tools.ant.taskdefs.ManifestTask.Mode();
                    mode.setValue("update");
                    manifest.setMode(mode);
                    manifest.setFile(srf);
                    if(manifest != null) {
                        var propertyArray = entries.split("\n");
                        for (var i = 0; i < propertyArray.length; i++) {
                            if (propertyArray[i].indexOf(":") == -1) {
                                continue;
                            }
                            var colonCount = 0;
                            for (var j = 0; j < propertyArray[i].length; j++) {
                                if (propertyArray[i].charAt(j) == ':') {
                                    colonCount++;
                                }
                            }
                            splitted = propertyArray[i].split(":");
                            if (colonCount > 1) {
                                var colonIndex = propertyArray[i].indexOf(":");
                                splitted[0] = propertyArray[i].substring(0, colonIndex);
                                splitted[1] = propertyArray[i].substring(colonIndex + 1);
                            }
                            var propertyAttr = new org.apache.tools.ant.taskdefs.Manifest.Attribute();
                            propertyAttr.setName(splitted[0].trim());
                            propertyAttr.setValue(splitted[1].trim());
                            manifest.addConfiguredAttribute(propertyAttr);
                        }
                        manifest.perform();
                    }
                }
                if (!isTrue(project.getProperty("manifest.is.liblet"))) {
                    var midlets = new String(project.getProperty("manifest.midlets"));
                    updateManifest(midlets);
                }
                var others = new String(project.getProperty("manifest.others"));
                updateManifest(others);
                var apipermissions = new String(project.getProperty("manifest.apipermissions"));
                updateManifest(apipermissions);
                var apipermissionsClasses = new String(project.getProperty("manifest.apipermissions.classes"));
                updateManifest(apipermissionsClasses);
                var pushregistry = new String(project.getProperty("manifest.pushregistry"));
                updateManifest(pushregistry);
                var manifestExtra = new String(project.getProperty("manifest.manifest"));
                updateManifest(manifestExtra);

                function addLiblet(id, liblet) {
                    var packaging = "MIDlet";
                    var isLiblet = project.getProperty("manifest.is.liblet");
                    if (isTrue(isLiblet)) {
                            packaging = "LIBlet";
                    }
                    var src = new String(project.getProperty("tmp.manifest.file"));
                    var srf = new java.io.File(src);
                    var manifest = project.createTask("manifest");
                    var mode = new org.apache.tools.ant.taskdefs.ManifestTask.Mode();
                    mode.setValue("update");
                    manifest.setMode(mode);
                    manifest.setFile(srf);
                    if(manifest != null) {
                        var propertyAttr = new org.apache.tools.ant.taskdefs.Manifest.Attribute();
                        propertyAttr.setName(packaging + "-Dependency-" + id);
                        propertyAttr.setValue(liblet);
                        manifest.addConfiguredAttribute(propertyAttr);
                    }
                    manifest.perform();
                }

                var libletId = 0;
                var libletCount = 0;
                while (true) {
                    var libletDep = project.getProperty("liblets." + libletId + ".dependency");
                    if (libletDep == null) {
                        break;
                    }
                    var extractLiblet = project.getProperty("liblets." + libletId + ".extract");
                    if (!isTrue(extractLiblet)) {
                        addLiblet(libletCount + 1, libletDep);
                        libletCount++;
                    }
                    libletId++;
                }

                (function addServices() {
                    var isLiblet = project.getProperty("manifest.is.liblet");
                        if (isTrue(isLiblet)) {
                            var services = "";
                            var classesDir = project.getProperty("build.classes.dir");
                            var classesDirF = project.resolveFile(classesDir);
                            var servicesDirF = new java.io.File(classesDirF + java.io.File.separator + "META-INF" + java.io.File.separator + "services");
                            if (servicesDirF != null && servicesDirF.exists()) {
                                var servicesArray = servicesDirF.listFiles();
                                if (servicesArray != null && servicesArray.length > 0) {
                                    for (var i = 0; i < servicesArray.length; i++) {
                                        services += servicesArray[i].getName();
                                        if (i < servicesArray.length - 1) {
                                            services += ",";
                                        }
                                    }
                                    var src = new String(project.getProperty("tmp.manifest.file"));
                                    var srf = new java.io.File(src);
                                    var manifest = project.createTask("manifest");
                                    var mode = new org.apache.tools.ant.taskdefs.ManifestTask.Mode();
                                    mode.setValue("update");
                                    manifest.setMode(mode);
                                    manifest.setFile(srf);
                                    if(manifest != null) {
                                        var propertyAttr = new org.apache.tools.ant.taskdefs.Manifest.Attribute();
                                        propertyAttr.setName("LIBlet-Services");
                                        propertyAttr.setValue(services);
                                        manifest.addConfiguredAttribute(propertyAttr);
                                    }
                                    manifest.perform();
                                }
                            }
                        }
                    })();
                ]]></script>
            </target>

            <xsl:comment>
                =================
                SIGNING SECTION
                =================
            </xsl:comment>

            <target name="-set-password">
                <property name="sign.enabled" value="false"/>
                <condition property="skip-sign-keystore-password-input">
                    <or>
                        <isfalse value="${{sign.enabled}}"/>
                        <and>
                            <isset property="sign.keystore"/>
                            <isset property="sign.keystore.password"/>
                            <not>
                                <equals arg1="${{sign.keystore}}" arg2="" trim="true"/>
                            </not>
                            <not>
                                <equals arg1="${{sign.keystore.password}}" arg2="" trim="true"/>
                            </not>
                        </and>
                    </or>
                </condition>
                <condition property="skip-sign-alias-password-input">
                    <or>
                        <isfalse value="${{sign.enabled}}"/>
                        <and>
                            <isset property="sign.keystore"/>
                            <isset property="sign.alias"/>
                            <isset property="sign.alias.password"/>
                            <not>
                                <equals arg1="${{sign.keystore}}" arg2="" trim="true"/>
                            </not>
                            <not>
                                <equals arg1="${{sign.alias}}" arg2="" trim="true"/>
                            </not>
                            <not>
                                <equals arg1="${{sign.alias.password}}" arg2="" trim="true"/>
                            </not>
                        </and>
                    </or>
                </condition>
            </target>
            <target name="-set-keystore-password">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="unless">skip-sign-keystore-password-input</xsl:attribute>
                <nb-enter-password keystore="${{sign.keystore}}" passwordproperty="sign.keystore.password"/>
            </target>
            <target name="-set-alias-password">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="unless">skip-sign-alias-password-input</xsl:attribute>
                <nb-enter-password keystore="${{sign.keystore}}" keyalias="${{sign.alias}}" passwordproperty="sign.alias.password"/>
            </target>
            <target name="-init-passwords">
                <xsl:attribute name="depends">-set-password,-set-keystore-password,-set-alias-password</xsl:attribute>
            </target>

            <xsl:comment>
                =================
                OBFUSCATING SECTION
                =================
            </xsl:comment>

            <target name="-pre-obfuscate"/>

            <target name="-post-obfuscate"/>

            <target name="obfuscate">
                <xsl:attribute name="depends">compile,-pre-obfuscate,-do-obfuscate,-post-obfuscate</xsl:attribute>
                <xsl:attribute name="description">Obfuscate project classes.</xsl:attribute>
            </target>

            <target name="-init-obfuscate">
                <property name="obfuscation.level" value="0"/>
                <condition property="no.obfusc">
                    <equals arg1="${{obfuscation.level}}" arg2="0"/>
                </condition>
            </target>

            <target name="-do-obfuscate">
                <xsl:attribute name="depends">-init-obfuscate</xsl:attribute>
                <xsl:attribute name="unless">no.obfusc</xsl:attribute>
                <property name="obfuscator.classpath" value=""/>
                <property name="obfuscation.custom" value=""/>
                <property name="obfuscator.srcjar" value="${{build.dir}}/unobfuscated.jar"/>
                <property name="obfuscator.destjar" value="${{build.dir}}/obfuscated.jar"/>
                <jar jarfile="${{obfuscator.srcjar}}" basedir="${{build.classes.dir}}"/>
                <nb-obfuscate srcjar="${{obfuscator.srcjar}}" destjar="${{obfuscator.destjar}}" obfuscatorclasspath="${{obfuscator.classpath}}" classpath="${{platform.bootcp}}:${{javac.classpath}}" obfuscationLevel="${{obfuscation.level}}" extraScript="${{obfuscation.custom}}"/>
                <delete includeEmptyDirs="true">
                    <fileset dir="${{build.classes.dir}}" includes="**/*" defaultexcludes="no"/>
                </delete>
                <unjar src="${{obfuscator.destjar}}" dest="${{build.classes.dir}}"/>
                <delete dir="${{build.classes.dir}}/META-INF"/>
                <delete file="${{obfuscator.srcjar}}"/>
                <delete file="${{obfuscator.destjar}}"/>
            </target>

            <xsl:comment>
                =================
                EXECUTION SECTION
                =================
            </xsl:comment>

            <target name="run">
                <xsl:attribute name="depends">init,clean,jar</xsl:attribute>
                <nb-run jadfile="${{dist.dir}}/${{dist.jad}}" jarfile="{{dist.jar.file}}" jadurl="${{dist.jad.url}}" device="${{platform.device}}" platformhome="${{platform.home}}" platformtype="${{platform.type}}" execmethod="${{run.method}}" commandline="${{platform.runcommandline}}" classpath="${{platform.bootclasspath}}:${{dist.dir}}/${{dist.jar}}" cmdoptions="${{run.cmd.options}}"/>
            </target>

            <xsl:comment>
                =================
                DEBUGGING SECTION
                =================
            </xsl:comment>

            <target name="debug">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">-debug-javame</xsl:attribute>
                <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
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

            <target name="-debug-javame" depends="init,clean,jar">
                <property name="run.method.debug" value="STANDARD"/>
                <parallel>
                    <nb-run debug="true" debugsuspend="true" debugserver="true" debuggeraddressproperty="jpda.port" platformtype="${{platform.type}}" platformhome="${{platform.home}}" device="${{platform.device}}" jadfile="${{dist.dir}}/${{dist.jad}}" jadurl="${{dist.jad.url}}" jarfile="${{dist.jar.file}}" execmethod="${{run.method.debug}}" commandline="${{platform.debugcommandline}}" classpath="${{platform.bootclasspath}}:${{dist.dir}}/${{dist.jar}}" cmdoptions="${{run.cmd.options}}"/>
                    <sequential>
                        <waitfor maxwait="5" maxwaitunit="second">
                            <isset property="jpda.port"/>
                        </waitfor>
                        <antcall target="-nbdebug"/>
                    </sequential>
                </parallel>
            </target>

            <target name="-nbdebug" description="Start NetBeans debugger" if="netbeans.home">
                <nb-mobility-debug address="${{jpda.port}}" name="${{app.codename}}" timeout="${{debugger.timeout}}" period="500"/>
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
