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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * Constants.java
 *
 * Created on January 16, 2004, 4:16 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping;

/**
 *
 * @author  nityad
 */
public interface Constants {

    static final String MAP_J2EEAPP_STANDALONE = "com.sun.appserv:type=applications,category=config"; //NOI18N

    //Server MBean
    static final String OBJ_J2EE = "com.sun.appserv:j2eeType=J2EEServer,name=server,category=runtime"; //NOI18N
    static final String[] JSR_SERVER_INFO = {"debugPort", "nodes", "serverVersion", "restartRequired", "serverVendor" }; //NOI18N
    static final String[] ADDITIONAL_SERVER_INFO = {"port", "domain" }; //NOI18N
    
    static final String MAP_RESOURCES = "com.sun.appserv:type=resources,category=config";//NOI18N               
    
    static final String MAP_JVMOptions = "com.sun.appserv:type=java-config,config=server-config,category=config"; //NOI18N
    static final String DEBUG_OPTIONS = "debug-options"; //NOI18N
    static final String JAVA_HOME = "java-home"; //NOI18N 
    static final String DEBUG_OPTIONS_ADDRESS = "address="; //NOI18N 
    static final String JPDA_PORT = "jpda_port_number"; //NOI18N 
    static final String SHARED_MEM = "shared_memory"; //NOI18N 
    static final String ISMEM = "transport=dt_shmem"; //NOI18N
    static final String ISSOCKET = "transport=dt_socket"; //NOI18N
    static final String DEF_DEUG_OPTIONS = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044"; //NOI18N                         
    static final String DEF_DEUG_OPTIONS_81 = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009"; //NOI18N                         
    static final String DEF_DEUG_OPTIONS_SOCKET = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=11000"; //NOI18N                         
    static final String DEF_DEUG_OPTIONS_SHMEM = "-Xdebug -Xrunjdwp:transport=dt_shmem,server=y,suspend=n,address="; //NOI18N                         
    
    //Config Mbean Queries
    static final String[] CONFIG_MODULE = {"web-module", "j2ee-application", "ejb-module", "connector-module", "appclient-module"}; //NOI18N
    
}
