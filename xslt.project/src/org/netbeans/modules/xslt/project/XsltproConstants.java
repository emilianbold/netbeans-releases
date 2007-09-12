/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.xslt.project;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface XsltproConstants {
    String XSLT_EXTENSION = "xsl"; // NOI18N
    String XSLT_EXTENSION2 = "xslt"; // NOI18N 
    String EMPTY_STRING = ""; // NOI18N
    String WHITESPACE=" "; // NOI18N
    String XSLTMAP_XML = "xsltmap.xml";// NOI18N
    String TRANSFORMMAP_XML = "transformmap.xml"; // NOI18N
    String XSLT_PROJECT_ICON = "org/netbeans/modules/xslt/project/resources/xsltProjectIcon.png"; // NOI18N
    String POPULATE_CATALOG="populate_catalog"; // NOI18N
    String VALIDATION_FLAG = "allow.build.with.error"; //NOI18N
    String SOURCES_TYPE_ICANPRO = "BIZPRO";
    
    
//    String NETBEANS_HOME = "netbeans.home"; //NOI18N
////    String WIZARD_BUNDLE = "org/netbeans/modules/xslt/project/prjwizard/Bundle"; // NOI18N
    String PROJECT_DIR = "projdir"; //NOI18N
    String NAME = "name"; //NOI18N
////    String SOURCE_ROOT = "sourceRoot"; //NOI18N
//
// TODO r
    String SET_AS_MAIN = "setAsMain"; //NOI18N
////    String J2EE_LEVEL = "j2eeLevel"; //NOI18N
//
//    String CONFIG_FILES_FOLDER = "configFilesFolder"; //NOI18N
//    String JAVA_ROOT = "javaRoot"; //NOI18N
//    String LIB_FOLDER = "libFolder"; //NOI18N
//    
//
//    String COMMAND_REDEPLOY = "redeploy";
//    String COMMAND_DEPLOY = "deploy";
    String ARTIFACT_TYPE_JAR = "jar";
//    String SOURCES_TYPE_JAVA = "java";
    String ARTIFACT_TYPE_EJB_WS="j2ee_archive";
//
//
//    // icanproProjec tProperties constants
//    // Special properties of the project
//    String EJB_PROJECT_NAME = "j2ee.icanpro.name";
//    String JAVA_PLATFORM = "platform.active";
//
//    // Properties stored in the PROJECT.PROPERTIES
//    /** root of external web module sources (full path), ".." if the sources are within project folder */
//    String DIST_JAR = "dist.jar";
//    String DEBUG_CLASSPATH = "debug.classpath";
//    String WSDL_CLASSPATH = "wsdl.classpath";
//
//    String JAR_NAME = "jar.name";
//    String JAR_COMPRESS = "jar.compress";
//
//    String JAVAC_SOURCE = "javac.source";
//    String JAVAC_DEBUG = "javac.debug";
//    String JAVAC_DEPRECATION = "javac.deprecation";
//    String JAVAC_TARGET = "javac.target";
//    String JAVAC_ARGS = "javac.compilerargs";
//    String META_INF = "meta.inf";
//    String RESOURCE_DIR = "resource.dir";
//    String BUILD_GENERATED_DIR = "build.generated.dir";
//    String BUILD_CLASSES_DIR = "build.classes.dir";
//    String BUILD_CLASSES_EXCLUDES = "build.classes.excludes";
//
//    String DIST_JAVADOC_DIR = "dist.javadoc.dir";
//
//    //================== Start of IcanPro =====================================//
//    //FIXME? REPACKAGING
//    String JBI_SETYPE_PREFIX = "com.sun.jbi.ui.devtool.jbi.setype.prefix";
//    String ASSEMBLY_UNIT_ALIAS = "com.sun.jbi.ui.devtool.jbi.alias.assembly-unit";
//    String ASSEMBLY_UNIT_DESCRIPTION = "com.sun.jbi.ui.devtool.jbi.description.assembly-unit";
//    String APPLICATION_SUB_ASSEMBLY_ALIAS = "com.sun.jbi.ui.devtool.jbi.alias.application-sub-assembly";
//    String APPLICATION_SUB_ASSEMBLY_DESCRIPTION = "com.sun.jbi.ui.devtool.jbi.description.application-sub-assembly";
//
//    String JBI_COMPONENT_CONF_FILE = "com.sun.jbi.ui.devtool.jbi.component.conf.file";
//    String JBI_COMPONENT_CONF_ROOT = "com.sun.jbi.ui.devtool.jbi.component.conf.root";
//    String JBI_DEPLOYMENT_CONF_FILE = "com.sun.jbi.ui.devtool.jbi.deployment.conf.file";
//    String JBI_DEPLOYMENT_CONF_ROOT = "com.sun.jbi.ui.devtool.jbi.deployment.conf.root";
//    String DISPLAY_NAME_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.displayName";
//    String HOST_NAME_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.hostName";
//    String ADMINISTRATION_PORT_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.administrationPort";
//    String DOMAIN_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.domain";
//    String HTTP_MONITOR_ON_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.httpMonitorOn";
//    String HTTP_PORT_NUMBER_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.httpPortNumber";
//    String LOCATION_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.location";
//    String PASSWORD_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.password";
//    String URL_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.url";
//    String USER_NAME_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.userName";
//
//    String JBI_REGISTRY_COMPONENT_FILE_KEY = "com.sun.jbi.registry.component.file";
//    String JBI_REGISTRY_BROKER_HOST_KEY = "com.sun.jbi.messaging.brokerHost";
//
//    String BC_DEPLOYMENT_JAR = "bcdeployment.jar";
//    String SE_DEPLOYMENT_JAR = "sedeployment.jar";    
    
}


