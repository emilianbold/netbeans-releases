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

package org.netbeans.modules.mobility.project.deployment;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Adam Sotona
 */
public class MobilityDeploymentProperties extends HashMap<String,Object> implements Runnable {

    public static final String DEPLOYMENT_PREFIX = "deployments."; //NOI18N
    
    /**
     * Creates a new instance of MobilityDeploymentProperties
     */
    public MobilityDeploymentProperties() {
        EditableProperties ep = PropertyUtils.getGlobalProperties();
        for (Map.Entry<String,String> en : ep.entrySet()) {
            String key = en.getKey();
            if (key.startsWith(DEPLOYMENT_PREFIX)) super.put(key, en.getValue());
        }
    }
    
    public Collection<String> getInstanceList(String deploymentTypeName) {
        String pref = DEPLOYMENT_PREFIX + deploymentTypeName + '.';
        int i = pref.length();
        HashSet<String> instances = new HashSet();
        for (String key : keySet()) {
            if (key.startsWith(pref)) {
                int j = key.indexOf('.', i+1);
                if (j > i) instances.add(key.substring(i, j));
            }
        }
        return instances;
    }
    
    public void removeInstance(String deploymentTypeName, String instanceName) {
        String pref = DEPLOYMENT_PREFIX + deploymentTypeName + '.' + instanceName + '.';
        for (String key : keySet().toArray(new String[0])) {
            if (key.startsWith(pref)) remove(key);
        }
        RequestProcessor.getDefault().post(this, 200);
    }
    
    public void createInstance(String deploymentTypeName, String instanceName) {
        for (DeploymentPlugin dp : Lookup.getDefault().lookupAll(DeploymentPlugin.class)) {
            if (deploymentTypeName.equalsIgnoreCase(dp.getDeploymentMethodName())) {
                String pref = DEPLOYMENT_PREFIX + deploymentTypeName + '.' + instanceName + '.';
                Map<String,Object> def = dp.getGlobalPropertyDefaultValues();
                if (def != null) {
                    for (Map.Entry<String,Object> en : def.entrySet()) {
                       if (en.getValue() != null) super.put(pref+en.getKey(), en.getValue().toString());
                    }
                    RequestProcessor.getDefault().post(this, 200);
                    return;
                }
            }
        }
    }

    public void run() {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                EditableProperties ep = PropertyUtils.getGlobalProperties();
                for (String key : ep.keySet().toArray(new String[0])) {
                    if (key.startsWith(DEPLOYMENT_PREFIX)) ep.remove(key);
                }
                for (Map.Entry<String,Object> en : entrySet()) {
                    ep.put(en.getKey(), en.getValue().toString());
                }
                try {
                    PropertyUtils.putGlobalProperties(ep);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
        });
    }

    public Object put(String key, Object value) {
        Object retValue = super.put(key, value);
        RequestProcessor.getDefault().post(this, 200);
        return retValue;
    }
}
