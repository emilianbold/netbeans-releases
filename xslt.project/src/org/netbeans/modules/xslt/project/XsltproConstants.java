/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.project;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface XsltproConstants {
//    String WIZARD_BUNDLE = "org/netbeans/modules/xslt/project/prjwizard/Bundle"; // NOI18N
    public static final String PROJECT_DIR = "projdir"; //NOI18N
    public static final String NAME = "name"; //NOI18N
//    public static final String SOURCE_ROOT = "sourceRoot"; //NOI18N

    public static final String SET_AS_MAIN = "setAsMain"; //NOI18N
    public static final String J2EE_LEVEL = "j2eeLevel"; //NOI18N

    public static final String CONFIG_FILES_FOLDER = "configFilesFolder"; //NOI18N
    public static final String JAVA_ROOT = "javaRoot"; //NOI18N
    public static final String LIB_FOLDER = "libFolder"; //NOI18N
    

    public static final String COMMAND_REDEPLOY = "redeploy";
    public static final String COMMAND_DEPLOY = "deploy";
    public static final String ARTIFACT_TYPE_JAR = "jar";
    public static final String SOURCES_TYPE_JAVA = "java";
    public static final String ARTIFACT_TYPE_EJB_WS="j2ee_archive";
    public static final String POPULATE_CATALOG="populate_catalog";

    public static final String SOURCES_TYPE_ICANPRO = "BIZPRO";

    
    
    
    
    
    // icanproProjec tProperties constants
    public static final String J2EE_1_4 = "1.4";
    public static final String J2EE_1_3 = "1.3";
    // Special properties of the project
    public static final String EJB_PROJECT_NAME = "j2ee.icanpro.name";
    public static final String JAVA_PLATFORM = "platform.active";
    public static final String J2EE_PLATFORM = "j2ee.platform";

    // Properties stored in the PROJECT.PROPERTIES
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    public static final String SOURCE_ROOT = "source.root";
    public static final String BUILD_FILE = "buildfile";
    public static final String DIST_DIR = "dist.dir";
    public static final String DIST_JAR = "dist.jar";
    public static final String JAVAC_CLASSPATH = "javac.classpath";
    public static final String DEBUG_CLASSPATH = "debug.classpath";
    public static final String WSDL_CLASSPATH = "wsdl.classpath";

    public static final String JAR_NAME = "jar.name";
    public static final String JAR_COMPRESS = "jar.compress";

    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance";
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type";
    public static final String JAVAC_SOURCE = "javac.source";
    public static final String JAVAC_DEBUG = "javac.debug";
    public static final String JAVAC_DEPRECATION = "javac.deprecation";
    public static final String JAVAC_TARGET = "javac.target";
    public static final String JAVAC_ARGS = "javac.compilerargs";
    public static final String VALIDATION_FLAG = "allow.build.with.error";
    public static final String SRC_DIR = "src.dir";
    public static final String META_INF = "meta.inf";
    public static final String RESOURCE_DIR = "resource.dir";
    public static final String BUILD_DIR = "build.dir";
    public static final String BUILD_GENERATED_DIR = "build.generated.dir";
    public static final String BUILD_CLASSES_DIR = "build.classes.dir";
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes";

    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir";

    //================== Start of IcanPro =====================================//
    //FIXME? REPACKAGING
    public static final String JBI_SETYPE_PREFIX = "com.sun.jbi.ui.devtool.jbi.setype.prefix";
    public static final String ASSEMBLY_UNIT_ALIAS = "com.sun.jbi.ui.devtool.jbi.alias.assembly-unit";
    public static final String ASSEMBLY_UNIT_DESCRIPTION = "com.sun.jbi.ui.devtool.jbi.description.assembly-unit";
    public static final String APPLICATION_SUB_ASSEMBLY_ALIAS = "com.sun.jbi.ui.devtool.jbi.alias.application-sub-assembly";
    public static final String APPLICATION_SUB_ASSEMBLY_DESCRIPTION = "com.sun.jbi.ui.devtool.jbi.description.application-sub-assembly";

    public static final String JBI_COMPONENT_CONF_FILE = "com.sun.jbi.ui.devtool.jbi.component.conf.file";
    public static final String JBI_COMPONENT_CONF_ROOT = "com.sun.jbi.ui.devtool.jbi.component.conf.root";
    public static final String JBI_DEPLOYMENT_CONF_FILE = "com.sun.jbi.ui.devtool.jbi.deployment.conf.file";
    public static final String JBI_DEPLOYMENT_CONF_ROOT = "com.sun.jbi.ui.devtool.jbi.deployment.conf.root";
    public static final String DISPLAY_NAME_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.displayName";
    public static final String HOST_NAME_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.hostName";
    public static final String ADMINISTRATION_PORT_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.administrationPort";
    public static final String DOMAIN_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.domain";
    public static final String HTTP_MONITOR_ON_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.httpMonitorOn";
    public static final String HTTP_PORT_NUMBER_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.httpPortNumber";
    public static final String LOCATION_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.location";
    public static final String PASSWORD_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.password";
    public static final String URL_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.url";
    public static final String USER_NAME_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.userName";

    public static final String JBI_REGISTRY_COMPONENT_FILE_KEY = "com.sun.jbi.registry.component.file";
    public static final String JBI_REGISTRY_BROKER_HOST_KEY = "com.sun.jbi.messaging.brokerHost";

    public static final String BC_DEPLOYMENT_JAR = "bcdeployment.jar";
    public static final String SE_DEPLOYMENT_JAR = "sedeployment.jar";    
    
}


