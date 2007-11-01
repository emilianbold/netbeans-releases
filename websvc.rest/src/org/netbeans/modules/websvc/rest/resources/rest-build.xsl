<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:webproject1="http://www.netbeans.org/ns/web-project/1"
                xmlns:webproject2="http://www.netbeans.org/ns/web-project/2"
                xmlns:webproject3="http://www.netbeans.org/ns/web-project/3"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                xmlns:jaxrs="http://www.netbeans.org/ns/jax-rs/1"
                exclude-result-prefixes="xalan p projdeps">
                    
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>

    <xsl:template match="/">
        <![CDATA[        ]]>
        <xsl:comment><![CDATA[
        *** GENERATED - DO NOT EDIT  ***
        ]]></xsl:comment>
        
        <xsl:variable name="name" select="/p:project/p:configuration/webproject3:data/webproject3:name"/>
        <!-- Synch with build-impl.xsl: -->
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-rest-build">
            <xsl:attribute name="basedir">..</xsl:attribute>
            
            <target name="-init-rest" if="rest.support.on">
                <condition property="platform.restlib.classpath" value="${{j2ee.platform.classpath}}">
                    <and>
                        <isset property="restlib.ignore.platform"/>
                        <isfalse value="${{restlib.ignore.platform}}"/>
                    </and>
                </condition>
                <condition property="restapt.redirect" value="false" else="true">
                    <and>
                        <isset property="rest.apt.redirect"/>
                        <isfalse value="${{rest.apt.redirect}}"/>
                    </and>
                </condition>
                <condition property="restapt.normalizeURI" value="false" else="true">
                    <and>
                        <isset property="rest.apt.normalizeURI"/>
                        <isfalse value="${{rest.apt.normalizeURI}}"/>
                    </and>
                </condition>
                <condition property="restapt.canonicalizeURIPath" value="false" else="true">
                    <and>
                        <isset property="rest.apt.canonicalizeURIPath"/>
                        <isfalse value="${{rest.apt.canonicalizeURIPath}}"/>
                    </and>
                </condition>
                <condition property="restapt.ignoreMatrixParams" value="false" else="true">
                    <and>
                        <isset property="rest.apt.ignoreMatrixParams"/>
                        <isfalse value="${{rest.apt.ignoreMatrixParams}}"/>
                    </and>
                </condition>
                <taskdef name="restapt" classname="com.sun.ws.rest.tools.ant.WebResourcesProcessorTask">
                    <classpath>
                        <path path="${{platform.restlib.classpath}}"/>
                        <path path="${{libs.restlib.classpath}}"/>
                    </classpath>
                </taskdef>
            </target>
    
            <target name="-rest-post-compile" depends="-init-rest" if="rest.support.on">
                <mkdir dir="${{build.generated.dir}}/rest-gen"/>
                <restapt fork="true" xEndorsed="true" sourcePath="${{src.dir}}" nocompile="true"
                         destdir="${{build.generated.dir}}/rest-gen" 
                         sourcedestdir="${{build.generated.dir}}/rest-gen">
                    <classpath>
                        <path path="${{javac.classpath}}"/>
                        <path path="${{libs.jaxws21.classpath}}"/>
                        <path path="${{j2ee.platform.classpath}}"/>
                        <pathelement location="${{build.web.dir}}/WEB-INF/classes"/>
                    </classpath>
                    <source dir="${{src.dir}}">
                        <include name="**/*.java"/>
                    </source>
                    <option key="redirect" value="${{restapt.redirect}}"/>
                    <option key="normalizeURI" value="${{restapt.normalizeURI}}"/>
                    <option key="canonicalizeURIPath" value="${{restapt.canonicalizeURIPath}}"/>
                    <option key="ignoreMatrixParams" value="${{restapt.ignoreMatrixParams}}"/>
                </restapt>
                <webproject2:javac srcdir="${{build.generated.dir}}/rest-gen" destdir="${{build.classes.dir}}"/>
                <copy todir="${{build.classes.dir}}">
                    <fileset dir="${{build.generated.dir}}/rest-gen" includes="**/*.wadl"/>
                </copy>
            </target>
            
            <target name="test-restbeans" depends="run-deploy,-init-display-browser">
                <replace file="${{restbeans.test.file}}" token="${{base.url.token}}" value="${{client.url}}"/>
                <nbbrowse url="${{restbeans.test.url}}"/>
            </target>
        </project>
            
    </xsl:template>
    
</xsl:stylesheet>
