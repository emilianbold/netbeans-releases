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
package org.netbeans.modules.bpel.model.api.resources;

/**
 * This is class for mark its pacakge.
 * We can access to files in this package via pointer to this class.
 * If package chages his name it will automatically reflected in usages
 * ( via refactoring ).
 * @author ads
 */
public final class ResourcePackageMarker {
    
    private ResourcePackageMarker() {}
    
    public static final String WS_BPEL_SCHEMA = "wsbpel_2_0.xsd"; // NOI18N
    public static final String WS_BPEL_1_1_SCHEMA = "bpel4ws_1_1.xsd"; // NOI18N
    public static final String WS_BPEL_SERVICE_REF_SCHEMA = "ws-bpel_serviceref.xsd"; // NOI18N
    public static final String XSD_SCHEMA = "xml.xsd"; // NOI18N
    public static final String TRACE_SCHEMA = "trace.xsd"; // NOI18N
    public static final String EDITOR_EXT_SCHEMA = "editor.xsd"; // NOI18N
    public static final String ERROR_HANDLING_WSDL = "ErrorHandling.wsdl"; // NOI18N
    public static final String ERROR_HANDLING_SCHEMA = "ErrorHandling.xsd"; // NOI18N

    /**
     * fix for IZ94241:
     * class.getPackage() returns null with antClassloader
     * workaround is to use class name and extract package name from it.
     *
     * @returns package name for this class
     **/
    public static String getPackage(){
        String result = ResourcePackageMarker.class.getName();
        int last = result.lastIndexOf("."); 
        return result.substring(0, last).replace('.','/');
    }
}
