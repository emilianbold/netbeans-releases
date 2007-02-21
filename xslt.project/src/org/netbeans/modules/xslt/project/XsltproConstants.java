/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.project;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface XsltproConstants {
    String WHITESPACE=" "; // NOI18N
    String XSLTMAP_XML = "xsltmap.xml"; // NOI18N
    String XSLT_PROJECT_ICON = "org/netbeans/modules/xslt/project/resources/xsltProjectIcon.png"; // NOI18N"
    
    
    String NETBEANS_HOME = "netbeans.home"; //NOI18N
//    String WIZARD_BUNDLE = "org/netbeans/modules/xslt/project/prjwizard/Bundle"; // NOI18N
    String PROJECT_DIR = "projdir"; //NOI18N
    String NAME = "name"; //NOI18N
//    String SOURCE_ROOT = "sourceRoot"; //NOI18N

    String SET_AS_MAIN = "setAsMain"; //NOI18N
    String J2EE_LEVEL = "j2eeLevel"; //NOI18N

    String CONFIG_FILES_FOLDER = "configFilesFolder"; //NOI18N
    String JAVA_ROOT = "javaRoot"; //NOI18N
    String LIB_FOLDER = "libFolder"; //NOI18N
    

    String COMMAND_REDEPLOY = "redeploy";
    String COMMAND_DEPLOY = "deploy";
    String ARTIFACT_TYPE_JAR = "jar";
    String SOURCES_TYPE_JAVA = "java";
    String ARTIFACT_TYPE_EJB_WS="j2ee_archive";
    String POPULATE_CATALOG="populate_catalog";

    String SOURCES_TYPE_ICANPRO = "BIZPRO";

    
    
    
    
    
    // icanproProjec tProperties constants
    String J2EE_1_4 = "1.4";
    String J2EE_1_3 = "1.3";
    // Special properties of the project
    String EJB_PROJECT_NAME = "j2ee.icanpro.name";
    String JAVA_PLATFORM = "platform.active";
    String J2EE_PLATFORM = "j2ee.platform";

    // Properties stored in the PROJECT.PROPERTIES
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    String SOURCE_ROOT = "source.root";
    String BUILD_FILE = "buildfile";
    String DIST_DIR = "dist.dir";
    String DIST_JAR = "dist.jar";
    String JAVAC_CLASSPATH = "javac.classpath";
    String DEBUG_CLASSPATH = "debug.classpath";
    String WSDL_CLASSPATH = "wsdl.classpath";

    String JAR_NAME = "jar.name";
    String JAR_COMPRESS = "jar.compress";

    String J2EE_SERVER_INSTANCE = "j2ee.server.instance";
    String J2EE_SERVER_TYPE = "j2ee.server.type";
    String JAVAC_SOURCE = "javac.source";
    String JAVAC_DEBUG = "javac.debug";
    String JAVAC_DEPRECATION = "javac.deprecation";
    String JAVAC_TARGET = "javac.target";
    String JAVAC_ARGS = "javac.compilerargs";
    String VALIDATION_FLAG = "allow.build.with.error";
    String SRC_DIR = "src.dir";
    String META_INF = "meta.inf";
    String RESOURCE_DIR = "resource.dir";
    String BUILD_DIR = "build.dir";
    String BUILD_GENERATED_DIR = "build.generated.dir";
    String BUILD_CLASSES_DIR = "build.classes.dir";
    String BUILD_CLASSES_EXCLUDES = "build.classes.excludes";

    String DIST_JAVADOC_DIR = "dist.javadoc.dir";

    //================== Start of IcanPro =====================================//
    //FIXME? REPACKAGING
    String JBI_SETYPE_PREFIX = "com.sun.jbi.ui.devtool.jbi.setype.prefix";
    String ASSEMBLY_UNIT_ALIAS = "com.sun.jbi.ui.devtool.jbi.alias.assembly-unit";
    String ASSEMBLY_UNIT_DESCRIPTION = "com.sun.jbi.ui.devtool.jbi.description.assembly-unit";
    String APPLICATION_SUB_ASSEMBLY_ALIAS = "com.sun.jbi.ui.devtool.jbi.alias.application-sub-assembly";
    String APPLICATION_SUB_ASSEMBLY_DESCRIPTION = "com.sun.jbi.ui.devtool.jbi.description.application-sub-assembly";

    String JBI_COMPONENT_CONF_FILE = "com.sun.jbi.ui.devtool.jbi.component.conf.file";
    String JBI_COMPONENT_CONF_ROOT = "com.sun.jbi.ui.devtool.jbi.component.conf.root";
    String JBI_DEPLOYMENT_CONF_FILE = "com.sun.jbi.ui.devtool.jbi.deployment.conf.file";
    String JBI_DEPLOYMENT_CONF_ROOT = "com.sun.jbi.ui.devtool.jbi.deployment.conf.root";
    String DISPLAY_NAME_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.displayName";
    String HOST_NAME_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.hostName";
    String ADMINISTRATION_PORT_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.administrationPort";
    String DOMAIN_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.domain";
    String HTTP_MONITOR_ON_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.httpMonitorOn";
    String HTTP_PORT_NUMBER_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.httpPortNumber";
    String LOCATION_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.location";
    String PASSWORD_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.password";
    String URL_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.url";
    String USER_NAME_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.userName";

    String JBI_REGISTRY_COMPONENT_FILE_KEY = "com.sun.jbi.registry.component.file";
    String JBI_REGISTRY_BROKER_HOST_KEY = "com.sun.jbi.messaging.brokerHost";

    String BC_DEPLOYMENT_JAR = "bcdeployment.jar";
    String SE_DEPLOYMENT_JAR = "sedeployment.jar";    
    
}


