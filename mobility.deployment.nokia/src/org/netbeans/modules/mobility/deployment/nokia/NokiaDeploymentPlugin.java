/*
 * Copyright 2005, 2007 Nokia Corporation. All rights reserved.
 *  
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). See LICENSE.TXT for exact terms.
 * You may not use this file except in compliance with the License.  You can obtain a copy of the
 * License at http://www.netbeans.org/cddl.html
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

package org.netbeans.modules.mobility.deployment.nokia;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import com.nokia.phone.deploy.*;
import java.util.Collections;
import javax.swing.JLabel;

/**
 * Provides deployment functionality to Nokia devices.
 */
public class NokiaDeploymentPlugin implements DeploymentPlugin {

    static final String PROP_DEPLOY_TO_ALL_DEVICES = "deployment.nokia.deploytoalldevices"; //NOI18N
    static final String PROP_DEPLOY_TO_SELECTED_DEVICES = "deployment.nokia.deploytoselecteddevices"; //NOI18N
    static final String PROP_SELECTED_DEVICES = "deployment.nokia.selecteddevices"; //NOI18N
    final Map<String,Object> propertyDefValues;

    Deployer deployer = null;
    
    /** Creates a new instance of TestDeploymentPlugin */
    public NokiaDeploymentPlugin() {
        deployer = new Deployer();
        HashMap<String,Object> m = new HashMap<String,Object>();
        m.put(PROP_DEPLOY_TO_ALL_DEVICES, "yes");//NOI18N
        m.put(PROP_SELECTED_DEVICES, "");//NOI18N
        propertyDefValues = Collections.unmodifiableMap(m);
    }
    
    public String getDeploymentMethodName() {
        return "NokiaDeployment"; //NOI18N
    }

    public String getDeploymentMethodDisplayName() {
        return org.openide.util.NbBundle.getMessage(NokiaDeploymentPlugin.class, "DeploymentPlugin.MethodDisplayName");
    }

    public String getAntScriptLocation() {
        return "modules/scr/deploy-nokia-impl.xml"; //NOI18N
    }

    public Map<String,Object> getProjectPropertyDefaultValues() {
        return propertyDefValues;
    }

    public Map<String,Object> getGlobalPropertyDefaultValues() {
        return propertyDefValues;
    }

    public Component createProjectCustomizerPanel() {
        if(!deployer.isOSSupportsDeployment()) {
              return new JLabel(org.openide.util.NbBundle.getMessage(NokiaDeploymentPlugin.class,
                                                                     "NokiaDeploymentPlugin.OSNotSupported"));
        } else if(!deployer.isPCSuiteInstalled() || !deployer.isConnected()) {
              return new JLabel(org.openide.util.NbBundle.getMessage(NokiaDeploymentPlugin.class,
                                                                     "NokiaDeploymentPlugin.PCSuiteNotAvailable"));
        } else {
            return new NokiaProjectCustomizerPanel(deployer);
        }
    }

    public Component createGlobalCustomizerPanel() {
        return createProjectCustomizerPanel();
    }
}
