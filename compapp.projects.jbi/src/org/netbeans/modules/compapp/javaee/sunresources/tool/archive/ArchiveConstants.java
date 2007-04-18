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

package org.netbeans.modules.compapp.javaee.sunresources.tool.archive;

/**
 * @author echou
 *
 */
public final class ArchiveConstants {

    // accepted archive extensions
    public static final String EAR = "EAR"; // NOI18N
    // ejb jar
    public static final String JAR = "JAR"; // NOI18N
    public static final String WAR = "WAR"; // NOI18N
    public static final String RAR = "RAR"; // NOI18N
    
    // archive types
    public enum ArchiveType { EAR, EJB, WAR, RAR, CLIENT, UNKNOWN }
    
    // DD path
    public static final String EAR_DESCRIPTOR_PATH = "META-INF/application.xml"; // NOI18N
    public static final String SUN_RESOURCES_DESCRIPTOR_PATH = "META-INF/sun-resources.xml"; // NOI18N
    public static final String EJB_DESCRIPTOR_PATH = "META-INF/ejb-jar.xml"; // NOI18N
    public static final String SUN_EJB_DESCRIPTOR_PATH = "META-INF/sun-ejb-jar.xml"; // NOI18N
    public static final String WEB_SERVICES_DESCRIPTOR_PATH = "META-INF/webservices.xml"; // NOI18N
    public static final String JBI_DESCRIPTOR_PATH = "META-INF/jbi.xml"; // NOI18N
    public static final String GRAPH_DESCRIPTOR_PATH = "META-INF/graph.xml"; // NOI18N
    
    // XML tagnames
    // application.xml
    public static final String TAG_APPLICATION = "application"; // NOI18N
    public static final String TAG_APP_MODULE = "module"; // NOI18N
    public static final String TAG_APP_WEB = "web"; // NOI18N
    public static final String TAG_APP_WEBURI = "web-uri"; // NOI18N
    public static final String TAG_APP_EJB = "ejb"; // NOI18N
    public static final String TAG_APP_RAR = "connector"; // NOI18N
    public static final String TAG_APP_CLIENT = "java"; // NOI18N
    
    
    // XML attrs
    public static final String ATTR_VERSION = "version"; // NOI18N
    
}
