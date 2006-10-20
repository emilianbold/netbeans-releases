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

package org.netbeans.modules.identity.samples.util;

import java.util.ArrayList;
import org.netbeans.modules.identity.samples.util.SoaSampleProjectProperties;


/**
 *
 * @author Vitaly Bychkov
 * @version 2 November 2005
 */
public class SoaSampleProjectProperties {
    public static final String J2EE_SERVER_INSTANCE_PROPERTY = "j2ee.server.instance"; // NOI18N
    public static final String NETBEANS_USERDIR = "netbeans.user"; // NOI18N
    public static final String SERVER_INSTANCE_SUN_APPSERVER = "Sun:AppServer"; // NOI18N
    
    public static final String BPEL_SAMPLES = "Bpel_samples"; // NOI18N
    
    public static final String WEB_PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/web-project/3";   //NOI18N
    
    public static final String APPSERVER_RT_REF = "file.reference.appserv-rt.jar"; // NOI18N
    
    public ArrayList privateProperties;

    private SoaSampleProjectProperties() {
        privateProperties = new ArrayList(); 
        privateProperties.add(J2EE_SERVER_INSTANCE_PROPERTY);
        privateProperties.add(NETBEANS_USERDIR);
        privateProperties.add(APPSERVER_RT_REF);
    }
    
    public static SoaSampleProjectProperties getDefault() {
        return new SoaSampleProjectProperties();
    }
    
    public boolean isPrivateProperty(String name) {
        return privateProperties.contains(name);
    }
}
