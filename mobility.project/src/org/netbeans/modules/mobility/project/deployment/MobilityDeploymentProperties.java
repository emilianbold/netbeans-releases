/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    
    final RequestProcessor.Task task;
    private final RequestProcessor requestProcessor;

    /**
     * Should use RP owned by the project, but not possible to fix all
     * calls to this constructor
     * @deprecated
     */
    @Deprecated
    public MobilityDeploymentProperties() {
        //Use a single threaded RP
        this (new RequestProcessor());
    }

    /**
     * Creates a new instance of MobilityDeploymentProperties
     */
    public MobilityDeploymentProperties(RequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
        task = requestProcessor.create(this);
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
        task.schedule(200);
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
                    task.schedule(200);
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

    @Override
    public Object put(String key, Object value) {
        Object retValue = super.put(key, value);
        task.schedule(200);
        return retValue;
    }
}
