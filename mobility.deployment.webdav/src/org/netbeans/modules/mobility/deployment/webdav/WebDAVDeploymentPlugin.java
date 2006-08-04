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
 * WebDAVDeploymentPlugin.java
 *
 */
package org.netbeans.modules.mobility.deployment.webdav;

import java.awt.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;

/**
 *
 * @author Adam Sotona
 */
public class WebDAVDeploymentPlugin implements DeploymentPlugin {
    
    static final String PROP_SERVER = "deployment.webdav.server"; //NOI18N
    static final String PROP_PORT = "deployment.webdav.port"; //NOI18N
    static final String PROP_REMOTEDIR = "deployment.webdav.remotedir"; //NOI18N
    static final String PROP_USERID = "deployment.webdav.userid"; //NOI18N
    static final String PROP_PASSWORD = "deployment.webdav.password"; //NOI18N
    
    final Map<String,Object> propertyDefValues;
    
    /** Creates a new instance of WebDAVDeploymentPlugin */
    public WebDAVDeploymentPlugin() {
        HashMap<String,Object> m = new HashMap<String,Object>();
        m.put(PROP_SERVER, "");//NOI18N
        m.put(PROP_PORT, Integer.valueOf("80"));
        m.put(PROP_REMOTEDIR, "");//NOI18N
        m.put(PROP_USERID, "");//NOI18N
        m.put(PROP_PASSWORD, "");//NOI18N
        propertyDefValues = Collections.unmodifiableMap(m);
    }
    
    public String getAntScriptLocation() {
        return "modules/scr/deploy-webdav-impl.xml"; // NOI18N
    }
    
    public String getDeploymentMethodName() {
        return "WebDAV"; // NOI18N
    }
    
    public String getDeploymentMethodDisplayName() {
        return NbBundle.getMessage(WebDAVDeploymentPlugin.class, "LBL_WebDAVTypeName"); //NOI18N
    }
    
    public synchronized Component createCustomizerPanel() {
        return new WebDAVCustomizerPanel();
    }
    
    public Map<String,Object> getPropertyDefaultValues() {
        return propertyDefValues;
    }
}
