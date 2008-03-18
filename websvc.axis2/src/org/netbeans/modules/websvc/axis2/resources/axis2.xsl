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
                xmlns:j2seproject3="http://www.netbeans.org/ns/j2se-project/3"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:axis2="http://www.netbeans.org/ns/axis2/1"> 
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
        
        <project>

            
            <xsl:comment>
                ===================
                JAX-WS WSIMPORT SECTION
                ===================
            </xsl:comment>
            
            <!-- java2wsdl task initialization -->
            <xsl:if test="/axis2:axis2/axis2:service/axis2:generate-wsdl">
                <target name="java2wsdl-init" depends="init">
                    <mkdir dir="${{basedir}}/xml-resources/axis2/META-INF"/>
                    <taskdef name="java2wsdl" classname="org.apache.ws.java2wsdl.Java2WSDLTask">
                         <classpath path="${{libs.axis2.classpath}}"/>
                    </taskdef>
                </target>
            </xsl:if>
            
            <!-- java2wsdl targets - one for each axis2:service -->
            <xsl:for-each select="/axis2:axis2/axis2:service">
                <xsl:if test="axis2:generate-wsdl">
                    <xsl:variable name="wsname" select="@name"/>               
                    <xsl:variable name="service_class" select="axis2:service-class"/>
                    <xsl:variable name="target_namespace" select="axis2:generate-wsdl/@targetNamespace"/>
                    <xsl:variable name="schema_namespace" select="axis2:generate-wsdl/@schemaNamespace"/>
                    
                    <target name="java2wsdl-check-{$wsname}" depends="java2wsdl-init">
                        <condition property="java2wsdl-check-{$wsname}.notRequired">
                            <available file="${{basedir}}/xml-resources/axis2/META-INF/{$wsname}.wsdl" type="file"/>
                        </condition>
                    </target>                    
                    <target name="java2wsdl-{$wsname}" depends="java2wsdl-check-{$wsname}, compile" unless="java2wsdl-check-{$wsname}.notRequired" >
                        <java2wsdl
                            className="{$service_class}"
                            serviceName="{$wsname}"
                            outputLocation="${{basedir}}/xml-resources/axis2/META-INF"
                            outputFileName="{$wsname}.wsdl"
                            targetNamespace="{$target_namespace}"
                            schemaTargetNamespace="{$schema_namespace}">
                                <classpath>
                                    <pathelement location="${{build.dir}}/classes"/>
                                </classpath>
                        </java2wsdl>
                    </target>
                    <target name="java2wsdl-clean-{$wsname}" depends="init" >
                        <delete file="${{basedir}}/xml-resources/axis2/META-INF/{$wsname}.wsdl"/>
                    </target>
                </xsl:if>
                <xsl:if test="axis2:wsdl-url">
                    <xsl:variable name="wsname" select="@name"/>
                    <xsl:variable name="wsdlUrl" select="axis2:wsdl-url"/>
                    <xsl:variable name="service_class" select="axis2:service-class"/>
                    <xsl:variable name="serviceName" select="axis2:java-generator/@serviceName"/>
                    <xsl:variable name="portName" select="axis2:java-generator/@portName"/>
                    <xsl:variable name="packageName" select="axis2:java-generator/@packageName"/>
                    <xsl:variable name="databindingName" select="axis2:java-generator/@databindingName"/>
                    <xsl:variable name="wsdlToJavaOptions" select="axis2:java-generator/@options"/>
                  
                    <target name="wsdl2java-{$wsname}" depends="init">
                        <delete dir="${{build.dir}}/axis2"/>
                        <java classname="org.apache.axis2.wsdl.WSDL2Java" fork="true">
                            <arg line="-uri {$wsdlUrl}"/>
                            <arg line="{$wsdlToJavaOptions}"/> 
                            <xsl:if test="axis2:java-generator/@sei"><arg line="-ssi"/></xsl:if>
                            <xsl:if test="axis2:java-generator/@databindingName = 'jibx'"><arg line="-uw"/></xsl:if>
                            <arg line="-sn {$serviceName}"/>
                            <arg line="-pn {$portName}"/>
                            <arg line="-p {$packageName}"/>
                            <arg line="-d {$databindingName}"/>
                            <arg line="-o ${{build.dir}}/axis2"/>
                            <classpath path="${{libs.axis2.classpath}}"/>
                        </java>
                        <copy toDir="${{src.dir}}" overwrite="true">
                            <fileset dir="${{build.dir}}/axis2/src">
                                <include name="**/*.java"/>
                            </fileset>
                        </copy>
                        <mkdir dir="${{basedir}}/xml-resources/axis2/META-INF"/>
                        <copy toDir="${{basedir}}/xml-resources/axis2/META-INF/" overwrite="true">
                            <fileset dir="${{build.dir}}/axis2/resources">
                                <include name="**/*.wsdl"/>
                                <include name="**/*.xsd"/>
                            </fileset>
                        </copy>
                    </target>
                    <target name="wsdl2java-refresh-{$wsname}" depends="init">
                        <delete dir="${{build.dir}}/axis2"/>
                        <java classname="org.apache.axis2.wsdl.WSDL2Java" fork="true">
                            <arg line="-uri ${{basedir}}/xml-resources/axis2/META-INF/{$serviceName}.wsdl"/>
                            <arg line="{$wsdlToJavaOptions}"/>
                            <xsl:if test="axis2:java-generator/@sei"><arg line="-ssi"/></xsl:if>
                            <xsl:if test="axis2:java-generator/@databindingName = 'jibx'"><arg line="-uw"/></xsl:if>
                            <arg line="-sn {$serviceName}"/>
                            <arg line="-pn {$portName}"/>
                            <arg line="-p {$packageName}"/>
                            <arg line="-d {$databindingName}"/>
                            <arg line="-o ${{build.dir}}/axis2"/>
                            <classpath path="${{libs.axis2.classpath}}"/>
                        </java>
                        <copy toDir="${{src.dir}}" overwrite="true">
                            <fileset dir="${{build.dir}}/axis2/src">
                                <include name="**/*.java"/>
                            </fileset>
                        </copy>
                    </target>
                    <target name="wsdl2java-clean-{$wsname}" depends="init" >
                        <delete file="${{basedir}}/xml-resources/axis2/META-INF/{$wsname}.wsdl"/>
                    </target>
                </xsl:if>
            </xsl:for-each>
            
            <!-- generate aar -->
            <xsl:if test="/axis2:axis2/axis2:service">
                <xsl:variable name="wsname" select="/axis2:axis2/axis2:service/@name"/>
                <target name="axis2-aar">
                    <xsl:attribute name="depends">
                        <xsl:text>compile</xsl:text>
                        <xsl:for-each select="/axis2:axis2/axis2:service/axis2:generate-wsdl">
                            <xsl:text>, java2wsdl-</xsl:text><xsl:value-of select="../@name"/>
                        </xsl:for-each>
                    </xsl:attribute>
                    <mkdir dir = "${{build.dir}}/axis2/WEB-INF/services"/>
                    <xsl:if test="/axis2:axis2/axis2:libraries">
                        <mkdir dir = "${{basedir}}/xml-resources/axis2/lib"/>
                        <copy todir="${{basedir}}/xml-resources/axis2/lib" flatten="true" overwrite="false">
                            <resources>
                                <xsl:for-each select="/axis2:axis2/axis2:libraries/axis2:library-ref">
                                    <xsl:variable name="file-name" select="@name"/>
                                    <file file="${{file.reference.{$file-name}}}"/>
                                </xsl:for-each>
                            </resources>
                        </copy>
                    </xsl:if>
                    <jar destfile="${{build.dir}}/axis2/WEB-INF/services/{$wsname}.aar">
                        <fileset excludes="**/Test.class" dir="${{build.dir}}/classes"/>
                        <fileset dir="${{basedir}}/xml-resources/axis2">
                            <include name="**/*.wsdl"/>
                            <include name="**/*.xsd"/>
                            <include name="**/*.xml"/>
                            <include name="**/*.jar"/>
                        </fileset>
                    </jar>
                    <xsl:if test="/axis2:axis2/axis2:libraries">
                        <delete dir = "${{basedir}}/xml-resources/axis2/lib"/>
                    </xsl:if>
                </target>
                <target name="axis2-deploy-dir-check" depends="axis2-aar">
                    <condition property="axis2-deploy-dir-required">
                        <isset property="axis2.deploy.dir"/>
                    </condition>
                </target>
                <target name="axis2-deploy-dir" depends="axis2-deploy-dir-check" if="axis2-deploy-dir-required">
                    <copy toDir="${{axis2.deploy.dir}}/WEB-INF/services">
                        <fileset dir="${{build.dir}}/axis2/WEB-INF/services">
                            <include name="*.aar"/>
                        </fileset>
                    </copy>
                </target>
                <target name="axis2-deploy-war-check" depends="axis2-aar">
                    <condition property="axis2-deploy-war-required">
                        <isset property="axis2.deploy.war"/>
                    </condition>
                </target>
                <target name="axis2-deploy-war" depends="axis2-deploy-war-check" if="axis2-deploy-war-required">
                    <jar destfile="${{axis2.deploy.war}}" update="true">
                        <fileset dir="${{build.dir}}/axis2">
                            <include name="**/*.aar"/>
                        </fileset>
                    </jar>
                </target>
                <target name="axis2-deploy" depends="axis2-deploy-dir, axis2-deploy-war"/>
            </xsl:if>
            
        </project>
        
    </xsl:template>

</xsl:stylesheet>
