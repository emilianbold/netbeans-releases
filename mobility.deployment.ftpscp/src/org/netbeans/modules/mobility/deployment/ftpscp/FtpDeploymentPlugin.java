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
 * FtpDeploymentPlugin.java
 *
 */
package org.netbeans.modules.mobility.deployment.ftpscp;

import java.awt.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class FtpDeploymentPlugin implements DeploymentPlugin {
    
    static final String PROP_SERVER = "deployment.ftp.server"; //NOI18N
    static final String PROP_PORT = "deployment.ftp.port"; //NOI18N
    static final String PROP_REMOTEDIR = "deployment.ftp.remotedir"; //NOI18N
    static final String PROP_USERID = "deployment.ftp.userid"; //NOI18N
    static final String PROP_PASSWORD = "deployment.ftp.password"; //NOI18N
    static final String PROP_PASSIVE = "deployment.ftp.passive"; //NOI18N
    static final String PROP_SEPARATOR = "deployment.ftp.separator"; //NOI18N
    static final String SEPARATORS[] = new String[] {"/", "\\"}; //NOI18N
    
    final Map<String,Object> propertyDefValues;
    
    /** Creates a new instance of FtpDeploymentPlugin */
    public FtpDeploymentPlugin() {
        HashMap<String,Object> m = new HashMap<String,Object>();
        m.put(PROP_SERVER, "");//NOI18N
        m.put(PROP_PORT, Integer.valueOf("21"));
        m.put(PROP_REMOTEDIR, "");//NOI18N
        m.put(PROP_USERID, "");//NOI18N
        m.put(PROP_PASSWORD, "");//NOI18N
        m.put(PROP_PASSIVE, Boolean.FALSE);
        m.put(PROP_SEPARATOR, "/");//NOI18N
        propertyDefValues = Collections.unmodifiableMap(m);
    }
    
    public String getAntScriptLocation() {
        return "modules/scr/deploy-ftp-impl.xml"; // NOI18N
    }
    
    public String getDeploymentMethodName() {
        return "Ftp"; // NOI18N
    }
    
    public String getDeploymentMethodDisplayName() {
        return NbBundle.getMessage(FtpDeploymentPlugin.class, "LBL_FtpTypeName"); //NOI18N
    }
    
    public Component createProjectCustomizerPanel() {
        return new FtpCustomizerPanel();
    }
    
    public Map<String,Object> getProjectPropertyDefaultValues() {
        return propertyDefValues;
    }

    public Map<String, Object> getGlobalPropertyDefaultValues() {
        return Collections.EMPTY_MAP;
    }

    public Component createGlobalCustomizerPanel() {
        return null;
    }
}
