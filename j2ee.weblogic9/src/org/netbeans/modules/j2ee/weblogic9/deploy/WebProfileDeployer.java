/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.lang.String;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.weblogic9.WLConnectionSupport;

/**
 *
 * @author Petr Hejl
 */
public final class WebProfileDeployer {

    private static final Logger LOGGER = Logger.getLogger(WebProfileDeployer.class.getName());

    private final WLDeploymentManager deploymentManager;

    public WebProfileDeployer(WLDeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targets) {
        Set<TargetModuleID> result = getAvailableModules(targets, moduleType).keySet();
        return result.toArray(new TargetModuleID[result.size()]);
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targets) {
        return getFilteredModules(moduleType, targets, "STATE_ACTIVE", false); // NOI18N
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targets) {
        return getFilteredModules(moduleType, targets, "STATE_ACTIVE", true); // NOI18N
    }

    private TargetModuleID[] getFilteredModules(ModuleType moduleType,
            Target[] targets, String filter, boolean inverted) {
        List<TargetModuleID> result = new ArrayList<TargetModuleID>();
        for (Map.Entry<TargetModuleID, String> entry : getAvailableModules(targets, moduleType).entrySet()) {
            if (inverted) {
                if (!filter.equals(entry.getValue())) {
                    result.add(entry.getKey());
                }
            } else {
                if (filter.equals(entry.getValue())) {
                    result.add(entry.getKey());
                }
            }
        }
        return result.toArray(new TargetModuleID[result.size()]);
    }

    private Map<TargetModuleID, String> getAvailableModules(final Target[] targets, final ModuleType moduleType) {
        // TODO we should rather really check the module type
        if (!ModuleType.WAR.equals(moduleType)) {
            return Collections.emptyMap();
        }

        // TODO - parent/child relationship ?
        WLConnectionSupport support = new WLConnectionSupport(deploymentManager);
        try {
            Map<TargetModuleID, String> result = support.executeAction(new WLConnectionSupport.JMXAction<Map<TargetModuleID, String>>() {

                @Override
                public Map<TargetModuleID, String> call(MBeanServerConnection connection) throws Exception {
                    Set<ObjectName> mgr = connection.queryNames(new ObjectName("com.bea:Name=DeploymentManager,Type=DeploymentManager,*"), null); // NOI18N
                    Iterator<ObjectName> mgrIt = mgr.iterator();
                    if (!mgrIt.hasNext()) {
                        return Collections.emptyMap();
                    }

                    Map<TargetModuleID, String> result = new HashMap<TargetModuleID, String>();
                    ObjectName jmxManager = (ObjectName) mgrIt.next();
                    ObjectName[] appDeploymentRuntimes = (ObjectName[]) connection.getAttribute(jmxManager, "AppDeploymentRuntimes"); // NOI18N
                    if (appDeploymentRuntimes != null) {
                        for (ObjectName app : appDeploymentRuntimes) {
                            String name = (String) connection.getAttribute(app, "Name"); // NOI18N
                            for (Target target : targets) {
                                String state = (String) connection.invoke(app, "getState", // NOI18N
                                        new Object[] {target.getName()}, new String[] {"java.lang.String"}); // NOI18N

                                WLTargetModuleID module = new WLTargetModuleID(target, name);

                                if ("STATE_ACTIVE".equals(state)) { // NOI18N
                                    ObjectName appRuntime = new ObjectName(
                                            "com.bea:ServerRuntime=" + target.getName() + ",Name=" + name + ",Type=ApplicationRuntime"); // NOI18N
                                    ObjectName[] componentRuntimes = (ObjectName[]) connection.getAttribute(appRuntime, "ComponentRuntimes"); // NOI18N
                                    for (ObjectName n: componentRuntimes) {
                                        String root = (String) connection.getAttribute(n, "ContextRoot"); // NOI18N
                                        if (root != null) {
                                            module.setContextURL(
                                                    "http://" + deploymentManager.getHost() // NOI18N
                                                    + ":" + deploymentManager.getPort() + root); // NOI18N
                                            break;
                                        }
                                    }
                                }
                                result.put(module, state);
                            }
                        }
                    }

                    return result;
                }

                @Override
                public String getPath() {
                    return null;
                }
            });
            return result;
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
            return Collections.emptyMap();
        }
    }


}
