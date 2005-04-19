/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.spi.webservices;

/**
 * @author  rico
 */
public interface WebServicesConstants {
    public static final String WEBSERVICES_DD = "webservices";//NOI18N
    public static final String WEB_SERVICES =     "web-services";//NOI18N
    public static final String WEB_SERVICE  =     "web-service";//NOI18N
    public static final String WEB_SERVICE_NAME = "web-service-name";//NOI18N
    public static final String WEB_SERVICE_FROM_WSDL = "from-wsdl"; // NOI18N
    public static final String CONFIG_PROP_SUFFIX = ".config.name";//NOI18N
    public static final String MAPPING_PROP_SUFFIX = ".mapping";//NOI18N
    public static final String MAPPING_FILE_SUFFIX = "-mapping.xml";//NOI18N
    public static final String WebServiceServlet_PREFIX = "WSServlet_";//NOI18N

    public static final String WSDL_FOLDER = "wsdl"; // NOI18N
    public static final String WEB_SERVICE_CLIENTS = "web-service-clients"; //NOI18N
    public static final String WEB_SERVICE_CLIENT = "web-service-client"; //NOI18N
    public static final String WEB_SERVICE_CLIENT_NAME = "web-service-client-name"; //NOI18N
    public static final String WEB_SERVICE_STUB_TYPE = "web-service-stub-type"; //NOI18N
    public static final String CLIENT_SOURCE_URL = "client-source-url"; //NOI18N
    public static final String WSCOMPILE="wscompile"; //NOI18N
    public static final String WSCOMPILE_CLASSPATH = "wscompile.classpath"; //NOI18N
    public static final String WSCOMPILE_TOOLS_CLASSPATH = "wscompile.tools.classpath"; //NOI18N
    public static final String WEBSVC_GENERATED_DIR = "websvc.generated.dir"; // NOI18N
    public static final String J2EE_PLATFORM_WSCOMPILE_CLASSPATH="j2ee.platform.wscompile.classpath"; //NOI18N
    public static final String [] WSCOMPILE_JARS = {
        "${" + J2EE_PLATFORM_WSCOMPILE_CLASSPATH + "}", //NOI18N
        "${wscompile.tools.classpath}" //NOI18N
    };
}
