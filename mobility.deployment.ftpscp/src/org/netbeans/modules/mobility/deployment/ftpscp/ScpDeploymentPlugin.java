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
 * ScpDeploymentPlugin.java
 *
 */
package org.netbeans.modules.mobility.deployment.ftpscp;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;

/**
 *
 * @author Adam Sotona
 */
public class ScpDeploymentPlugin implements DeploymentPlugin {
    
    static final String PROP_SERVER = "deployment.scp.server"; //NOI18N
    static final String PROP_PORT = "deployment.scp.port"; //NOI18N
    static final String PROP_REMOTEDIR = "deployment.scp.remotedir"; //NOI18N
    static final String PROP_USERID = "deployment.scp.userid"; //NOI18N
    static final String PROP_PASSWORD = "deployment.scp.password"; //NOI18N
    static final String PROP_PASSPHRASE = "deployment.scp.passphrase"; //NOI18N
    static final String PROP_USE_KEYFILE = "deployment.scp.usekeyfile"; //NOI18N
    static final String PROP_KEYFILE = "deployment.scp.keyfile"; //NOI18N
    
    final Map<String,Object> propertyDefValues;
    
    /** Creates a new instance of ScpDeploymentPlugin */
    public ScpDeploymentPlugin() {
        HashMap<String,Object> m = new HashMap<String,Object>();
        m.put(PROP_SERVER, "");//NOI18N
        m.put(PROP_PORT, Integer.valueOf("22"));
        m.put(PROP_USERID, "");//NOI18N
        m.put(PROP_PASSWORD, "");//NOI18N
        m.put(PROP_PASSPHRASE, "");//NOI18N
        m.put(PROP_KEYFILE, new File(""));//NOI18N
        m.put(PROP_USE_KEYFILE, "no");//NOI18N
        propertyDefValues = Collections.unmodifiableMap(m);
    }
    
    public String getAntScriptLocation() {
        return "modules/scr/deploy-scp-impl.xml"; // NOI18N
    }
    
    public String getDeploymentMethodName() {
        return "Scp";
    }
    
    public String getDeploymentMethodDisplayName() {
        return NbBundle.getMessage(ScpDeploymentPlugin.class, "LBL_ScpTypeName"); //NOI18N
    }
    
    public synchronized Component createProjectCustomizerPanel() {
        return new ScpProjectCustomizerPanel();
    }
    
    public Map<String,Object> getProjectPropertyDefaultValues() {
        return Collections.singletonMap(PROP_REMOTEDIR, (Object)"");//NOI18N
    }

    public Map<String, Object> getGlobalPropertyDefaultValues() {
        return propertyDefValues;
    }

    public Component createGlobalCustomizerPanel() {
        return new ScpCustomizerPanel();
    }
}
