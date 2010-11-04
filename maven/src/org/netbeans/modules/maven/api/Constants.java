/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.api;

/**
 * Various constants used across the integration, Maven property names with a meaning in the IDE,
 * plugin groupIds, artifactIds etc.
 * @author mkleint
 */
public interface Constants {
    
    /**
     * Maven property that hints netbeans to use a given license template.
     */ 
    public static final String HINT_LICENSE = "netbeans.hint.license"; //NOI18N

    /**
     * Maven property that designates the jdk platform to use in the IDE on classpath for project.
     * Equivalent to the "platform.active" property in Ant based projects.
     * Workaround for issue http://www.netbeans.org/issues/show_bug.cgi?id=104974
     * Will only influence the classpath in the IDE, not the maven build itself.
     */
    public static final String HINT_JDK_PLATFORM = "netbeans.hint.jdkPlatform"; //NOI18N

    
    /**
     * Maven property that hints netbeans to handle the project as if it were of given packaging..
     * Influences the available default action mappings, panels in customizers and other UI functionality in the IDE.
     * Useful for cases when you define a custom packaging eg "jar2" but want the ide to handle it as j2se/jar project.
     * Meaningful values include: jar,war,ejb,ear,nbm
     */ 
    public static final String HINT_PACKAGING = "netbeans.hint.packaging"; //NOI18N
    
    /**
     * apache maven default groupid for maven plugins. 
     */ 
    public static final String GROUP_APACHE_PLUGINS = "org.apache.maven.plugins"; //NOI18N
    
    public static final String PLUGIN_COMPILER = "maven-compiler-plugin";//NOI18N
    public static final String PLUGIN_WAR = "maven-war-plugin";//NOI18N
    public static final String PLUGIN_SITE = "maven-site-plugin";//NOI18N
    public static final String PLUGIN_RESOURCES = "maven-resources-plugin";//NOI18N
    public static final String PLUGIN_EJB = "maven-ejb-plugin";//NOI18N
    public static final String PLUGIN_EAR = "maven-ear-plugin";//NOI18N
    public static final String PLUGIN_JAR = "maven-jar-plugin";//NOI18N
    public static final String PLUGIN_SUREFIRE = "maven-surefire-plugin";//NOI18N
    public static final String PLUGIN_CHECKSTYLE = "maven-checkstyle-plugin";//NOI18N
    
    public static final String ENCODING_PARAM = "encoding"; //NOI18N
    public static final String SOURCE_PARAM = "source";//NOI18N
    public static final String TARGET_PARAM = "target";//NOI18N

    /**
     *
     * this property was introduced as part of this proposal:
     * http://docs.codehaus.org/display/MAVENUSER/POM+Element+for+Source+File+Encoding
     */
    public static String ENCODING_PROP = "project.build.sourceEncoding"; //NOI18N


    /**
     * When used as a property when executing maven, it will start a debugger before invoking a project related action.
     * will replace the ${jpda.address} expression in action's properties with the correct value of
     * localhost port number.
     * allowed values:
     * <ul>
     * <li>
     * true - starts the debugger and waits for the process to attach to it.
     * </li>
     * <li>
     * maven - starts the debugger and generates correct MAVEN_OPTS value that is passed to the command line maven executable.
     * MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=n,address=${jpda.address}
     * </li>
     * </ul>
     */
    public static final String ACTION_PROPERTY_JPDALISTEN = "jpda.listen";

    /**
     * Optional property, if defined the project type will attempt to redirect meaningful
     * run/debug/profile/test action invokations to the compile on save infrastructure.
     * Possible values
     * <ul>
     * <li>all  - both tests and application gets run by netbeans quick run infrastructure</li>
     * <li>test  - only tests are run by netbeans quick run infrastructure, not application - default value</li>
     * <li>app  - only application is run by netbeans quick run infrastructure, not tests</li>
     * <li>none - no compile on save
     * </ul>
     * @since NetBeans 6.7
     */
    public static final String HINT_COMPILE_ON_SAVE = "netbeans.compile.on.save"; //NOI18N

    /**
     * Optional property, if defined the IDE will try to use the project's checkstyle configuration
     * to transparently change the java files formatting (according to the setup rules)
     * @since NetBeans 6.8
     */
    public static final String HINT_CHECKSTYLE_FORMATTING = "netbeans.checkstyle.format"; //NOI18N

}
